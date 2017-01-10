/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.constraints

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.Foo
import org.codehaus.groovy.runtime.InvokerHelper
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests that the ValidPropertyConstraint is working as expected.
 */
class ValidPropertyConstraintIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testDefaultConstraint() {
        def testObject = createFooParent([childByDefault: Foo.findByCode( "AT" )])
        validate( testObject )

        def nonSavedChild = createBadFooChild()
        testObject.childByDefault = nonSavedChild
        assertFalse "The child has an invalid child code and should of not been validated", testObject.validate()

        testObject.childByDefault = null
        validate( testObject )
    }

    @Test
    void testConstraintByCode() {
        def testObject = createFooParent([childByCode: Foo.findByCode("AT")])
        validate( testObject )
        assertTrue "There should of been a childByCode set", testObject.childByCode != null

        def nonSavedChild = createBadFooChild()
        testObject.childByCode = nonSavedChild
        assertFalse "The child has an invalid child code and should of not been validated", testObject.validate()

        testObject.childByCode = null
        validate( testObject )
    }

    @Test
    void testConstraintById() {
        def testObject = createFooParent([childById: Foo.findByCode("AT")])
        validate( testObject )
        assertTrue "There should of been a childById set", testObject.childById != null

        def nonSavedChild = createBadFooChild()
        nonSavedChild.id = -1
        testObject.childById = nonSavedChild
        assertFalse "This is not a valid id and should of been rejected", testObject.validate()

        testObject.childById = null
        validate( testObject )
    }

    private Foo createFoo(params = null) {
        def foo = new Foo([code            : "TT", description: "TT", addressStreetLine1: "TT", addressStreetLine2: "TT", addressStreetLine3: "TT", addressCity: "TT",
                           addressState    : "TT", addressCountry: "TT", addressZipCode: "TT", systemRequiredIndicator: "N", voiceResponseMessageNumber: 1, statisticsCanadianInstitution: "TT",
                           districtDivision: "TT", houseNumber: "TT", addressStreetLine4: "TT", lastModified: new Date()])
        if (params) {
            use(InvokerHelper) {
                foo.setProperties(params)
            }
        }
        return foo
    }

    private Foo createFooParent(params) {
        return createFoo(params)
    }

    private Foo createBadFooChild() {
        def foo = createFoo()
        foo.code = "badcode"
        return foo

    }
}
