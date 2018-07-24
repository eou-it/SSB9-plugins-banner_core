/* ******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package banner.core

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.web.filter.GrailsAnonymousAuthenticationFilter
import grails.plugins.Plugin
import grails.util.Environment
import grails.util.GrailsClassUtils as GCU
import grails.util.Holders
import grails.util.Holders  as CH
import groovy.util.logging.Slf4j
import net.hedtech.banner.db.BannerDS
import net.hedtech.banner.db.BannerDS as BannerDataSource
import net.hedtech.banner.mep.MultiEntityProcessingService
import net.hedtech.banner.security.*
import net.hedtech.banner.service.DefaultLoaderService
import net.hedtech.banner.service.HttpSessionService
import net.hedtech.banner.service.LoginAuditService
import net.hedtech.banner.service.ServiceBase
import oracle.jdbc.pool.OracleDataSource
import org.apache.commons.dbcp.BasicDataSource
import org.codehaus.groovy.runtime.GStringImpl
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor as NativeJdbcExtractor
import org.springframework.jndi.JndiObjectFactoryBean
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextPersistenceFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

import javax.servlet.Filter
import javax.sql.DataSource
import java.util.concurrent.Executors

/****************** Added for DS inject fix *****************/
//import org.grails.orm.hibernate.ConfigurableLocalSessionFactoryBean
/**
 * A Grails Plugin supporting cross cutting concerns.
 *
 */
@Slf4j
class BannerCoreGrailsPlugin extends Plugin {
    String version = "9.28.1"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.2 > *"

    // the other plugins this plugin depends on
    List loadAfter = ['springSecurityCore']
    def dependsOn =  [
                       springSecurityCore: '3.2.1 => *'
                     ]

    // resources that are excluded from plugin packaging
    def pluginExcludes = ["grails-app/views/error.gsp"]

    def author = "ellucian"
    def authorEmail = ""
    def title = "Banner Core Framework Plugin"
    def description = '''This plugin adds Spring Security (aka Acegi) and a custom
                         |DataSource implementation (BannerDataSource) that together
                         |provide for authentication and authorization based upon
                         |Banner Security configuration. In addition, this plugin provides
                         |additional framework support (e.g., injecting CRUD methods into
                         |services, providing base test classes) to facilitate development of
                         |Banner web applications.'''.stripMargin()

    def documentation = ""

    def profiles = ['web']


