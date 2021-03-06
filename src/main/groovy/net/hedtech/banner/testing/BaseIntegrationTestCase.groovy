/*******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.testing

import grails.util.GrailsNameUtils
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import grails.web.mapping.LinkGenerator
import grails.web.servlet.context.GrailsWebApplicationContext
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.configuration.ConfigurationUtils
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.security.FormContext
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugins.web.taglib.ValidationTagLib
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder

import static org.junit.Assert.*

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
@Slf4j
class BaseIntegrationTestCase extends Assert {

    def transactional = false         // this turns off 'Grails' test framework management of transactions
    def useTransactions = true        // and this enables our own management of transactions, which is what most tests will want
    def exposeTransactionAwareSessionFactory = false

    def formContext = null            // This may be set within the subclass, prior to calling super.setUp(). If it isn't,
    // it will be looked up automatically.

    def selfServiceBannerAuthenticationProvider
    def bannerAuthenticationProvider  // injected
    def dataSource                    // injected via spring
    
	//def transactionManager            // injected via spring
    
	def sessionFactory                // injected via spring
    def nativeJdbcExtractor           // injected via spring
    def messageSource                 // injected via spring
    def codecLookup                  // injected via spring
    private validationTagLibInstance  // assigned lazily - see getValidationTagLib method

    def controller = null             // assigned by subclasses, e.g., within the setUp()
    def flash                         // Use this to look at the flash messages and errors
    def params                        // Use this to set params in each test: MyController.metaClass.getParams = { -> params }
    def renderMap                     // Use this to look at the rendered map: MyController.metaClass.render = { Map map -> renderMap = map }
    def redirectMap                   // Use this to look at the rendered map: MyController.metaClass.redirect = { Map map -> redirectMap = map }

    def username = ""
    def password = ""
	
	def isController = false
	
	@Autowired
    WebApplicationContext webAppCtx

    LinkGenerator grailsLinkGenerator

	/**
     * Performs a login for the standard 'grails_user' if necessary, and calls super.setUp().
     * If you need to log in another user or ensure no user is logged in,
     * then you must either NOT call super.setUp from your setUp method
     * or you must not extend from this class (but extend from GroovyTestCase directly).
     **/
    @Before
    public void setUp() {
        params = [:]
        renderMap = [:]
        redirectMap = [:]
        flash = [:]
        // bannerAuthenticationProvider = new BannerAuthenticationProvider()
        webAppCtx = new GrailsWebApplicationContext()
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
            log.info("Warning: No FormContext has been set, and it cannot be set automatically without knowing the controller...")
        }

        MockHttpServletRequest request = new GrailsMockHttpServletRequest(webAppCtx.servletContext)
        MockHttpServletResponse response = new GrailsMockHttpServletResponse()
        GrailsWebMockUtil.bindMockWebRequest(webAppCtx, request, response)

        if (controller) {
            controller.class.metaClass.getParams = { -> params }
            controller.class.metaClass.getFlash = { -> flash  }
            controller.class.metaClass.redirect = { Map args -> redirectMap = args  }
            controller.class.metaClass.render = { Map args -> renderMap = args  }
            def applicationTagLib = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
            controller.metaClass.message = applicationTagLib.message
            controller.grails_artefact_controller_support_RequestForwarder__linkGenerator = applicationTagLib.linkGenerator
            controller.grails_artefact_controller_support_ResponseRedirector__linkGenerator = applicationTagLib.linkGenerator
        }
        loginIfNecessary(username,password)

		/*
        if (useTransactions) {
            sessionFactory.currentSession.with {
                connection().rollback()                 // needed to protect from other tests
                clear()                                 // needed to protect from other tests
                disconnect()                            // needed to release the old database connection
                reconnect( dataSource.getConnection() ) // get a new connection that has unlocked the needed roles
            }
            transactionManager.getTransaction().setRollbackOnly()                 // and make sure we don't commit to the database
            sessionFactory?.queryCache?.clear()                                     //clear the query cache when ehcache is being used
        }
		*/
    }


    /**
     * Clears the hibernate session, but does not logout the user. If your test
     * needs to logout the user, it should do so by explicitly calling logout().
     **/
    @After
    public void tearDown() {
        FormContext.clear()
        //RequestContextHolder.resetRequestAttributes()
		/*
        if (useTransactions) {
            sessionFactory.currentSession.connection().rollback()
            sessionFactory.currentSession.close()
        }
		*/
    }


    /**
     * Convenience method to login a user if not already logged in. You may pass in a username and password,
     * or omit and accept the default 'grails_user' and 'u_pick_it' for admin and 'HOSWEB002' and '111111' for ssb
     **/
    protected void loginIfNecessary(String username,password) {
       if (!SecurityContextHolder.getContext().getAuthentication()) {
           if(username != null && username.empty || (password != null && password.Empty())){
               username = "grails_user"
               password = "u_pick_it"
           }
           login username, password
       }
    }


    /**
     * Convenience method to login a user. You may pass in a username and password,
     * or omit and accept the default 'grails_user' and 'u_pick_it'.
     **/
    protected void login( userName = "grails_user", password = "u_pick_it" ) {
        Authentication auth = bannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( userName, password ) )
        SecurityContextHolder.getContext().setAuthentication( auth )
    }

    /**
     * Convenience method to login a user. You may pass in a username and password,
     * or omit and accept the default 'HOSWEB002' and '111111'.
     **/
    protected void loginSSB( userName = "HOSWEB002", password = "111111" ) {
        Authentication auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( userName, password ) )
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
            def ae = new ApplicationException( domainObj.class, e )
            fail "Could not save $domainObj due to ${ae}"
        }
    }


    /**
     * Convenience closure to validate a domain object and log the errors if not successful.
     * Usage: validate( myEntityInstance )
     **/
    protected Closure validate = { domainObject, failOnError = true ->
        if (!domainObject) {
           domainObject.validate()

        }
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
//            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
    }


    protected Date removeFractionalSecondsFrom( Date date ) {
        Calendar cal = Calendar.getInstance()
        cal.setTime( date )
        cal.set( Calendar.MILLISECOND, 0 ) // truncate fractional seconds, so that we can compare dates to those retrieved from the database
        new Date( cal.getTime().getTime() )
    }


    /**
     * Asserts that a FieldError exists for the expectedField, and that other FieldError attributes are as expected.
     * The FieldError may be asserted against the following properties.  The expectedField property.
     *
     *    [ fieldName: fieldName, errorName: errorName, modelName: modelName,
     *      partialMessage: partialMessage, exactMessage: exactMessage, rejectedValue: rejectedValue ]
     *
     * @param errors the list of FieldError objects that should contain the expected FieldError properties
     * @param expected a Map of expected field error properties, where only expectedField is required
     **/
    protected void assertFieldErrorContent( List errors, Map expected ) {

        def fieldErrors = errors.findAll { it.field == expected.fieldName }
        assertTrue "Did not find field errors for field '${expected.fieldName}'", fieldErrors instanceof List && fieldErrors.size() > 0

        if (expected.modelName) {
            assertTrue "Field errors do not have expected model name ('${expected.modelName}'), but instead error(s) have content ${fieldErrors*.model}",
                    fieldErrors.every { expected.modelName == it.model }
        }

        if (expected.rejectedValue) {
            assertTrue "Field error not found having rejected value '${expected.rejectedValue}', but instead error(s) have content ${fieldErrors*.rejectedValue}",
                    fieldErrors.any { expected.rejectedValue == it.rejectedValue }
        }

        if (expected.partialMessage) {
            assertTrue "Field error not found having partial message content '${expected.partialMessage}', but instead error(s) have content ${fieldErrors*.message}",
                    fieldErrors.any { it.message?.contains( expected.partialMessage ) }
        }

        if (expected.exactMessage) {
            assertTrue "Field error not found having exact message content '${expected.exactMessage}', but instead error(s) have content ${fieldErrors*.message}",
                    fieldErrors.any { expected.exactMessage == it.message }
        }

    }


    protected void assertErrorsFor( model, errorName, fieldList ) {
        fieldList.each { field ->
            def fieldError = model.errors.getFieldError( field )
            assertNotNull "Did not find expected '$errorName' error for ${model?.class.simpleName}.$field",
                    fieldError?.codes.find { it == "${GrailsNameUtils.getPropertyNameRepresentation( model?.class )}.${field}.${errorName}.error" }
        }
    }


    protected void assertNoErrorsFor( model, fieldList ) {
        fieldList.each {
            assertNull "Found unexpected error for ${model?.class.simpleName}.$it", model.errors.getFieldError( it )
        }
    }


    protected void assertApplicationException( ApplicationException ae, String resourceCodeOrExceptedMessage, String message = null ) {

        if (ae.sqlException) {
            if (!ae.sqlException.toString().contains( resourceCodeOrExceptedMessage )) {
                fail( "This should of returned a SQLException with a message '$resourceCodeOrExceptedMessage'." )
            }
        }
        else if (ae.wrappedException?.message) {

            def messageEvaluator
            if (ae.type == "MultiModelValidationException" ) {
                messageEvaluator = {
                    ae.wrappedException.modelValidationErrorsMaps.collect {
                        it.errors.getAllErrors().collect{ err -> err.codes }
                    }.flatten().toString().contains( resourceCodeOrExceptedMessage )
                }
            }
            else {
                // Default evaluation
                // Typically we would be more explicit, but we have gotten into the habit of doing a regex to evaluate the
                // wrapped exception with the 'resourceCode' varying from including '@@r1:' but excluding potential parameter information that
                // comes on the tail of the ApplicationException.  We are just evaluating that the message contains the code.
                messageEvaluator = {
                    ae.wrappedException.message.contains( resourceCodeOrExceptedMessage )
                }
            }

            if (messageEvaluator()) {
                // this is ok, we found the correct error message
            }
            else {
                if (message == null) {
                    message = "Did not find expected error code $resourceCodeOrExceptedMessage.  Found '${ae.wrappedException}' instead."
                }

                fail( message )
            }
        }
        else {
            throw new Exception( "Unable to assert application exception" )
        }
    }


    protected ValidationTagLib getValidationTagLib() {
        if (!validationTagLibInstance) {
            validationTagLibInstance = new ValidationTagLib()
        }
        validationTagLibInstance
    }


    protected def message = { attrs ->
        getValidationTagLib().messageImpl( attrs )
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
    protected assertLocalizedError( model, errorName, matchString, prop ) {
        assertTrue "Did not find expected '$errorName' property error for ${model?.class?.simpleName}.$prop, but got ${model.errors.getFieldError( prop )}",
                model.errors.getFieldError( prop ).toString() ==~ /.*nullable.*/
        assertTrue "Did not find expected field error ${getErrorMessage( model.errors.getFieldError( prop ) )}",
                getErrorMessage( model.errors.getFieldError( prop ) ) ==~ matchString
    }


    /**
     * Convenience method to assert that there are no errors upon validation.  This will fail with the
     * localized message for easier debugging
     **/
    protected void assertNoErrorsUponValidation( domainObj ) {
        validate( domainObj )
    }

    protected void assertLength(int length, def array) {
        assertEquals(length, array?.size());
    }


    private getFormControllerMap() {
        ConfigurationUtils.getConfiguration()?.formControllerMap
    }
    public void SSBSetUp(username,password){
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
            log.warn("Warning: No FormContext has been set, and it cannot be set automatically without knowing the controller...")
        }

        if (controller) {
            controller.class.metaClass.getParams = { -> params }
            controller.class.metaClass.getFlash = { -> flash  }
            controller.class.metaClass.redirect = { Map args -> redirectMap = args  }
            controller.class.metaClass.render = { Map args -> renderMap = args  }
        }
        loginSSB(username,password)

		/*
        if (useTransactions) {
            sessionFactory.currentSession.with {
                connection().rollback()                 // needed to protect from other tests
                clear()                                 // needed to protect from other tests
                disconnect()                            // needed to release the old database connection
                reconnect( dataSource.getConnection() ) // get a new connection that has unlocked the needed roles
            }
            transactionManager.getTransaction().setRollbackOnly()                 // and make sure we don't commit to the database
            sessionFactory?.queryCache?.clear()
        }
		*/
    }

}
