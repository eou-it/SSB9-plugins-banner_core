/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

import net.hedtech.banner.security.FormContext

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory


/**
 * Base class for functional tests.
 */
class BaseFunctionalTestCase extends functionaltestplugin.FunctionalTestCase {

    def sessionFactory      // injected by Spring
    def dataSource          // injected by Spring
    def transactionManager  // injected by Spring

    // IMPORTANT: Either the formContext property or FormContext threadlocal needs to be set -- prior to calling super.setUp() from a subclass.
    def formContext = null  // This may be set within the subclass, or the FormContext may be set directly by the subclass.


    /**
     * Performs a login for the standard 'grails_user' and calls super.setUp().
     * If you need to log in another user or ensure no user is logged in,
     * then you must either NOT call super.setUp from your setUp method
     * or you must not extend from this class (but extend from GroovyTestCase directly).
     **/
    protected void setUp() {
        super.setUp()

        if (formContext) {
            FormContext.set( formContext )
        } else if (!FormContext.get()){
            println "Warning: No FormContext has been set, and functional tests currently cannot set this automatically..."
        }

        // Monkey patch string to add the base 64 codec
        String.metaClass.encodeAsBase64 = {
            org.codehaus.groovy.grails.plugins.codecs.Base64Codec.encode(delegate)
        }

    }


    // -------------------- Login / Logout Helper Methods ---------------------


    public static def authHeader() {
        def username = 'grails_user' // TODO: Get username and password from configuration
        def password = 'u_pick_it'
        def authString = "$username:$password".encodeAsBase64() // note: This codec is added to 'String' within the base class setUp()
        "Basic ${authString}"
    }


    protected def login() {
        login "grails_user", "u_pick_it"
    }


    protected def login( username, password ) {
        post( "/j_spring_security_check" ) {
            j_username = username
            j_password = password
        }
    }


    protected def loginBad() {
       login "grails_user", "bad"
    }


    protected def logout() {
        post "/j_spring_security_logout"
    }


    // ------------------ Page Assertion Helper Methods --------------------


    protected def assertAtLoginPage() {
       assertTitle "Login"
    }


    // ----------------------- XML Helper Methods --------------------------


    // Used to create a schema validator, for testing XML content.
    protected def schemaValidatorFor( xsdUrl ) {
        def factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI )
        def schema = factory.newSchema( new StreamSource( new URL( xsdUrl ).openStream() ) )
        schema.newValidator()
    }

}

