/* ******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package banner.core

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.web.GrailsSecurityFilterChain
import grails.plugin.springsecurity.web.filter.GrailsAnonymousAuthenticationFilter
import grails.plugins.Plugin
import grails.util.Environment
import grails.util.GrailsClassUtils as GCU
import grails.util.Holders
import grails.util.Holders  as CH
import grails.util.Metadata
import groovy.util.logging.Slf4j
import net.hedtech.banner.db.BannerDS as BannerDataSource
import net.hedtech.banner.db.BannerDataSourceConnectionSourceFactory
import net.hedtech.banner.mep.MultiEntityProcessingService
import net.hedtech.banner.security.*
import net.hedtech.banner.service.DefaultLoaderService
import net.hedtech.banner.service.HttpSessionService
import net.hedtech.banner.service.LoginAuditService
import net.hedtech.banner.service.ServiceBase
import oracle.jdbc.pool.OracleDataSource
import org.apache.commons.dbcp.BasicDataSource
import org.codehaus.groovy.runtime.GStringImpl
import org.grails.orm.hibernate.HibernateEventListeners
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.core.Ordered
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor as NativeJdbcExtractor
import org.springframework.jndi.JndiObjectFactoryBean
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextPersistenceFilter
import net.hedtech.banner.service.AuditTrailPropertySupportHibernateListener

import javax.servlet.Filter
import java.util.concurrent.Executors

/**
 * A Grails Plugin supporting cross cutting concerns.
 *
 */
