/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import com.sungardhe.banner.security.FormContext

import grails.util.GrailsNameUtils

import groovy.sql.Sql

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.springframework.security.Authentication
import org.springframework.security.context.SecurityContextHolder
import org.springframework.security.providers.UsernamePasswordAuthenticationToken as UPAT


/**
 * Base class for integration tests, that sets the FormContext and logs in 'GRAILS_USER' if necessary 
 * (i.e., if a user is not alrady logged in) during setUp() before each test.  
 * If the 'controller' property is initialized within the subclass setUp() prior to calling super.setUp(), this class 
 * will automatically set the FormContext and will wire in the convenience maps (params, flash, renderMap, redirectMap). 
 * If the controller is not set prior to invoking this class' setUp(), then this must be manually performed. 
 * In this case (e.g., the integration test isn't testing a controller), it is required that the 
 * FormContext be set explicitly before calling this class' setUp(). 
 * Lastly, this base class provides additional helper methods.  To ensure the login/logout is 
 * effective, this class manipulates the hibernate session and database connecitons. 
 */
class BaseIntegrationTestCase extends GroovyTestCase {
    
    def transactional = false
    
    def formContext = null            // This may be set within the subclass, prior to calling super.setUp(). If it isn't, 
                                      // it will be looked up automatically.
    
    def bannerAuthenticationProvider  // injected
    def dataSource                    // injected via spring
    def transactionManager            // injected via spring
    def sessionFactory                // injected via spring 
    def nativeJdbcExtractor           // injected via spring
    def messageSource                 // injected via spring
    
    def controller = null             // assigned by subclasses, e.g., within the setUp()
    def flash                         // Use this to look at the flash messages and errors
    def params                        // Use this to set params in each test: MyController.metaClass.getParams = { -> params }
    def renderMap                     // Use this to look at the rendered map: MyController.metaClass.render = { Map map -> renderMap = map }
    def redirectMap                   // Use this to look at the rendered map: MyController.metaClass.redirect = { Map map -> redirectMap = map }


    /**
     * Performs a login for the standard 'grails_user' if necessary, and calls super.setUp(). 
     * If you need to log in another user or ensure no user is logged in, 
     * then you must either NOT call super.setUp from your setUp method 
     * or you must not extend from this class (but extend from GroovyTestCase directly).
     **/
    protected void setUp() {
        super.setUp()
        params = [:]
        renderMap = [:]
        redirectMap = [:]
        flash = [:]
        
        if (formContext) {
            FormContext.set( formContext )
        } else if (controller) {
            // the formContext wasn't set explicitly, but we should be able to set it automatically since we know the controller
            def controllerName = controller?.class.simpleName.replaceAll( /Controller/, '' )            
            Map formControllerMap = getFormControllerMap() // note: getFormControllerMap() circumvents a current grails bug
            def associatedFormsList = formControllerMap[ controllerName?.toLowerCase() ]  
            formContext = associatedFormsList 
            FormContext.set( associatedFormsList )
        } else {
            println "Warning: No FormContext has been set, and it cannot be set automatically without knowing the controller..."
        }
        
        if (controller) {
            controller.class.metaClass.getParams = { -> params }
            controller.class.metaClass.getFlash = { -> flash  }
            controller.class.metaClass.redirect = { Map args -> redirectMap = args  }
            controller.class.metaClass.render = { Map args -> renderMap = args  }
        }
        
        loginIfNecessary() 
        
        sessionFactory.currentSession.connection().rollback()                 // needed to protect from other tests
        sessionFactory.currentSession.clear()                                 // needed to protect from other tests
        sessionFactory.currentSession.disconnect()                            // needed to release the old database connection
        sessionFactory.currentSession.reconnect( dataSource.getConnection() ) // get a new connection that has unlocked the needed roles
        transactionManager.getTransaction().setRollbackOnly()                 // and make sure we don't commit to the database
    }


    /**
     * Clears the hibernate session, but does not logout the user. If your test 
     * needs to logout the user, it should do so by explicitly calling logout().
     **/
    protected void tearDown() {
         super.tearDown()
         FormContext.clear()

         sessionFactory.currentSession.connection().rollback()
         sessionFactory.currentSession.close()
    }
    
    
    protected void loginIfNecessary( userName = "grails_user", password = "u_pick_it" ) {
        if (!SecurityContextHolder.getContext().getAuthentication()) {
            login userName, password
        }
    }
 

