/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package com.sungardhe.banner.constraints

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import com.sungardhe.banner.testing.Foo

/**
 * Tests that the ValidPropertyConstraint is working as expected.
 */
class ValidPropertyConstraintIntegrationTests extends BaseIntegrationTestCase {

    protected void setUp() {
        formContext = ['SSAPREQ', 'STVLEVL']
        super.setUp()
    }

    void testDefaultConstraint() {
        def testObject = createFooParent([childByDefault: Foo.findByCode( "AT" )])
        validate( testObject )

        def nonSavedChild = createBadFooChild()
        testObject.childByDefault = nonSavedChild
        assertFalse "The child has an invalid child code and should of not been validated", testObject.validate()

        testObject.childByDefault = null
        validate( testObject )
    }

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
        def foo = new Foo([code: "TT", description: "TT", addressStreetLine1: "TT", addressStreetLine2: "TT", addressStreetLine3: "TT", addressCity: "TT",
                addressState: "TT", addressCountry: "TT", addressZipCode: "TT", systemRequiredIndicator: "N", voiceResponseMessageNumber: 1, statisticsCanadianInstitution: "TT",
                districtDivision: "TT", houseNumber: "TT", addressStreetLine4: "TT", lastModified: new Date()])
        foo.properties = params
        return foo
    }

    private Foo createFooParent(params) {
        return createFoo(params)
    }

    private Foo createBadFooChild(params) {
        def foo = createFoo(params)
        foo.code = "badcode"
        return foo

    }
}
