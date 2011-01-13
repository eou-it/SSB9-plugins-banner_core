/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

import com.sungardhe.banner.controllers.RestfulControllerMixin
import com.sungardhe.banner.db.BannerDS as BannerDataSource
import com.sungardhe.banner.security.BannerAuthenticationProvider
import com.sungardhe.banner.service.ServiceBase
import com.sungardhe.banner.supplemental.SupplementalDataSupportMixin
import com.sungardhe.banner.supplemental.SupplementalDataHibernateListener
import com.sungardhe.banner.supplemental.SupplementalDataService
import com.sungardhe.banner.supplemental.SupplementalDataPersistenceManager

import grails.util.GrailsUtil

import oracle.jdbc.pool.OracleDataSource

import org.apache.commons.dbcp.BasicDataSource
import org.apache.commons.logging.LogFactory

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor as NativeJdbcExtractor
import org.springframework.jndi.JndiObjectFactoryBean
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.transaction.annotation.Transactional
import com.sungardhe.banner.service.AuditTrailPropertySupportHibernateListener
import com.sungardhe.banner.representations.ResourceRepresentationRegistry
import com.sungardhe.banner.security.BannerPreAuthenticatedFilter
import javax.servlet.Filter
import com.sungardhe.banner.security.BannerAccessDecisionVoter

/**
 * A Grails Plugin providing cross cutting concerns such as security and database access
 * for Banner web applications.
 * */
@Transactional()
class BannerCoreGrailsPlugin {

    // Note: the groupId 'should' be used when deploying this plugin via the 'grails maven-deploy --repository=snapshots' command,
    // however it is not being picked up.  Consequently, a pom.xml file is added to the root directory with the correct groupId
    // and will be removed when the maven-publisher plugin correctly sets the groupId based on the following field.
    String groupId = "com.sungardhe"

    // Note: Using '0.1-SNAPSHOT' (to put a timestamp on the artifact) is not used due to GRAILS-5624 see: http://jira.codehaus.org/browse/GRAILS-5624
    // Until this is resolved, Grails application's that use a SNAPSHOT plugin do not check for a newer plugin release, so that the
    // only way we'd be able to upgrade a project would be to clear the .grails and .ivy2 cache to force a fetch from our Nexus server.
    // Consequently, we'll use 'RELEASES' so that each project can explicitly identify the needed plugin version. Using RELEASES provides
    // more control on 'when' a grails app is updated to use a newer plugin version, and therefore 'could' allow delayed testing within those apps
    // independent of deploying a new plugin build to Nexus.
    //
//    String version = "0.1-SNAPSHOT"
    String version = "0.2.9"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.0 > *"

    // the other plugins this plugin depends on
    def dependsOn = ['springSecurityCore': '1.0.1']

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "SunGard Higher Education"
    def authorEmail = "horizon-support@sungardhe.com"
    def title = "Banner Core Framework Plugin"
    def description = '''This plugin adds Spring Security (aka Acegi) and a custom
                         |DataSource implementation (BannerDataSource) that together
                         |provide for authentication and authorization based upon
                         |Banner Security configuration. In addition, this plugin provides
                         |additional framework support (e.g., injecting CRUD methods into
                         |services, providing base test classes) to facilitate development of
                         |Banner web applications.'''.stripMargin() 

    def documentation = "http://sungardhe.com/development/horizon/plugins/banner-core"


    def doWithWebDescriptor = { xml ->
        // no-op
    }


    def doWithSpring = {

        switch (GrailsUtil.environment) {
            case GrailsApplication.ENV_PRODUCTION:
                log.info "Will use a dataSource configured via JNDI"
                underlyingDataSource( JndiObjectFactoryBean ) {
                    jndiName = "java:comp/env/${ConfigurationHolder.config.myDataSource.jndiName}"
                }
                break
            default: // we'll use our locally configured dataSource for development and test environments
                log.info "Using development/test datasource"
                underlyingDataSource( BasicDataSource ) {
                    maxActive = 5
                    maxIdle = 2
                    defaultAutoCommit = "false"
                    driverClassName = "${ConfigurationHolder.config.myDataSource.driver}"
                    url = "${ConfigurationHolder.config.myDataSource.url}"
                    password = "${ConfigurationHolder.config.myDataSource.password}"
                    username = "${ConfigurationHolder.config.myDataSource.username}"
                }
                break
        }

        nativeJdbcExtractor( NativeJdbcExtractor )

        dataSource( BannerDataSource ) {
            underlyingDataSource = ref( underlyingDataSource )
            nativeJdbcExtractor = ref( nativeJdbcExtractor )
        }

        sqlExceptionTranslator( org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator, 'Oracle' ) {
            dataSource = ref( dataSource )
        }

        resourceRepresentationRegistry( ResourceRepresentationRegistry ) { bean ->
            bean.initMethod = 'init'
        }

        supplementalDataPersistenceManager( SupplementalDataPersistenceManager ) {
            dataSource = ref( dataSource )
            sessionFactory = ref( sessionFactory )
            supplementalDataService = ref( supplementalDataService )

        }

        supplementalDataService( SupplementalDataService ) { bean ->
            dataSource = ref( dataSource )
            sessionFactory = ref( sessionFactory )
            supplementalDataPersistenceManager = ref( supplementalDataPersistenceManager )
            bean.initMethod = 'init'
        }

        roleVoter( BannerAccessDecisionVoter )

        authenticationDataSource( OracleDataSource )

        bannerAuthenticationProvider( BannerAuthenticationProvider ) {
            dataSource = ref( dataSource )
            authenticationDataSource = ref( authenticationDataSource )
        }

        bannerPreAuthenticatedFilter( BannerPreAuthenticatedFilter ) {
            dataSource = ref( dataSource )
            authenticationManager = ref( authenticationManager )
        }

        authenticationManager( ProviderManager ) {
            providers = [bannerAuthenticationProvider]
        }

        basicAuthenticationEntryPoint( BasicAuthenticationEntryPoint ) {
            realmName = 'Banner REST API Realm'
        }

        basicAuthenticationFilter( BasicAuthenticationFilter ) {
            authenticationManager = ref( authenticationManager )
            authenticationEntryPoint = ref( basicAuthenticationEntryPoint )
        }

        basicExceptionTranslationFilter( ExceptionTranslationFilter ) {
            authenticationEntryPoint = ref( 'basicAuthenticationEntryPoint' )
            accessDeniedHandler = ref( 'accessDeniedHandler' )
        }

        anonymousProcessingFilter( AnonymousAuthenticationFilter ) {
            key = 'horizon-anon'
            userAttribute = 'anonymousUser,ROLE_ANONYMOUS'
        }
    }