    /**
     * Convenience method to login a user. You may pass in a username and password, 
     * or omit and accept the default 'grails_user' and 'u_pick_it'.
     **/
    protected void login( userName = "grails_user", password = "u_pick_it" ) {
        Authentication auth = bannerAuthenticationProvider.authenticate( new UPAT( userName, password ) )
        SecurityContextHolder.getContext().setAuthentication( auth )
    }
    
    
    /**
     * Convenience method to logout a user. This simply clears the authentication
     * object from the Spring security context holder.
     **/
    protected void logout() {
        SecurityContextHolder.getContext().setAuthentication( null )
    }
    

    /**
     * Convenience closure to save a domain object and log the errors if not successful.
     * Usage: save( myEntityInstance )
     **/
    protected Closure save = { domainObj ->
        try {
            assertNotNull domainObj
            domainObj.save( failOnError:true, flush: true )
        } catch (e) {
            e.printStackTrace()
            fail "Could not save $domainObj due to ${e.message}"
        }
    }


    /**
     * Convenience closure to validate a domain object and log the errors if not successful.
     * Usage: validate( myEntityInstance )
     **/
    protected Closure validate = { domainObject, failOnError = true ->
        assertNotNull domainObject
        domainObject.validate()

        if (domainObject.hasErrors() && failOnError ) {
            String message = ""

            domainObject.errors.allErrors.each {
                message += "${getErrorMessage( it )}\n"
            }
            fail( message )
        }
    }


    protected executeUpdateSQL( String updateStatement, id ) {
        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( updateStatement, [ id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        } 
    }
    
    
    protected boolean assertErrorsFor( model, errorName, fieldList ) {
        fieldList.each { field ->
            def fieldError = model.errors.getFieldError( field )
            assertNotNull "Did not find expected '$errorName' error for ${model?.class.simpleName}.$field", 
                          fieldError?.codes.find { it == "${GrailsNameUtils.getPropertyNameRepresentation( model?.class )}.${field}.${errorName}.error" }
        }
    }
    
    
    protected boolean assertNoErrorsFor( model, fieldList ) {
        fieldList.each { 
            assertNull "Found unexpected error for ${model?.class.simpleName}.$it", model.errors.getFieldError( it )
        }
    }


    /**
     * Convience method to return a localized string based off of an error.
     **/
    protected String getErrorMessage( error ) {        
        return messageSource.getMessage( error, Locale.getDefault()  )
    }
    

    /** 
     * Convenience method to assert an expected error is found, and that it's localized message matches the supplied matchString.
     **/
    protected boolean assertLocalizedError( model, errorName, matchString, prop ) {
        assertTrue "Did not find expected '$errorName' property error for ${model?.class.simpleName}.$prop, but got ${model.errors.getFieldError( prop )}", 
                    model.errors.getFieldError( prop ).toString() ==~ /.*nullable.*/
        assertTrue "Did not find expected field error ${getErrorMessage( model.errors.getFieldError( prop ) )}", 
                    getErrorMessage( model.errors.getFieldError( prop ) ) ==~ matchString
    }

   
    /**
     * Convience method to assert that there are no errors upon validation.  This will fail with the
     * localized message for easier debugging
     **/
    protected void assertNoErrorsUponValidation( domainObj ) {
        validate( domainObj )
    }
    

    private getFormControllerMap() {
        if (CH && CH.config) {
            CH?.config?.formControllerMap
        } else {
            // Grails bug GRAILS-4687, and http://n4.nabble.com/Grails-Unit-Integration-Testing-apparent-Random-Failures-td1315936.html#a1315936
            // result in the configuration holder being null when running all, or all integration, tests. The holder is availabel 
            // when running tests individually. To workaround this, we'll use the ConfigSlurper to read the formControllerMap.
            def config = new ConfigSlurper().parse( new File( 'grails-app/conf/Config.groovy' ).toURL() )
            config?.formControllerMap
        }
    }

}