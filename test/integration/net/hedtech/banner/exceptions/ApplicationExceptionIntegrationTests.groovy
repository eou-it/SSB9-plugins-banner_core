/*******************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.exceptions

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.testing.Bar
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.Foo
import net.hedtech.banner.testing.TermController
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.dao.DataIntegrityViolationException as ConstraintException
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException as OptimisticLockException
import org.springframework.security.core.context.SecurityContextHolder as SCH

import java.sql.SQLException

/**
 * An integration test for a ApplicationException.
 **/
class ApplicationExceptionIntegrationTests extends BaseIntegrationTestCase {

    def zipService
    private validationTagLibInstance

    @Before
	public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        controller = new TermController()
        controller.zipService = zipService
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testWrappedValidationException() {

        def foo = new Foo( newTestFooParams( "TTTTTTTTT" ) )
        foo.description = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"
		try {
		    foo.save( failOnError:true, flush: true )
		    fail( "Invalid foo was successfully saved!" )
		}
		catch (ValidationException e) {

		    def ae = new ApplicationException( Foo, e )
    	    assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString().contains( "code.maxSize" ) // should include the resourceCode
		    assertEquals 'ValidationException', ae.getType()

		    def returnMap = ae.returnMap( message ) // 'message' closure is provided by BaseIntegrationTestCase
		    assertTrue "Return map not as expected but was: ${returnMap.message}",
		                returnMap.message ==~ /.*The Foo cannot be saved, as it contains errors.*/
		    assertFalse returnMap.success

		    // note that validation exceptions, unlike most others, may hold many localized error messages that may be presented to a user
            assertEquals 2L, returnMap.errors?.size()
            assertTrue returnMap.errors instanceof List

            assertFieldErrorContent( returnMap.errors, [ fieldName: "code", modelName: "net.hedtech.banner.testing.Foo",
                                                         exactMessage: "The foo code is too long, it must be no more than 2 characters",
                                                         rejectedValue: "TTTTTTTTT" ] )

            assertFieldErrorContent( returnMap.errors, [ fieldName: "description", modelName: "net.hedtech.banner.testing.Foo",
                                                         partialMessage: "exceeds the maximum size of",
                                                         rejectedValue: "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT" ] )

 		    assertNotNull returnMap.underlyingErrorMessage // this is the underlying exception's message (which is not appropriate to display to a user)
		}
    }


