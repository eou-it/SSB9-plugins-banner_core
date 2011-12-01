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
package com.sungardhe.banner.security

import com.sungardhe.banner.db.BannerDS as BannerDataSource
import com.sungardhe.banner.exceptions.ApplicationException
import com.sungardhe.banner.service.KeyBlockHolder
import com.sungardhe.banner.service.ServiceBase

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import java.sql.Connection

import groovy.sql.Sql





/**
 * Integration test for the self service Banner authentication provider.  
 **/
class ResetPasswordIntegrationTests extends GroovyTestCase {


    def resetPasswordService;
    def dataSource  // injected by Spring
    def conn        // set in setUp
    def db          // set in setUp

    protected void setUp(){
        conn = dataSource.getSsbConnection()
        db = new Sql( conn )
        dataSetup()
        super.setUp()
    }

    def testQuestionAnswer(){
        def user = "HOSS001"
        def answers = ["dummy", "red", "scott"]
        def newPassword = "000000"
        def questionAnswerMap = resetPasswordService.getQuestionInfoByLoginId(user)
        assertNotNull(questionAnswerMap.get(user+"pidm"))
        assertEquals(questionAnswerMap.get(user)?.size, 3)

        assertTrue(resetPasswordService.isAnswerMatched(answers[0], questionAnswerMap.get(user+"pidm"), 1))
        assertTrue(resetPasswordService.isAnswerMatched(answers[1], questionAnswerMap.get(user+"pidm"), 2))
        assertTrue(resetPasswordService.isAnswerMatched(answers[2], questionAnswerMap.get(user+"pidm"), 3))
    }

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
        println pidm
        resetPasswordService.resetUserPassword(pidm, newPassword )

        db.eachRow("select gobtpac_salt,gobtpac_pin from gobtpac where gobtpac_pidm = ? ", [pidm]) { row ->
        salt = row.gobtpac_salt
        bannerPasswd = row.gobtpac_pin
        }
        println salt
        db.call( "{call gspcrpt.p_saltedhash(?,?,?)}", [
            newPassword,
            salt ,
            Sql.VARCHAR
            ]
            ) {userpasswd -> userPin = userpasswd}

        assertEquals(userPin,bannerPasswd)

    }

    private void dataSetup(){

        def user = "HOSS001"
        db.executeUpdate("""
                    DECLARE
                    LV_ANS_SALT VARCHAR(255);
                    LV_ANS_DESC VARCHAR(255);
                    LV_PIDM_ID  VARCHAR(255);
                    LV_QUESTION_EXISTS_COUNT VARCHAR(255);
                    BEGIN

                    SELECT SPRIDEN_PIDM INTO LV_PIDM_ID FROM spriden WHERE SPRIDEN_ID = ${user};

                    LV_ANS_SALT := gspcrpt.f_get_salt(8) ;
                    gspcrpt.P_SALTEDHASH ('dummy',LV_ANS_SALT ,LV_ANS_DESC );

                    SELECT COUNT(GOBANSR_NUM) INTO LV_QUESTION_EXISTS_COUNT FROM gobansr WHERE GOBANSR_NUM ='1';

                    IF LV_QUESTION_EXISTS_COUNT = 0 THEN
                    INSERT INTO gobansr (GOBANSR_PIDM, GOBANSR_NUM, GOBANSR_GOBQSTN_ID, GOBANSR_ANSR_DESC, GOBANSR_ANSR_SALT, GOBANSR_ACTIVITY_DATE, GOBANSR_USER_ID, GOBANSR_DATA_ORIGIN) VALUES (LV_PIDM_ID, '1', '2', LV_ANS_DESC, LV_ANS_SALT, SYSDATE, 'GRAILS_USER', 'GRAILS_TEST_LOAD');
                    ELSE
                    UPDATE gobansr SET GOBANSR_ANSR_DESC = LV_ANS_DESC, GOBANSR_ANSR_SALT=LV_ANS_SALT WHERE GOBANSR_NUM = '1';
                    END IF;

                    LV_ANS_SALT := gspcrpt.f_get_salt(8) ;
                    gspcrpt.P_SALTEDHASH ('red',LV_ANS_SALT ,LV_ANS_DESC );
                    SELECT COUNT(GOBANSR_NUM) INTO LV_QUESTION_EXISTS_COUNT FROM gobansr WHERE GOBANSR_NUM ='2';

                    IF LV_QUESTION_EXISTS_COUNT = 0 THEN
                    INSERT INTO gobansr (GOBANSR_PIDM, GOBANSR_NUM, GOBANSR_GOBQSTN_ID, GOBANSR_ANSR_DESC, GOBANSR_ANSR_SALT, GOBANSR_ACTIVITY_DATE, GOBANSR_USER_ID, GOBANSR_DATA_ORIGIN) VALUES (LV_PIDM_ID, '2', '3', LV_ANS_DESC, LV_ANS_SALT, SYSDATE, 'GRAILS_USER', 'GRAILS_TEST_LOAD');
                    ELSE
                    UPDATE gobansr SET GOBANSR_ANSR_DESC = LV_ANS_DESC, GOBANSR_ANSR_SALT=LV_ANS_SALT WHERE GOBANSR_NUM = '2';
                    END IF;

                    LV_ANS_SALT := gspcrpt.f_get_salt(8) ;
                    gspcrpt.P_SALTEDHASH ('scott',LV_ANS_SALT ,LV_ANS_DESC );

                    SELECT COUNT(GOBANSR_NUM) INTO LV_QUESTION_EXISTS_COUNT FROM gobansr WHERE GOBANSR_NUM ='2';

                    IF LV_QUESTION_EXISTS_COUNT = 0 THEN
                    INSERT INTO gobansr (GOBANSR_PIDM, GOBANSR_NUM, GOBANSR_GOBQSTN_ID, GOBANSR_ANSR_DESC, GOBANSR_ANSR_SALT, GOBANSR_ACTIVITY_DATE, GOBANSR_USER_ID, GOBANSR_DATA_ORIGIN) VALUES (LV_PIDM_ID, '3', '6', LV_ANS_DESC, LV_ANS_SALT, SYSDATE, 'GRAILS_USER', 'GRAILS_TEST_LOAD');
                    ELSE
                    UPDATE gobansr SET GOBANSR_ANSR_DESC = LV_ANS_DESC, GOBANSR_ANSR_SALT = LV_ANS_SALT WHERE GOBANSR_NUM = '3';
                    END IF;
                    commit;
                    END;
        """)
    }

    
}