    Closure doWithSpring() { {->
        //TODO :grails_332_change, needs to revisit
        setupExternalConfig()

        // TODO :grails_332_change, needs to revisit
        // secureAdhocPatterns()
        def conf = SpringSecurityUtils.securityConfig
        switch (Environment.current) {
            case Environment.PRODUCTION:
                log.info "Will use a dataSource configured via JNDI"
                underlyingDataSource(JndiObjectFactoryBean) {
                    jndiName = "java:comp/env/${CH.config.bannerDataSource.jndiName}"
                }
                if (isSsbEnabled()) {
                    underlyingSsbDataSource(JndiObjectFactoryBean) {
                        jndiName = "java:comp/env/${CH.config.bannerSsbDataSource.jndiName}"
                    }
                }
                break
            default: // we'll use our locally configured dataSource for development and test environments
                log.info "Using development/test datasource"
                underlyingDataSource(BasicDataSource) {
                    maxActive = 5
                    maxIdle = 2
                    defaultAutoCommit = "false"
                    driverClassName = "${CH.config.bannerDataSource.driver}"
                    url = "${CH.config.bannerDataSource.url}"
                    password = "${CH.config.bannerDataSource.password}"
                    username = "${CH.config.bannerDataSource.username}"
                }
                if (isSsbEnabled()) {
                    underlyingSsbDataSource(BasicDataSource) {
                        maxActive = 5
                        maxIdle = 2
                        defaultAutoCommit = "false"
                        driverClassName = "${CH.config.bannerSsbDataSource.driver}"
                        url = "${CH.config.bannerSsbDataSource.url}"
                        password = "${CH.config.bannerSsbDataSource.password}"
                        username = "${CH.config.bannerSsbDataSource.username}"
                    }

                }
                break
        }

        nativeJdbcExtractor(NativeJdbcExtractor)

        dataSource(BannerDataSource) {
            underlyingDataSource = ref(underlyingDataSource)
            try {
                underlyingSsbDataSource = ref(underlyingSsbDataSource)
            } catch (MissingPropertyException) { } // don't inject it if we haven't configured this datasource
            nativeJdbcExtractor = ref(nativeJdbcExtractor)
        }

        sqlExceptionTranslator(org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator, 'Oracle') {
            dataSource = ref(dataSource)
        }

        userAuthorityService(BannerGrantedAuthorityService) { bean ->
        }

        multiEntityProcessingService(MultiEntityProcessingService) { bean ->
            dataSource = ref(dataSource)
            sessionFactory = ref('sessionFactory')
            bean.initMethod = 'init'
        }

        bannerAuthenticationFailureHandler(BannerAuthenticationFailureHandler){ bean ->
            defaultFailureUrl = SpringSecurityUtils.securityConfig.failureHandler.defaultFailureUrl
        }

        roleVoter(BannerAccessDecisionVoter)

        httpSessionService(HttpSessionService) {
            dataSource = ref(dataSource)
        }

        authenticationDataSource(OracleDataSource)

        loginAuditService(LoginAuditService) {
            dataSource = ref(dataSource)
        }

        defaultLoaderService(DefaultLoaderService) {
            dataSource = ref(dataSource)
        }


        bannerAuthenticationProvider(BannerAuthenticationProvider) {
            dataSource = ref(dataSource)
            authenticationDataSource = ref(authenticationDataSource)
        }

        selfServiceBannerAuthenticationProvider(SelfServiceBannerAuthenticationProvider) {
            dataSource = ref(dataSource)
        }

        bannerPreAuthenticatedFilter(BannerPreAuthenticatedFilter) {
            dataSource = ref(dataSource)
            authenticationManager = ref('authenticationManager')
        }


        bannerMepCodeFilter(BannerMepCodeFilter)

        basicAuthenticationEntryPoint(BasicAuthenticationEntryPoint) {
            realmName = 'Banner REST API Realm'
        }

        statelessSecurityContextRepository(HttpSessionSecurityContextRepository) {
            allowSessionCreation = false
            disableUrlRewriting = false
        }

        /*
    statelessSecurityContextPersistenceFilter(SecurityContextPersistenceFilter) {
        securityContextRepository = ref('statelessSecurityContextRepository')
        forceEagerSessionCreation = false
    }
        */

        statelessSecurityContextPersistenceFilter(SecurityContextPersistenceFilter , ref('statelessSecurityContextRepository')) {
            //securityContextRepository = ref('statelessSecurityContextRepository')
            forceEagerSessionCreation = false
        }

        /*
    basicAuthenticationFilter(BasicAuthenticationFilter) {
        authenticationManager = ref('authenticationManager')
        authenticationEntryPoint = ref('basicAuthenticationEntryPoint')
    }
        */

        basicAuthenticationFilter(BasicAuthenticationFilter , ref('authenticationManager'),  ref('basicAuthenticationEntryPoint')) {
            //authenticationManager = ref('authenticationManager')
            //authenticationEntryPoint = ref('basicAuthenticationEntryPoint')
        }

        /*
    basicExceptionTranslationFilter(ExceptionTranslationFilter) {
        authenticationEntryPoint = ref('basicAuthenticationEntryPoint')
        accessDeniedHandler = ref('accessDeniedHandler')
    }
        */

        basicExceptionTranslationFilter(ExceptionTranslationFilter , ref('basicAuthenticationEntryPoint')) {
            //authenticationEntryPoint = ref('basicAuthenticationEntryPoint')
            accessDeniedHandler = ref('accessDeniedHandler')
        }

        anonymousProcessingFilter(GrailsAnonymousAuthenticationFilter) {
            authenticationDetailsSource = ref('authenticationDetailsSource')
            key = 'horizon-anon'
        }

        applicationEventMulticaster(SimpleApplicationEventMulticaster) {
            taskExecutor = Executors.newCachedThreadPool()
        }

        resetPasswordService(ResetPasswordService) {
            dataSource = ref(dataSource)
            authenticationDataSource = ref(authenticationDataSource)
            sessionFactory = ref('sessionFactory')
        }

        // ---------------- JMX Mbeans (incl. Logging) ----------------

        /*
    log4jBean(HierarchyDynamicMBean)

    mbeanServer(MBeanServerFactoryBean) {
        locateExistingServerIfPossible = true
    }

    switch (GrailsUtil.environment) {
        case "development": // 'pass through', so logging will be exported via JMX for 'development' and 'production'
        case "production":
            String log4jBeanName = getUniqueJmxBeanNameFor('log4j') + ':hierarchy=default'

            exporter(MBeanExporter) {
                server = mbeanServer
                beans = [("$log4jBeanName" as String): log4jBean]
            }
            break
    }
        */

        // Switch to grails.util.Holders in Grails 2.x
        if (!CH.config.privacy?.codes) {
            // Populate with default privacy policy codes
            CH.config.privacy.codes = "INT NAV UNI"
        }
    }
    }