    // To support reporting of errors for many model instances, a MultiModelValidationException.validate() may be invoked with a list of model instances.
    // If any fail validation, the errors are accumulated and returned within a MultiModelValidationException instance. This exception should then be wrapped
    // within an ApplicationException, so it can be handled normally (i.e., have localization applied).
    @Test
    void testWrappedMultiModelValidationException() {

        def foo = new Foo( newTestFooParamsWithAuditTrail() )

        def expectedNull = MultiModelValidationException.validate( [ foo ] ) // foo is valid, so this should return null
        assertNull "Expected null, but got $expectedNull", expectedNull

        def foo1 = new Foo( code: "TTTTTTTTT", description: "TT", lastModified: new Date(), lastModifiedBy: 'horizon', dataOrigin: 'horizon' )
        foo1.id = 9991
        def foo2 = new Foo( code: "TT", description: "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT",
                                    lastModified: new Date(), lastModifiedBy: 'horizon', dataOrigin: 'horizon' )
        foo2.id = 9992

		// note the String passed into validate below has no effect -- no errors are reported for it as it isn't 'validatable'
		def mmve = MultiModelValidationException.validate( [ foo1, foo2, new Bar( code: 'Z9', description: null ), "I'm a string with no validate()!" ] )
		// asserts the modelValidationErrorsMaps list, where each item is a map containing [entitySimpleClassName: theSimpleName, id: theId, errors: theErrors]
        assertEquals 3L, mmve?.modelValidationErrorsMaps?.size()
        assertEquals 1L, mmve?.modelValidationErrorsMaps?.find { it.entitySimpleClassName == 'Foo' && it.id == 9991 }.errors.allErrors.size()

        // assert the 'aggregated' Errors object. Note that this Errors implementation is not complete -- it exposes only the 'allErrors' property
        // that other Spring Errors implementations provide. Spring implementations protect themselves from having errors from different
        // domain objects, so a 'skeleton' implementation is employed simply to provide the same 'allErrors' interface.
        assertEquals 3L, mmve?.errors?.allErrors?.size()

        // The following 'domain-specific' Errors objects are true 'Spring' implementations
		assertEquals 1L, mmve.getErrorsFor( 'Bar' )?.allErrors.size()
		assertEquals 2L, mmve.getErrorsFor( 'Foo' )?.allErrors.size()
		assertEquals 1L, mmve.getErrorsFor( 'Foo', 9992 )?.allErrors.size() // here we grab the errors for a specific model instance

		// now that we've tested the MultiModelValidationException, we'll wrap it in an ApplicationException and test the returnMap as normal
		def ae = new ApplicationException( 'ParentModelClassName', mmve )
	    assertNotNull ae.toString()
	    assertEquals 'MultiModelValidationException', ae.getType()
	    def returnMap = ae.returnMap( controller.localizer )

	    assertFalse returnMap.success
	    assertTrue returnMap.message ==~ /.*Please correct the following errors and try again.*/
	    assertNotNull returnMap.errors  // lumps all validation errors across all model instances into one list
	    assertTrue returnMap.errors ==~ /.*The foo code is too long, it must be no more than 2 characters.*/
	    assertNotNull returnMap.modelValidationErrorsMaps  // simplifies retrieval of errors using entityName and id
	    assertTrue returnMap.underlyingErrorMessage ==~ /.*@@r1:multi.model.validation.errors@@.*/
    }


    @Test
    void testAccummulateSpringErrors() {

        def mmve = new MultiModelValidationException()
        def foo1 = new Foo( code: "TTTTTTTTT",
                            description: "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT",
                            lastModified: new Date(), lastModifiedBy: 'horizon', dataOrigin: 'horizon' )
        foo1.id = 9991
        foo1.validate()
        mmve.addErrors( foo1.errors ) // note: the errors object in a Grails model implements the Spring validation 'Errors' interface

        def foo2 = new Foo( code: "TT",
                            description: "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT",
                            lastModified: new Date(), lastModifiedBy: 'horizon', dataOrigin: 'horizon' )
        foo2.id = 9992
        foo2.validate()
        mmve.addErrors( foo2.errors )

        // Now we'll assert that we have 3 total errors, 2 for foo1 and 1 for foo2
        assertEquals 3L, mmve.getErrors().allErrors?.size()
        assertEquals 3L, mmve.getErrorsFor( 'Foo' )?.allErrors?.size()
        assertEquals 2L, mmve.getErrorsFor( 'Foo', foo1.id )?.allErrors?.size()
        assertEquals 1L, mmve.getErrorsFor( 'Foo', foo2.id )?.allErrors?.size()

        // If we try to add Errors for one of our Foo instances again, we'll get an exception
        shouldFail( IllegalStateException ) {
            mmve.addErrors( foo1.errors )
        }
    }

    @Test
    void testWrappedOptimisticLockException() {

        def foo = new Foo( newTestFooParamsWithAuditTrail() )
        foo.save( failOnError:true, flush: true )

		def sql
        try {
            sql = new Sql( dataSource.getConnection() )
            sql.executeUpdate( "update STVCOLL set STVCOLL_VERSION = 999 where STVCOLL_SURROGATE_ID = ?", [foo.id] )
        }
        finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }

