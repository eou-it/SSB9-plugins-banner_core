/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

import net.hedtech.banner.db.BannerDS as BannerDataSource
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.service.KeyBlockHolder
import net.hedtech.banner.service.ServiceBase
import net.hedtech.banner.testing.BaseIntegrationTestCase
import grails.util.Holders  as CH
import org.junit.Assert
import org.junit.Before
import org.junit.After
import org.junit.Test

import java.sql.Connection

import groovy.sql.Sql





/**
 * Integration test for the self service Banner authentication provider.  
 **/
class ResetPasswordIntegrationTests extends BaseIntegrationTestCase{


  //  def resetPasswordService;
 //   def dataSource  // injected by Spring
   // def conn        // set in setUp
   // def db          // set in setUp

    @Before
    public void setUp(){
      //  conn = dataSource.getSsbConnection()
      //  db = new Sql( conn )
      //  dataSetup()
      //super.setUp()
    }
	
	@After
    public void tearDown() {
        //super.tearDown()
    }

    @Test
    void testDummy() {

    }
/*
    def testQuestionAnswer(){
        def user = "HOSS001"
        def answers = ["dummy", "red", "scott"]
        def questionAnswerMap = resetPasswordService.getQuestionInfoByLoginId(user)
        assertNotNull(questionAnswerMap.get(user+"pidm"))
        assertEquals(questionAnswerMap.get(user)?.size, 3)

        assertTrue(resetPasswordService.isAnswerMatched(answers[0], questionAnswerMap.get(user+"pidm"), 1))
        assertTrue(resetPasswordService.isAnswerMatched(answers[1], questionAnswerMap.get(user+"pidm"), 2))
        assertTrue(resetPasswordService.isAnswerMatched(answers[2], questionAnswerMap.get(user+"pidm"), 3))
    }

    @Test
     def testResetPassword(){
        def user = "HOSS001"
        def newPassword = "000000"
        def pidm
        def salt
        def userPin
        def bannerPasswd
        db.eachRow("select spriden_pidm from spriden where spriden_id = 'HOSS001' AND spriden_change_ind is null ") { row ->
        pidm = row.spriden_pidm
        }

        resetPasswordService.resetUserPassword(pidm, newPassword )

        db.eachRow("select gobtpac_salt,gobtpac_pin from gobtpac where gobtpac_pidm = ? ", [pidm]) { row ->
        salt = row.gobtpac_salt
        bannerPasswd = row.gobtpac_pin
        }

        db.call( "{call gspcrpt.p_saltedhash(?,?,?)}", [
            newPassword,
            salt ,
            Sql.VARCHAR
            ]
            ) {userpasswd -> userPin = userpasswd}

        assertEquals(userPin,bannerPasswd)

    }

    @Test
    def testNonPidmPasswordNotify() {
         def user = "rajesh.kumar@sungardhe.com"
         def gidm = resetPasswordService.getNonPidmIdm(user)
         resetPasswordService.generateResetPasswordURL(user, "http://localhost:808/resetPassword/recovery" )
    }


     @Test
     def testNonPidmPasswordReset() {
         def user = "rajesh.kumar@sungardhe.com"
         def newPassword =  "123456"
         def salt
         def bannerPassword
         def userPassword
         resetPasswordService.resetNonPidmPassword (user, newPassword )

         db.eachRow("select gpbprxy_pin, gpbprxy_salt  from gpbprxy where lower(gpbprxy_email_address)=?", [user.toLowerCase()]){ row ->
             salt = row.gpbprxy_salt
             bannerPassword = row.gpbprxy_pin
         }
         db.call( "{call gspcrpt.p_saltedhash(?,?,?)}", [
            newPassword,
            salt ,
            Sql.VARCHAR
            ]
            ) {pswd -> userPassword = pswd}

         assertEquals(userPassword, bannerPassword)

    }

    @Test
    def testPidmUserAccountDisabled(){
        def user = "HOSS001"
        def pidm
        db.eachRow("SELECT SPRIDEN_PIDM FROM spriden WHERE SPRIDEN_ID = ?", [user]){row ->
            pidm = row.SPRIDEN_PIDM
        }
        for (int i =0; i< 3; i++){
            resetPasswordService.loginAttempt(pidm)
        }
        assertTrue(resetPasswordService.isPidmAccountDisabled(user))
    }
*/


