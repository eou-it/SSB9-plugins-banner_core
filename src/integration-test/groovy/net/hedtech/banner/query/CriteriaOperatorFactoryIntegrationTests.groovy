/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.query.operators.BetweenOperator
import net.hedtech.banner.query.operators.ContainsOperator
import net.hedtech.banner.query.operators.EqualsIgnoreCaseOperator
import net.hedtech.banner.query.operators.EqualsOperator
import net.hedtech.banner.query.operators.GreaterThanEqualsOperator
import net.hedtech.banner.query.operators.GreaterThanOperator
import net.hedtech.banner.query.operators.IsNotNullOperator
import net.hedtech.banner.query.operators.IsNullOperator
import net.hedtech.banner.query.operators.LessThanEqualsOperator
import net.hedtech.banner.query.operators.LessThanOperator
import net.hedtech.banner.query.operators.NotEqualsOperator
import net.hedtech.banner.query.operators.Operators
import net.hedtech.banner.query.operators.StartsWithOperator
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback

class CriteriaOperatorFactoryIntegrationTests extends BaseIntegrationTestCase {

    def criteriaOperatorFactory

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        criteriaOperatorFactory= new CriteriaOperatorFactory()
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testGetCriteriaOperatorEquals() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.EQUALS)
        assertTrue(key instanceof EqualsOperator)
    }



    @Test
    void testGetCriteriaOperatorNotEquals() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.NOT_EQUALS)
        assertTrue(key instanceof NotEqualsOperator)
    }


    @Test
    void testGetCriteriaOperatorEqualsIgnoreCase() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.EQUALS_IGNORE_CASE)
        assertTrue(key instanceof EqualsIgnoreCaseOperator)
    }


    @Test
    void testGetCriteriaOperatorBetween() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.BETWEEN)
        assertTrue(key instanceof BetweenOperator)
    }


    @Test
    void testGetCriteriaOperatorGreaterThan() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.GREATER_THAN)
        assertTrue(key instanceof GreaterThanOperator)
    }


    @Test
    void testGetCriteriaOperatorGreaterThanEquals() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.GREATER_THAN_EQUALS)
        assertTrue(key instanceof GreaterThanEqualsOperator)
    }


    @Test
    void testGetCriteriaOperatorLessThan() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.LESS_THAN)
        assertTrue(key instanceof LessThanOperator)
    }


    @Test
    void testGetCriteriaOperatorLessThanEquals() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.LESS_THAN_EQUALS)
        assertTrue(key instanceof LessThanEqualsOperator)
    }


    @Test
    void testGetCriteriaOperatorIsNull() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.IS_NULL)
        assertTrue(key instanceof IsNullOperator)
    }


    @Test
    void testGetCriteriaOperatorIsNotNull() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.IS_NOT_NULL)
        assertTrue(key instanceof IsNotNullOperator)
    }


    @Test
    void testGetCriteriaOperatorContains() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.CONTAINS)
        assertTrue(key instanceof ContainsOperator)
    }



    @Test
    void testGetCriteriaOperatorStartsWith() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.STARTS_WITH)
        assertTrue(key instanceof StartsWithOperator)
    }



    @Test
    void testGetCriteriaOperatorDefault() {
        def key = criteriaOperatorFactory.getCriteriaOperator(Operators.SOUNDS_LIKE)
        assertNull(key)
    }


    @Test
    void testOperatorsGroup() {
        def operatorGroups = CriteriaOperatorFactory.operatorGroups
        assertNotNull(operatorGroups)
    }

}
