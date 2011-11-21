package com.sungardhe.banner.forgotpin

import com.sungardhe.banner.testing.BaseIntegrationTestCase

/**
 * Created by IntelliJ IDEA.
 * User: Vijendra.Rao
 * Date: 21/11/11
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
class ForgotPinServiceIntegrationTest extends BaseIntegrationTestCase{

    def forgotPinService

    protected void setUp(){
         super.setUp()
    }

    void testGetQuestionInfoByLoginId(){

        def loginId = "210009107"

        def questionsMap = forgotPinService.getQuestionInfoByLoginId(loginId)

        assertNotNull(questionsMap.get(loginId+"pidm"))
        assertTrue(questionsMap.get(loginId).size() > 0)
    }

    void testIsAnswerMatched(){
        def pidm = "27"
        def userAnswer = "kichha"
        def questionNumber = "3"

        assertTrue(forgotPinService.isAnswerMatched(userAnswer, pidm, questionNumber))

    }

    void testIsPidmUser(){
        def loginId = "210009107"

        assertTrue(forgotPinService.isPidmUser(loginId))

        loginId = "dummy.user@sungard.com"

        assertFalse(forgotPinService.isPidmUser(loginId))
    }
}