    /*@Test
    void testIsPidmUser() {
        //assertTrue(resetPasswordService.isPidmUser("HOSS001"))
        assertTrue (true)
    }*/
    private void dataSetup(){

        def user = "HOSS001"
        db.executeUpdate("""
                        DECLARE
                        LV_ANS_SALT VARCHAR(255);
                        LV_ANS_DESC VARCHAR(255);
                        LV_PIDM_ID  VARCHAR(255);
                        LV_QUESTION_EXISTS_COUNT VARCHAR(255);
                        BEGIN

                        SELECT SPRIDEN_PIDM INTO LV_PIDM_ID FROM spriden WHERE SPRIDEN_ID = 'HOSS001';

                        LV_ANS_SALT := gspcrpt.f_get_salt(8) ;
                        gspcrpt.P_SALTEDHASH ('dummy',LV_ANS_SALT ,LV_ANS_DESC );

                        SELECT COUNT(GOBANSR_NUM) INTO LV_QUESTION_EXISTS_COUNT FROM gobansr WHERE GOBANSR_NUM ='1' AND GOBANSR_PIDM = LV_PIDM_ID;

                        IF LV_QUESTION_EXISTS_COUNT = 0 THEN
                        INSERT INTO gobansr (GOBANSR_PIDM, GOBANSR_NUM, GOBANSR_GOBQSTN_ID, GOBANSR_ANSR_DESC, GOBANSR_ANSR_SALT, GOBANSR_ACTIVITY_DATE, GOBANSR_USER_ID, GOBANSR_DATA_ORIGIN) VALUES (LV_PIDM_ID, '1', '2', LV_ANS_DESC, LV_ANS_SALT, SYSDATE, 'GRAILS_USER', 'GRAILS_TEST_LOAD');
                        ELSE
                        UPDATE gobansr SET GOBANSR_ANSR_DESC = LV_ANS_DESC, GOBANSR_ANSR_SALT=LV_ANS_SALT WHERE GOBANSR_NUM = '1'  AND GOBANSR_PIDM = LV_PIDM_ID;
                        END IF;

                        LV_ANS_SALT := gspcrpt.f_get_salt(8) ;
                        gspcrpt.P_SALTEDHASH ('red',LV_ANS_SALT ,LV_ANS_DESC );
                        SELECT COUNT(GOBANSR_NUM) INTO LV_QUESTION_EXISTS_COUNT FROM gobansr WHERE GOBANSR_NUM ='2' AND GOBANSR_PIDM = LV_PIDM_ID;

                        IF LV_QUESTION_EXISTS_COUNT = 0 THEN
                        INSERT INTO gobansr (GOBANSR_PIDM, GOBANSR_NUM, GOBANSR_GOBQSTN_ID, GOBANSR_ANSR_DESC, GOBANSR_ANSR_SALT, GOBANSR_ACTIVITY_DATE, GOBANSR_USER_ID, GOBANSR_DATA_ORIGIN) VALUES (LV_PIDM_ID, '2', '3', LV_ANS_DESC, LV_ANS_SALT, SYSDATE, 'GRAILS_USER', 'GRAILS_TEST_LOAD');
                        ELSE
                        UPDATE gobansr SET GOBANSR_ANSR_DESC = LV_ANS_DESC, GOBANSR_ANSR_SALT=LV_ANS_SALT WHERE GOBANSR_NUM = '2' AND GOBANSR_PIDM = LV_PIDM_ID;
                        END IF;

                        LV_ANS_SALT := gspcrpt.f_get_salt(8) ;
                        gspcrpt.P_SALTEDHASH ('scott',LV_ANS_SALT ,LV_ANS_DESC );

                        SELECT COUNT(GOBANSR_NUM) INTO LV_QUESTION_EXISTS_COUNT FROM gobansr WHERE GOBANSR_NUM ='3'  AND GOBANSR_PIDM = LV_PIDM_ID;

                        IF LV_QUESTION_EXISTS_COUNT = 0 THEN
                        INSERT INTO gobansr (GOBANSR_PIDM, GOBANSR_NUM, GOBANSR_GOBQSTN_ID, GOBANSR_ANSR_DESC, GOBANSR_ANSR_SALT, GOBANSR_ACTIVITY_DATE, GOBANSR_USER_ID, GOBANSR_DATA_ORIGIN) VALUES (LV_PIDM_ID, '3', '6', LV_ANS_DESC, LV_ANS_SALT, SYSDATE, 'GRAILS_USER', 'GRAILS_TEST_LOAD');
                        ELSE
                        UPDATE gobansr SET GOBANSR_ANSR_DESC = LV_ANS_DESC, GOBANSR_ANSR_SALT = LV_ANS_SALT WHERE GOBANSR_NUM = '3'  AND GOBANSR_PIDM = LV_PIDM_ID;
                        END IF;
                        COMMIT;

                        END;
        """)

        def guestUserId = "-999898"
        def guestEmailAddress = "vijendra.rao@sungard.com"

        db.executeUpdate("update gpbprxy set gpbprxy_email_address=? where gpbprxy_proxy_idm=?", [guestEmailAddress, guestUserId])
    }

    
}