        foo.description = "This better fail"
		try {
            foo.save( failOnError:true, flush: true )
		}
		catch (OptimisticLockException e) {
		    def ae = new ApplicationException( Foo, e )
		    assertNotNull ae.toString()
		    assertEquals 'OptimisticLockException', ae.getType()

		    def returnMap = ae.returnMap( controller.localizer )
		    assertFalse returnMap.success
		    assertTrue returnMap.message ==~ /.*Another user has updated this.*/
		    assertNull returnMap.errors
		    assertTrue returnMap.underlyingErrorMessage ==~ /.*optimistic locking failed.*/ // underlying hibernate error message, not presentable to user...
		}
    }

    @Test
    void testWrappedNotFoundException() {
		def e = new NotFoundException( id: -666666, entityClassName: 'Foo' )
		def ae = new ApplicationException( Foo, e )
	    assertNotNull ae.toString()
		assertEquals 'NotFoundException', ae.getType()
		assertNotNull (e.toString())

		def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success
		assertTrue returnMap.message ==~ /.*not found.*/
	    assertNull returnMap.errors
	    assertTrue returnMap.underlyingErrorMessage ==~ /.*NotFoundException:\[id=-666666, entityClassName=Foo\].*/
    }

	@Test
	void testWrappedAthorizationException() {
		def authorizationEx = new AuthorizationException("Test")
		assertNotNull(authorizationEx)
		authorizationEx = new AuthorizationException("Test", new Object())
		assertNotNull(authorizationEx)
		authorizationEx = new AuthorizationException("Test", new Throwable())
		assertNotNull(authorizationEx)
	}

	@Test
	void testMepCodeNotFoundException() {
		def mepCodeNotFoundException = new MepCodeNotFoundException()
		assertNotNull(mepCodeNotFoundException.getMessage())
		assertNotNull(mepCodeNotFoundException.toString())
		mepCodeNotFoundException.mepCode = "test"
		assertNotNull(mepCodeNotFoundException.getMessage())
		assertNotNull(mepCodeNotFoundException.toString())
		mepCodeNotFoundException.mepCode = mepCodeNotFoundException.MESSAGE_KEY_MEPCODE_NOT_FOUND
		assertNotNull(mepCodeNotFoundException.getMessage())
		assertNotNull(mepCodeNotFoundException.toString())
	}


    // Tests our ability to handle a constraint exception programmatically created, that has no underlying SQLException
    @Test
    void testWrappedConstraintException() {
		def e = new ConstraintException( 'test' )
		def ae = new ApplicationException( Foo, e )
	    assertNotNull ae.toString()
		assertEquals 'ConstraintException', ae.getType()

		ae = new ApplicationException( "Test", e )
		assertNotNull ae.toString()

		def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success
		assertTrue returnMap.message ==~ /.*A constraint violation in the database has prevented saving.*/
	    assertNull returnMap.errors
	    assertNotNull returnMap.underlyingErrorMessage ==~ /.*test.*/
    }


    @Test
    public void testWrappedNoParentIntegrityConstraintException() {
        SQLException e = new SQLException( "ORA-02291: integrity constraint (SATURN.FK1_SV_FOO_INV_DUMMY_CODE) violated - parent key not found ORA-06512: more info...", "0", 2291 )
		def ae = new ApplicationException( Foo, e )
	    assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString() ==~ /.*parent key not found ORA-06512.*/
		assertEquals 'SQLException', ae.getType()

		def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success

		assertEquals "Message not as expected, but was: ${returnMap.message}", "No parent found for this foo", returnMap.message
	    assertNull returnMap.errors
	    assertTrue "Underlying error message not as expected, but was: ${returnMap.underlyingErrorMessage}",
	               returnMap.underlyingErrorMessage ==~ /.*ORA-02291.*integrity constraint \(SATURN\.FK1_SV_FOO_INV_DUMMY_CODE\) violated - parent key not found ORA-06512.*/
    }


    @Test
    public void testWrappedChildExistsIntegrityConstraintException() {
        SQLException e = new SQLException( "ORA-02292: integrity constraint (SATURN.FK1_SV_FOO_INV_DUMMY_CODE) violated - child record found ORA-06512: more info...", "0", 2292 )
		def ae = new ApplicationException( Foo, e )
	    assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString() ==~ /.*child record found ORA-06512.*/

		assertEquals 'SQLException', ae.getType()

		def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success

		assertEquals "Message not as expected, but was: ${returnMap.message}", "A child record exists for this foo", returnMap.message
	    assertNull returnMap.errors
	    assertTrue "Underlying error message not as expected, but was: ${returnMap.underlyingErrorMessage}",
	               returnMap.underlyingErrorMessage ==~ /.*ORA-02292.*integrity constraint \(SATURN\.FK1_SV_FOO_INV_DUMMY_CODE\) violated - child record found ORA-06512.*/
    }


    @Test
    public void testWrappedLocalizedBannerApiException() {
        SQLException e = new SQLException( "::this is the first error::::this is the second error::::this is the third one::", "0", -20100 )
		def ae = new ApplicationException( Foo, e )
	    assertTrue "toString() does not have expected content, but has: ${ae}",
	               ae.toString().contains( "::this is the first error::::this is the second error::::this is the third one::" )
	    assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString().contains( "-20100" )
		assertEquals 'SQLException', ae.getType()

		def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success

		assertTrue "Message not as expected, but was: ${returnMap.message}", returnMap.message.contains( "The following error(s) have occurred" )
	    assertTrue "Expected 3 errors but got ${returnMap?.errors?.size()}", 3 == returnMap?.errors?.size()
	    assertEquals "this is the first error", returnMap.errors[0]
	    assertEquals "this is the second error", returnMap.errors[1]
	    assertEquals "this is the third one", returnMap.errors[2]
	    assertTrue returnMap.underlyingErrorMessage ==~ /.*::this is the first error::::this is the second error::::this is the third one::.*/
    }

    @Test
    public void testOracleVpdPolicySaveException() {
        SCH.context?.authentication?.principal?.mepProcessContext = "BANNER"
        SQLException e = new SQLException( "policy with check option violation", "0", -28115 )
		def ae = new ApplicationException( Foo, e )

        def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success

		assertTrue "Message not as expected, but was: ${returnMap.message}", returnMap.message.contains( "can not be saved with the process context BANNER" )

    }

    @Test
    public void testWrappedResourceCodeEncodedBannerApiException() {
        SQLException e = new SQLException( "@@r1:test.banner.api.exception:groovy:grails@@", "0", -20200 )
		def ae = new ApplicationException( Foo, e )
	    assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString().contains( "@@r1:test.banner.api.exception:groovy:grails@@" )
	    assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString().contains( "-20200" )
		assertEquals 'SQLException', ae.getType()

		def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success

		assertTrue returnMap.message ==~ /.*Oops, it must be time to learn groovy and grails!.*/
	    assertNull returnMap.errors
	    assertTrue returnMap.underlyingErrorMessage ==~ /.*@@r1:test.banner.api.exception:groovy:grails@@.*/
    }

    @Test
    public void testDoublyWrappedResourceCodeEncodedBannerApiException() {
        SQLException e = new SQLException( "@@r1:test.banner.api.exception:groovy:grails@@", "0", -20200 )
		def ae = new ApplicationException( Foo, new ConstraintException( 'Wrapper around SQLException', e )	)
	    assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString().contains( "@@r1:test.banner.api.exception:groovy:grails@@" )
	    assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString().contains( "-20200" )
		assertEquals 'SQLException', ae.getType()

		def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success

		assertTrue returnMap.message ==~ /.*Oops, it must be time to learn groovy and grails!.*/
	    assertNull returnMap.errors
	    assertTrue returnMap.underlyingErrorMessage ==~ /.*@@r1:test\.banner\.api\.exception:groovy:grails@@.*/
    }


    @Test
    public void testWrappedResourceCodeEncodedBannerApiExceptionWithEscapes() {
        def bannerMsg = /@@r1:test2.banner.api.exception:Banner:grails:"groovy\\java\\\\javascript": and SQL\:PL\/SQL:@@/
        SQLException e = new SQLException( bannerMsg, "0", -20200 )
		def ae = new ApplicationException( Foo, e )
	    assertTrue ae.toString().contains( bannerMsg )
		assertEquals 'SQLException', ae.getType()

		def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success

//		assertTrue returnMap.message ==~ /.*Banner is built using the grails framework, using "groovy\\java\\\\javascript", and SQL:PL\/SQL!.*/
	    assertNull returnMap.errors

	    assertEquals bannerMsg, returnMap.underlyingErrorMessage

    }

    @Test
    void testWrappedAnyOtherException() {
		def e = new RuntimeException( 'my test runtime exception' )
		def ae = new ApplicationException( Foo, e )
		assertEquals 'RuntimeException', ae.getType()

		def returnMap = ae.returnMap( controller.localizer )
		assertFalse returnMap.success
		assertTrue "returnMap was not as expected, but was: ${returnMap.message}", returnMap.message?.contains( "my test runtime exception" )
	    assertNull returnMap.errors
	    assertTrue returnMap.underlyingErrorMessage ==~ /.*my test runtime exception.*/
    }


	@Test
	public void testBusinessLogicValidationException() {
		def ae = new ApplicationException(this.getClass(), new BusinessLogicValidationException("blank.message",
				["S9034823", "default.home.label"]
		))
		assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString().contains("blank.message")

		def returnMap = ae.returnMap(controller.localizer)

		assertEquals returnMap.headers["X-Status-Reason"], 'Validation failed'
		assertEquals returnMap.message, "Property [S9034823] of class [Home] cannot be blank"
		//assertTrue "List of errors not returned", returnMap.errors instanceof List
		assertTrue returnMap.errors.toString(), returnMap.errors[0].message.contains("Property [S9034823] of class [Home] cannot be blank")
	}


	@Test
	public void testBusinessLogicValidationExceptionWithErrorProperties() {
		List errorProperties = ["#/instructorRoster.instructor.id", "#/term.code"]
		BusinessLogicValidationException businessLogicValidationException = new BusinessLogicValidationException("blank.message", ["S9034823", "default.home.label"], errorProperties)
		def ae = new ApplicationException(this.getClass(), businessLogicValidationException)
		assertTrue "toString() does not have expected content, but has: ${ae}", ae.toString().contains("blank.message")

		def returnMap = ae.returnMap(controller.localizer)

		assertEquals returnMap.headers["X-Status-Reason"], 'Validation failed'
		assertEquals returnMap.message, "Property [S9034823] of class [Home] cannot be blank"
		assertTrue returnMap.errors.toString(), returnMap.errors[0].message.contains("Property [S9034823] of class [Home] cannot be blank")
		assertTrue returnMap.errors[0].errorProperties.containsAll(errorProperties)
	}

	// new ApplicationException(  new BusinessLogicValidationException(“adviseeSearch.person.deceased”, [“HOF00714”, “201410”]  )  )

    private Map newTestFooParams( code = "TT" ) {
        [ code: code, description: "Horizon Test - $code", addressStreetLine1: "TT", addressStreetLine2: "TT", addressStreetLine3: "TT", addressCity: "TT",
          addressState: "TT", addressCountry: "TT", addressZipCode: "TT", systemRequiredIndicator: "N", voiceResponseMessageNumber: 1, statisticsCanadianInstitution: "TT",
          districtDivision: "TT", houseNumber: "TT", addressStreetLine4: "TT" ]
    }


    private Map newTestFooParamsWithAuditTrail( code = "TT" ) {
         newTestFooParams( code ) << [ lastModified: new Date(), lastModifiedBy: 'horizon', dataOrigin: 'horizon' ]
    }

    protected ValidationTagLib getValidationTagLib() {
        if (!validationTagLibInstance) {
            validationTagLibInstance = new ValidationTagLib()
        }
        validationTagLibInstance.setMessageSource(messageSource);
        validationTagLibInstance.codecLookup =codecLookup
        validationTagLibInstance
    }
    protected def message = { attrs ->
        getValidationTagLib().messageImpl( attrs )
    }

}