@Slf4j
class BannerCoreGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.2 > *"

    // the other plugins this plugin depends on
    List loadAfter = ['hibernate','i18nCore','springSecurityCore','springSecuritySaml','springSecurityCas']
    //List loadBefore = ['springSecuritySaml','springSecurityCas']

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
        String appName = Metadata.current.getApplicationName()
        println "AppName is = ${appName}"
        setupExternalConfig()
        String serverType = CH.config.targetServer
        switch (Environment.current) {
            case Environment.PRODUCTION:
                if(serverType?.equalsIgnoreCase('weblogic')){
                    log.info "weblogic Will use a dataSource configured via JNDI"
                    underlyingDataSource(JndiObjectFactoryBean) {
                        jndiName = "${Holders.config.bannerDataSource.jndiName}"
                    }
                    if (isSsbEnabled()) {
                        underlyingSsbDataSource(JndiObjectFactoryBean) {
                            jndiName = "${Holders.config.bannerSsbDataSource.jndiName}"
                        }
                    }
                } else {
                    log.info "Tomcat Will use a dataSource configured via JNDI"
                    underlyingDataSource(JndiObjectFactoryBean) {
                        jndiName = "java:comp/env/${CH.config.bannerDataSource.jndiName}"
                    }
                    if (isSsbEnabled()) {
                        underlyingSsbDataSource(JndiObjectFactoryBean) {
                            jndiName = "java:comp/env/${CH.config.bannerSsbDataSource.jndiName}"
                        }
                    }
                }
                break
            default: // we'll use our locally configured dataSource for development and test environments
                log.info "Using development/test datasource"
                underlyingDataSource(BasicDataSource) {
                    maxActive = 5// Define the spring security filters
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

      /*  dataSource(BannerDataSource) {
            underlyingDataSource = ref(underlyingDataSource)
            try {
                underlyingSsbDataSource = ref(underlyingSsbDataSource)
            } catch (MissingPropertyException) { } // don't inject it if we haven't configured this datasource
            nativeJdbcExtractor = ref(nativeJdbcExtractor)
        }*/

        /*** custom datasource fix ***/
        dataSourceConnectionSourceFactory(BannerDataSourceConnectionSourceFactory)
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
        /*def ssbMapping = ['/ssb/*']
        bannerMepCodeFilter(FilterRegistrationBean) {
            filter = bean(BannerMepCodeFilter)
            urlPatterns = ssbMapping
            order = Ordered.LOWEST_PRECEDENCE + 111
        }*/

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

        auditTrailPropertySupportHibernateListener(AuditTrailPropertySupportHibernateListener)

        hibernateEventListeners(HibernateEventListeners) {
            listenerMap = ['pre-insert': auditTrailPropertySupportHibernateListener,
                           'pre-update': auditTrailPropertySupportHibernateListener]
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
        def conf = SpringSecurityUtils.securityConfig
        // build providers list here to give dependent plugins a chance to register some
        def providerNames = []
        if (conf.providerNames) {
            providerNames.addAll conf.providerNames
        }
        else {
            if (isSsbEnabled()) providerNames = ['selfServiceBannerAuthenticationProvider', 'bannerAuthenticationProvider']
            else providerNames = ['bannerAuthenticationProvider']
        }
        applicationContext.authenticationManager.providers = createBeanList(providerNames, applicationContext)

        // Define the spring security filters
        def authenticationProvider = Holders.config.banner.sso.authenticationProvider
        List<Map<String, ?>> filterChains = []
        switch (authenticationProvider) {
            case 'external':
                filterChains << [pattern: '/**/api/**',   filters: 'statelessSecurityContextPersistenceFilter,bannerMepCodeFilter,authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor']
                filterChains << [pattern: '/**/qapi/**',  filters: 'statelessSecurityContextPersistenceFilter,bannerMepCodeFilter,authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor']
                filterChains << [pattern: '/**',          filters: 'securityContextPersistenceFilter,logoutFilter,bannerMepCodeFilter, bannerPreAuthenticatedFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor']
                break
            case 'saml':
                break
            default:
                filterChains << [pattern: '/**/api/**',   filters: 'statelessSecurityContextPersistenceFilter,bannerMepCodeFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor']
                filterChains << [pattern: '/**/qapi/**',  filters: 'statelessSecurityContextPersistenceFilter,bannerMepCodeFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor']
                filterChains << [pattern: '/**',          filters: 'securityContextPersistenceFilter,logoutFilter,bannerMepCodeFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor']
                break
        }

        List<GrailsSecurityFilterChain> chains = new ArrayList<GrailsSecurityFilterChain>()
        for (Map<String, ?> entry in filterChains) {
            String value = (entry.filters ?: '').toString().trim()
            List<Filter> filters = value.toString().split(',').collect { String name -> applicationContext.getBean(name, Filter) }
            chains << new GrailsSecurityFilterChain(entry.pattern as String, filters)
        }
       applicationContext.springSecurityFilterChain.filterChains = chains
        //set the transaction timeout on transaction manager time unit in seconds
        def transTimeOut = CH.config.banner?.transactionTimeout instanceof Integer ? CH.config.banner?.transactionTimeout : 30
        applicationContext.getBean('transactionManager')?.setDefaultTimeout(transTimeOut)
    }

    void onChange(Map<String, Object> event) {
        // no-op
    }

    void onConfigChange(Map<String, Object> event) {

    }

    void onShutdown(Map<String, Object> event) {
        // no-op
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


    private createBeanList(names, ctx) { names.collect { name -> ctx.getBean(name) } }


    private static setupExternalConfig() {
        def config = CH.config
        def locations = config.grails.config.locations
        String filePathName
        String configText

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
                println "External configuration file: " + filePathName
                configText = new File(filePathName)?.text
            } else {
                if (filePathName) {
                    println "In prod mode using configuration file '$fileName' from the system path"
                    log.info "In prod mode using configuration file '$fileName' from the system path"
                    configText = new File(filePathName)?.text
                } else {
                    filePathName = Thread.currentThread().getContextClassLoader().getResource( "$fileName" )?.getFile()
                    configText   = Thread.currentThread().getContextClassLoader().getResource( "$fileName" ).text
                    println "Using configuration file '$fileName' from the classpath"
                    log.info "Using configuration file '$fileName' from the classpath (e.g., from within the war file)"
                }
            }
            if(filePathName && configText) {
                try {
                    if(filePathName.endsWith('.groovy')){
                        loadExternalGroovyConfig(configText)
                    }
                    else if(filePathName.endsWith('.properties')){
                        loadExternalPropertiesConfig(filePathName)
                    }
                }
                catch (e) {
                    println "NOTICE: Caught exception while loading configuration files (depending on current grails target, this may be ok): ${e.message}"
                }
            } else {
                println "Configuration files not found either in system variable or in classpath."
            }
        }
    }

    private static String getFilePath( filePath ) {
        if (filePath && new File( filePath ).exists()) {
            "${filePath}"
        }
    }

    private static void loadExternalPropertiesConfig(String filePathName) {
        FileInputStream inputStream = new FileInputStream(filePathName)
        Properties prop = new Properties()
        prop.load(inputStream)
        Holders.config.merge(prop)
    }

    private static void loadExternalGroovyConfig(String configText) {
        Map properties = configText ? new ConfigSlurper(Environment.current.name).parse(configText)?.flatten() : [:]
        Holders.config.merge(properties)
    }
}