    def doWithDynamicMethods = { ctx ->

        // Deprecated -- the following mixes in the ServiceBase class that provides default CRUD methods,
        // into all services having a 'static boolean defaultCrudMethods = true' property.
        // This approach is deprecated in favor of extending from the ServiceBase base class.
        // Extending from ServiceBase enables declarative Transaction demarcation using annotations.
        // Mixing in the base class requires the 'boolean transactional = true' line, and does not provide
        // the more granular control of transaction attributes possible with annotations.
        //
        application.serviceClasses.each { serviceArtefact ->
            def needsCRUD = GCU.getStaticPropertyValue( serviceArtefact.clazz, "defaultCrudMethods" )
            if (needsCRUD) {
                serviceArtefact.clazz.mixin ServiceBase
            }
        }

        // mix-in and register RESTful actions for any controller having this line:
        //     static List mixInRestActions = [ 'show', 'list', 'create', 'update', 'destroy' ]
        // Note that if any actions are omitted from this line, they will not be accessible (as they won't be registered)
        // even though they will still be mixed-in.
        application.controllerClasses.each { controllerArtefact ->
            def neededRestActions = GCU.getStaticPropertyValue( controllerArtefact.clazz, "mixInRestActions" )
            if (neededRestActions?.size() > 0) {
                for (it in neededRestActions) {
                    controllerArtefact.registerMapping it
                }
                controllerArtefact.clazz.mixin RestfulControllerMixin
            }
        }

        // mix-in supplemental data support into all models
        application.domainClasses.each { modelArtefact ->
            try {
                modelArtefact.clazz.mixin SupplementalDataSupportMixin
            } catch (e) {
                e.printStackTrace()
                throw e
            }
        }

        // inject the logger into every class (Grails only injects this into some artifacts)
        application.allClasses.each {->
            it.metaClass.getLog = {->
                LogFactory.getLog it
            }
        }
    }

    // Register Hibernate event listeners.
    def doWithApplicationContext = { applicationContext ->
        def listeners = applicationContext.sessionFactory.eventListeners

        // register hibernate listener to load supplemental data
        addEventTypeListener( listeners, new SupplementalDataHibernateListener(), 'postLoad' )

        // register hibernate listener for populating audit trail properties before inserting and updating models
        def auditTrailSupportListener = new AuditTrailPropertySupportHibernateListener()
        ['preInsert', 'preUpdate'].each {
            addEventTypeListener( listeners, auditTrailSupportListener, it )
        }

        // Define the spring security filters
        def authenticationProvider = ConfigurationHolder?.config?.banner.sso.authenticationProvider
        LinkedHashMap<String, String> filterChain = new LinkedHashMap();
        switch (authenticationProvider) {
            case 'cas':
                filterChain['/api/**'] = 'authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor'
                filterChain['/**'] = 'securityContextPersistenceFilter,logoutFilter,casAuthenticationFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor'
                break
            case 'external':
                filterChain['/api/**'] = 'authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor'
                filterChain['/**'] = 'securityContextPersistenceFilter,logoutFilter,bannerPreAuthenticatedFilter,authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor'
                break
            default:
                filterChain['/api/**'] = 'authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor'
                filterChain['/**'] = 'securityContextPersistenceFilter,logoutFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor'
                break
        }

        LinkedHashMap<String, List<Filter>> filterChainMap = new LinkedHashMap();
        filterChain.each { key, value ->
            def filters = value.toString().split(',').collect {
                name -> applicationContext.getBean( name )
            }
            filterChainMap[key] = filters
        }
        applicationContext.springSecurityFilterChain.filterChainMap = filterChainMap
    }


    def onChange = { event ->
        // no-op
    }


    def onConfigChange = { event ->
        // no-op
    }


    def addEventTypeListener( listeners, listener, type ) {
        def typeProperty = "${type}EventListeners"
        def typeListeners = listeners."${typeProperty}"

        def expandedTypeListeners = new Object[typeListeners.length + 1]
        System.arraycopy( typeListeners, 0, expandedTypeListeners, 0, typeListeners.length )
        expandedTypeListeners[-1] = listener

        listeners."${typeProperty}" = expandedTypeListeners
    }
}
