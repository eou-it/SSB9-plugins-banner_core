/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.query.criteria

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
@Rollback
class QueryIntegrationTests extends BaseIntegrationTestCase {

    def query

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        query= new Query("SELECT * FROM employee")
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testOrderByQuery() {
        Query orderByQuery = Query.createQuery("empId")
        query.orderBy(orderByQuery)
    }


    @Test
    void testOrderByQueryWithTwoParameters() {
        Query query1 = new Query("SELECT * FROM testTable")
        Query query2 = new Query("testId")
        Query actualOutput = query.orderBy(query1, query2)
        assertNotNull(actualOutput)
    }


    @Test
    void testOrQuery() {
        Query orQuery = Query.createQuery("SELECT * FROM testTable WHERE testId='12' ")
        Query query2 = new Query("SELECT * FROM testTable WHERE testName='testName'")
        orQuery.or(query2)
    }


    @Test
    void testOrQueryWithTwoParameters() {
        Query query1 = new Query("SELECT * FROM testTable WHERE testId='testId'")
        Query query2 = new Query("SELECT * FROM testTable WHERE testName='testName'")
        Query q = new Query()
        Query actualOutput = q.or(query1, query2)
        assertNotNull(actualOutput)
    }


    @Test
    void testAndQuery() {
        Query andQuery = Query.createQuery("SELECT * FROM testTable WHERE testId='testId' ")
        Query query2 = new Query("SELECT * FROM testTable WHERE testName='testName'")
        andQuery.and(query2)
    }


    @Test
    void testAndQueryWithTwoParameters() {
        Query query1 = new Query("SELECT * FROM testTable WHERE testId='testId'")
        Query query2 = new Query("SELECT * FROM testTable WHERE testName='testName'")
        Query q = new Query()
        Query actualOutput = q.and(query1, query2)
        assertNotNull(actualOutput)
    }


    @Test
    void testHashCode() {
        Query q = new Query()
        int hashcode = q.hashCode()
        assertNotNull(hashcode)
    }

}
