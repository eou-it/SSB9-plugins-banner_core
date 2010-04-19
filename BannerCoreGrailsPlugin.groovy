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

import org.codehaus.groovy.grails.orm.hibernate.ConfigurableLocalSessionFactoryBean
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsAccessDeniedHandlerImpl

import com.sungardhe.banner.security.BannerAuthenticationProvider
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
    
    String version = "0.1-SNAPSHOT" // We'll use SNAPSHOT during development, to put a timestamp on the artifact

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.2.0 > *"
    
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
        application.serviceClasses.findAll { service ->
            service.metaClass.theClass.metaClass.properties.find { p ->
                ((p.name == "defaultCrudMethods") && (service.metaClass.theClass.defaultCrudMethods))
            }
        }.each { domainManagedService ->
            String serviceName = GrailsNameUtils.getPropertyName( domainManagedService.metaClass.theClass )
            String domainName = serviceName.substring( 0, serviceName.indexOf( "Service" ) )

            def domainClass = grailsApplication.domainClasses.find {
                it.name.toLowerCase() == domainName.toLowerCase()
            }

            DomainManagementMethodsInjector.injectDataManagement(domainManagedService, domainClass.metaClass.theClass)
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
