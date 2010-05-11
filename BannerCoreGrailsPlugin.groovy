/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

import oracle.jdbc.pool.OracleDataSource

import org.apache.commons.dbcp.BasicDataSource
import org.apache.commons.logging.LogFactory

import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import org.codehaus.groovy.grails.orm.hibernate.ConfigurableLocalSessionFactoryBean
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsAccessDeniedHandlerImpl

import com.sungardhe.banner.security.BannerAuthenticationProvider
import com.sungardhe.banner.service.DomainManagementMethodsInjector
import com.sungardhe.banner.db.BannerDS as BannerDataSource

import org.springframework.beans.factory.config.MapFactoryBean
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor as NativeJdbcExtractor
import org.springframework.security.ui.basicauth.BasicProcessingFilterEntryPoint
import org.springframework.security.ui.ExceptionTranslationFilter
import org.springframework.security.util.FilterChainProxy


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
    //String version = "0.1-SNAPSHOT"
    String version = "0.1.3" 

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.0 > *"
    
    // the other plugins this plugin depends on
    def dependsOn = [ acegi: "0.5.2" ]
    
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def author = "SunGard Higher Education"
    def authorEmail = "horizon-support@sungardhe.com"
    def title = "Banner Core Framework Plugin"
    def description = '''\\
This plugin adds Spring Security (aka Acegi) and a custom 
DataSource implementation (BannerDataSource) that together 
provide for authentication and authorization based upon 
Banner Security configuration. In addition, this plugin provides
additional framework support (e.g., injecting CRUD methods into 
services, providing base test classes) to facilitate development of 
Banner web applications.  
'''

    def documentation = "http://sungardhe.com/development/horizon/plugins/banner-core"
    

    def doWithWebDescriptor = { xml ->
        // no-op 
    }


    def doWithSpring = {
        
        underlyingDataSource( BasicDataSource ) {
            maxActive = 5
            maxIdle = 2
            defaultAutoCommit = "false"
            // Note: url, username, password, and driver must be configured in a local configuration file: home-dir/.grails/banner_on_grails-local-config.groovy
        }
        
        sqlExceptionTranslator( org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator, 'Oracle' ) {
            dataSource = dataSource
        }

        nativeJdbcExtractor( NativeJdbcExtractor )

        authenticationDataSource( OracleDataSource )

        dataSource( BannerDataSource ) {
            underlyingDataSource = underlyingDataSource
            nativeJdbcExtractor = nativeJdbcExtractor
        }

        bannerAuthenticationProvider( BannerAuthenticationProvider ) {
            dataSource = dataSource
            authenticationDataSource = authenticationDataSource
        }

        basicAuthenticationEntryPoint( BasicProcessingFilterEntryPoint ) {
            realmName = 'REST API Realm'
        }
        
        basicExceptionTranslationFilter( ExceptionTranslationFilter ) {
          authenticationEntryPoint = ref( 'basicAuthenticationEntryPoint' )
          accessDeniedHandler = ref( 'accessDeniedHandler' )
          portResolver = ref( 'portResolver' )
        }

        springSecurityFilterChain( FilterChainProxy ) {
          filterInvocationDefinitionSource = """
               CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON
               PATTERN_TYPE_APACHE_ANT
               /api/**=authenticationProcessingFilter,basicProcessingFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor
               /**=httpSessionContextIntegrationFilter,logoutFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,rememberMeProcessingFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor
               """
        }        
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
        
        // inject the logger into every class (Grails only injects this into some artifacts)
        application.allClasses.each { c ->
            c.metaClass.getLog = { ->
                LogFactory.getLog( c )
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
