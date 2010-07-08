/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

import com.sungardhe.banner.db.BannerDS as BannerDataSource
import com.sungardhe.banner.security.BannerAuthenticationProvider
import com.sungardhe.banner.service.DomainManagementMethodsInjector

import grails.util.GrailsUtil

import oracle.jdbc.pool.OracleDataSource

import org.apache.commons.dbcp.BasicDataSource
import org.apache.commons.logging.LogFactory

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor as NativeJdbcExtractor
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.jndi.JndiObjectFactoryBean


/**
 * A Grails Plugin providing cross cutting concerns such as security and database access 
 * for Banner web applications. 
 **/
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
    String version = "0.1-SNAPSHOT"
    //String version = "0.1.11"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.0 > *"
    
    // the other plugins this plugin depends on
    def dependsOn = [ 'springSecurityCore': "0.3.1" ]
    
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
                         |Banner web applications.'''//.stripMargin()  // TODO Enable this once we adopt Groovy 1.7.3

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

        authenticationDataSource( OracleDataSource )

        bannerAuthenticationProvider( BannerAuthenticationProvider ) {
            dataSource = ref( dataSource )
            authenticationDataSource = ref( authenticationDataSource )
        }

        authenticationManager( ProviderManager ) {
            providers = [ bannerAuthenticationProvider ]
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
//          portResolver = ref( 'portResolver' )
        }
         
        anonymousProcessingFilter( AnonymousAuthenticationFilter ) {
            key = 'horizon-anon'
            userAttribute = 'anonymousUser,ROLE_ANONYMOUS'
        }
/*       
        anonymousAuthenticationProvider( AnonymousAuthenticationProvider ) {
            key = 'horizon-anon'
        }
        
        httpSessionContextIntegrationFilter( HttpSessionContextIntegrationFilter ) {
            forceEagerSessionCreation = true
        }

        springSecurityFilterChain( FilterChainProxy ) {
          filterInvocationDefinitionSource = """
               CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON
               PATTERN_TYPE_APACHE_ANT
               /api/**=authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor
               /**=httpSessionContextIntegrationFilter,logoutFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,rememberMeProcessingFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor
               """
        }   
*/     
    }
    

    def doWithDynamicMethods = { ctx ->
        
        // inject CRUD methods into all services that have a this line: static defaultCrudMethods = true
        application.serviceClasses.each { serviceArtefact ->
            
            def needsCRUD = GCU.getStaticPropertyValue( serviceArtefact.clazz, "defaultCrudMethods" )  
            
            if (needsCRUD) {
                String serviceSimpleName = serviceArtefact.clazz.simpleName
                String domainSimpleName = serviceSimpleName.substring( 0, serviceSimpleName.indexOf( "Service" ) )

                def domainArtefact = application.domainClasses.find {
                    it.clazz.simpleName.toLowerCase() == domainSimpleName.toLowerCase()
                }
                DomainManagementMethodsInjector.injectDataManagement( serviceArtefact.clazz, domainArtefact.clazz )
            }
        }

        application.controllerClasses.each { controllerArtefact ->
            if (controllerArtefact.clazz.metaClass.properties.find { it.name.startsWith('hasRestMixin') }) {
                controllerArtefact.registerMapping( 'list' )
                controllerArtefact.registerMapping( 'show' )
                controllerArtefact.registerMapping( 'create' )
                controllerArtefact.registerMapping( 'update' )
                controllerArtefact.registerMapping( 'destroy' )
                println "${controllerArtefact} has been registered with REST API methods mixed-in to the controller"
            }
        }

                
        // inject the logger into every class (Grails only injects this into some artifacts)
        application.allClasses.each { ->
            it.metaClass.getLog = { ->
                LogFactory.getLog( it )
            }
        }
    }
    

    def doWithApplicationContext = { applicationContext ->
        // no-op
    }
    

    def onChange = { event ->
        // no-op
    }
    

    def onConfigChange = { event ->
        // no-op
    }
    
}
