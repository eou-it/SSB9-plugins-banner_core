/*******************************************************************************
 Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration test for the Reset password service.
 **/
class ResetPasswordServiceIntegrationTests extends BaseIntegrationTestCase {


    def resetPasswordService;
    def dataSource
    def conn
    Sql db
    public static final String PERSON_RESP005 = 'RESP005'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        conn = dataSource.getSsbConnection()
        db = new Sql(conn)
    }


    @After
    public void tearDown() {
        super.tearDown()
        db.close()
        conn.close()
    }


    @Test
    void testContainsNumber() {
        assertTrue(resetPasswordService.containsNumber("123123"))
    }


    @Test
    void testContainsCharacters() {
        assertTrue(resetPasswordService.containsCharacters("TESTHOSH123"))
    }


    @Test
    void testInjection() {
        assertNotNull resetPasswordService
    }


    @Test
    void testGetNoPidmId() {
        assertNotNull resetPasswordService.getNonPidmIdm("sss01@ssb.com")
    }


    @Test
    void testGetQuestionInfoByLoginId() {
        assertEquals [:], resetPasswordService.getQuestionInfoByLoginId(null)
    }

    @Test
    void testResetNonPidmPassword() {
        resetPasswordService.resetNonPidmPassword('sss01@ssb.com', '111111')
    }


    @Test
    void getUserDefinedQuestionsConfigForId() {
        def QuestionInfoMap = resetPasswordService.getUserDefinedQuestionsConfigForId(PERSON_RESP005, db)
        assertTrue QuestionInfoMap.size() > 0
    }

}