    void doWithDynamicMethods() {
        // Deprecated -- the following mixes in the ServiceBase class that provides default CRUD methods,
        // into all services having a 'static boolean defaultCrudMethods = true' property.
        // This approach is deprecated in favor of extending from the ServiceBase base class.
        // Extending from ServiceBase enables declarative Transaction demarcation using annotations.
        // Mixing in the base class requires the 'boolean transactional = true' line, and does not provide
        // the more granular control of transaction attributes possible with annotations.
        //

        /*
        application.serviceClasses.each { serviceArtefact ->
            def needsCRUD = GCU.getStaticPropertyValue(serviceArtefact.clazz, "defaultCrudMethods")
            if (needsCRUD) {
                serviceArtefact.clazz.mixin ServiceBase
            }
        }
        */

        grailsApplication.serviceClasses.each { serviceArtefact ->
            def needsCRUD = GCU.getStaticPropertyValue(serviceArtefact.clazz, "defaultCrudMethods")
            if (needsCRUD) {
                serviceArtefact.clazz.mixin ServiceBase
            }
        }

        String.metaClass.flattenString = {
            return delegate.replace("\n", "").replaceAll(/  */, " ")
        }

        GString.metaClass.flattenString = {
            return delegate.replace("\n", "").replaceAll(/  */, " ")
        }
    }

    void doWithApplicationContext() {

        println "==============> doWithApplicationContext() datasourse fix start ===========>"

        def sessionFactory = applicationContext.sessionFactory

        ConnectionProvider connectionProvider = ((SessionFactoryImplementor) sessionFactory).getServiceRegistry().getService(ConnectionProvider.class);
        if(connectionProvider instanceof DatasourceConnectionProviderImpl) {
            DataSource ds = ((DatasourceConnectionProviderImpl) connectionProvider).getDataSource();
            if(ds instanceof TransactionAwareDataSourceProxy) {
                println "======= Yes ====== TransactionAwareDataSourceProxy"
                //DataSource dsTemp = ((TransactionAwareDataSourceProxy) ds).getTargetDataSource();
                //BannerDS bannerds = new BannerDS()
                BannerDS bannerds = applicationContext.getBean("dataSource")
                ((TransactionAwareDataSourceProxy) ds).setTargetDataSource(bannerds)

            }
            if (ds instanceof LazyConnectionDataSourceProxy){
                println "======= Yes ====== LazyConnectionDataSourceProxy"
            }

        }

        println "==============> doWithApplicationContext() datasourse fix end ===========>"


        // build providers list here to give dependent plugins a chance to register some
        def conf = SpringSecurityUtils.securityConfig
        def providerNames = []
        if (conf.providerNames) {
            providerNames.addAll conf.providerNames
        }
        else {
            if (isSsbEnabled()) providerNames = ['selfServiceBannerAuthenticationProvider', 'bannerAuthenticationProvider']
            else providerNames = ['bannerAuthenticationProvider']
        }
        applicationContext.authenticationManager.providers = createBeanList(providerNames, applicationContext)


        //Hibernate Event Listeners commented for Grails-3 as it has been chanaged.

        /*
        def listeners = applicationContext.sessionFactory.eventListeners

        // register hibernate listener for populating audit trail properties before inserting and updating models
        def auditTrailSupportListener = new AuditTrailPropertySupportHibernateListener()
        ['preInsert', 'preUpdate'].each {
            addEventTypeListener(listeners, auditTrailSupportListener, it)
        }
        */

        // Define the spring security filters
        def authenticationProvider = CH?.config?.banner.sso.authenticationProvider
        println "authenticationProvider = " +authenticationProvider
        LinkedHashMap<String, String> filterChain = new LinkedHashMap();
        switch (authenticationProvider) {
            case 'external':
                filterChain['/**/api/**'] = 'statelessSecurityContextPersistenceFilter,bannerMepCodeFilter,authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor'
                filterChain['/**/qapi/**'] = 'statelessSecurityContextPersistenceFilter,bannerMepCodeFilter,authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor'
                filterChain['/**'] = 'securityContextPersistenceFilter,logoutFilter,bannerMepCodeFilter,bannerPreAuthenticatedFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor'
                break
            case 'saml':
                break
            default:
                filterChain['/**/api/**'] = 'statelessSecurityContextPersistenceFilter,bannerMepCodeFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor'
                filterChain['/**/qapi/**'] = 'statelessSecurityContextPersistenceFilter,bannerMepCodeFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor'
                filterChain['/**'] = 'securityContextPersistenceFilter,logoutFilter,bannerMepCodeFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor'
                break
        }

        LinkedHashMap<RequestMatcher, List<Filter>> filterChainMap = new LinkedHashMap()
        filterChain.each { key, value ->
            def filters = value.toString().split(',').collect {
                name -> applicationContext.getBean(name)
            }
            filterChainMap[new AntPathRequestMatcher(key)] = filters
        }

        //applicationContext.springSecurityFilterChain.filterChainMap = filterChainMap

        //set the teransaction timeout on transaction manager time unit in seconds
        def transTimeOut = CH.config.banner?.transactionTimeout instanceof Integer ? CH.config.banner?.transactionTimeout : 30
        applicationContext.getBean('transactionManager')?.setDefaultTimeout(transTimeOut)
    }

