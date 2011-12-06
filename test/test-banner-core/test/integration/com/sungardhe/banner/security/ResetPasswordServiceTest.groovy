package com.sungardhe.banner.security

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql

/**
 * Created by IntelliJ IDEA.
 * User: Vijendra.Rao
 * Date: 25/11/11
 * Time: 6:19 PM
 * To change this template use File | Settings | File Templates.
 */
class ResetPasswordServiceTest extends BaseIntegrationTestCase{

    def resetPasswordService;

    protected void setUp(){
        super.setUp()
        dataSetup()
    }

    def testResetPasswordForPidmUser(){
        def user = "HOSS001"
        def answers = ["dummy", "red", "scott"]
        def newPassword = "000000"
        def questionAnswerMap = resetPasswordService.getQuestionInfoByLoginId(user)
        assertNotNull(questionAnswerMap.get(user+"pidm"))
        assertEquals(questionAnswerMap.get(user)?.size, 3)

        assertTrue(resetPasswordService.isAnswerMatched(answers[0], questionAnswerMap.get(user+"pidm"), 1))
        assertTrue(resetPasswordService.isAnswerMatched(answers[1], questionAnswerMap.get(user+"pidm"), 2))
        assertTrue(resetPasswordService.isAnswerMatched(answers[2], questionAnswerMap.get(user+"pidm"), 3))

        assertTrue(resetPasswordService.resetUserPassword(questionAnswerMap.get(user+"pidm"), newPassword) > 0)



    }

    private void dataSetup(){

        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def user = "HOSS001"
        sql.call("""
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
                    COMMIT;

                    END;
        """)
    }
}