    void onChange(Map<String, Object> event) {
        // no-op
    }

    void onConfigChange(Map<String, Object> event) {
        secureAdhocPatterns()
    }

    void onShutdown(Map<String, Object> event) {
        // no-op
    }
    def addEventTypeListener(listeners, listener, type) {
        def typeProperty = "${type}EventListeners"
        def typeListeners = listeners."${typeProperty}"

        def expandedTypeListeners = new Object[typeListeners.length + 1]
        System.arraycopy(typeListeners, 0, expandedTypeListeners, 0, typeListeners.length)
        expandedTypeListeners[-1] = listener

        listeners."${typeProperty}" = expandedTypeListeners
    }


    private def isSsbEnabled() {
        CH.config.ssbEnabled instanceof Boolean ? CH.config.ssbEnabled : false
    }


    private def getUniqueJmxBeanNameFor(String name) {
        def nameToRegister = CH.config.jmx.exported."$name"
        if (nameToRegister instanceof String || nameToRegister instanceof GStringImpl) {
            return "$nameToRegister" as String
        } else {
            return name
        }
    }

    private static void secureAdhocPatterns (){

        def DEFAULT_ADHOC_INCLUDES = [
                '/images/**', '/css/**', '/js/**', '/plugins/**'
        ]

        def DEFAULT_ADHOC_EXCLUDES = [
                '/WEB-INF/**'
        ]

        def resourcesConfig = Holders.grailsApplication.config.grails.resources

        if (!resourcesConfig.adhoc.includes ) {
            log.warn("No grails.resources.adhoc.includes specified... adding default includes: " + DEFAULT_ADHOC_INCLUDES)
            resourcesConfig.adhoc.includes = DEFAULT_ADHOC_INCLUDES
        }

        if (!resourcesConfig.adhoc.excludes ) {
            log.warn("No grails.resources.adhoc.excludes specified... adding default excludes: " + DEFAULT_ADHOC_EXCLUDES)
            resourcesConfig.adhoc.excludes = DEFAULT_ADHOC_EXCLUDES
        } else if (!resourcesConfig.adhoc.excludes.contains('/WEB-INF/**')) {
            log.warn("Specified grails.resources.adhoc.excludes does not exclude WEB-INF ... appending default excludes: " + DEFAULT_ADHOC_EXCLUDES)
            resourcesConfig.adhoc.excludes.addAll(DEFAULT_ADHOC_EXCLUDES)
        }

        log.info("Final grails.resources.adhoc.includes" + resourcesConfig.adhoc.includes)
        log.info("Final grails.resources.adhoc.excludes" + resourcesConfig.adhoc.excludes)
    }

    private createBeanList(names, ctx) { names.collect { name -> ctx.getBean(name) } }


    private setupExternalConfig() {
        def config = CH.config
        def locations = config.grails.config.locations
        String filePathName

        locations.each { propertyName,  fileName ->
            filePathName = getFilePath(System.getProperty(propertyName))
            if (Environment.getCurrent() != Environment.PRODUCTION) {
                if (!filePathName) {
                    filePathName = getFilePath("${System.getProperty('user.home')}/.grails/${fileName}")
                    if (filePathName) log.info "Using configuration file '\$HOME/.grails/$fileName'"
                }
                if (!filePathName) {
                    filePathName = getFilePath("${fileName}")
                    if (filePathName) log.info "Using configuration file '$fileName'"
                }
                if (!filePathName) {
                    filePathName = getFilePath("grails-app/conf/$fileName")
                    if (filePathName) log.info "Using configuration file 'grails-app/conf/$fileName'"
                }
            } else {
                //filePathName = Thread.currentThread().getContextClassLoader().getResource( "$fileName" )?.toURI()
                //filePathName = "classpath:$fileName"
            }
            if(filePathName) {
                println "External configuration file: " + filePathName
                try {
                    config.merge(new ConfigSlurper().parse(new File(filePathName).text))
                }
                catch (e) {
                    log.warn "NOTICE: Caught exception while loading configuration files (depending on current grails target, this may be ok): ${e.message}"
                }
            }
        }
    }

    private static String getFilePath( filePath ) {
        if (filePath && new File( filePath ).exists()) {
            "${filePath}"
        }
    }

}
