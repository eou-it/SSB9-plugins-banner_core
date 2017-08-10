/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/


import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.security.ResetPasswordService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder
import org.apache.commons.codec.binary.Base64
import java.sql.SQLException


class ResetPasswordControllerIntegrationTests extends BaseIntegrationTestCase {

    ResetPasswordService resetPasswordService
    public static final String PERSON_RESP001 = 'RESP001'
    public static final String PERSON_RESP002 = 'RESP002'
    public static final String PERSON_RESP003 = 'RESP003'
    public static final String PERSON_RESP004 = 'tuessb01@ssb.com'
    public static final String PERSON_RESP005 = 'RESP004'

    def dataSource
    def conn
    Sql db
    ResetPasswordController resetPasswordController


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        resetPasswordController = new ResetPasswordController()
        conn = dataSource.getSsbConnection()
        conn.setAutoCommit(false)
        db = new Sql(conn)
    }


    @After
    public void tearDown() {
        super.tearDown()
        conn.rollback()
        conn.close()
        db.close()
    }


    @Test
    void testWithoutQuestions() {
        resetPasswordController.questans()
        assertEquals(302, resetPasswordController.response.status)
    }


    @Test
    void testWithNoUser() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.questans()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testWithEmptyUser() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", "  ")
        resetPasswordController.questans()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testWithResetPasswordDisabled() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", PERSON_RESP001)
        def oldIsResetSsbPasswordEnabled = Holders?.config.ssbPassword.reset.enabled
        Holders?.config.ssbPassword.reset.enabled = false
        resetPasswordController.questans()
        assertEquals(302, resetPasswordController.response.status)
        Holders?.config.ssbPassword.reset.enabled = oldIsResetSsbPasswordEnabled
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testWithResetPasswordNull() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", PERSON_RESP001)
        def oldIsResetSsbPasswordEnabled = Holders?.config.ssbPassword.reset.enabled
        Holders?.config.ssbPassword.reset.enabled = null
        resetPasswordController.questans()
        assertEquals(302, resetPasswordController.response.status)
        Holders?.config.ssbPassword.reset.enabled = oldIsResetSsbPasswordEnabled
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testWithQuestionInfoMapAndUserName() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", PERSON_RESP001)
        enableUser()
        resetPasswordController.questans()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testWithNoQuestionInfoMapAndUserName() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", PERSON_RESP002)
        resetPasswordController.questans()
        assertEquals(302, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testQuestionsWithDisabledAccount() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", PERSON_RESP003)
        resetPasswordController.questans()
        assertEquals(302, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testQuestionsWithNonPidmUser() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", PERSON_RESP004)
        def oldIsResetSsbPasswordEnabled = Holders?.config.ssbPassword.guest.reset.enabled
        Holders?.config.ssbPassword.guest.reset.enabled = true
        try {
            resetPasswordController.questans()
        } catch (SQLException e) {
            assertTrue(true)
        }
        Holders?.config.ssbPassword.guest.reset.enabled = oldIsResetSsbPasswordEnabled
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testQuestionsWithNonPidmUserWithResetPasswordNull() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", PERSON_RESP004)
        def oldIsResetSsbPasswordEnabled = Holders?.config.ssbPassword.guest.reset.enabled
        Holders?.config.ssbPassword.guest.reset.enabled = null
        resetPasswordController.questans()
        assertEquals(302, resetPasswordController.response.status)
        Holders?.config.ssbPassword.guest.reset.enabled = oldIsResetSsbPasswordEnabled
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testQuestionsWithNonPidmUserWithResetPasswordFalse() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", PERSON_RESP004)
        def oldIsResetSsbPasswordEnabled = Holders?.config.ssbPassword.guest.reset.enabled
        Holders?.config.ssbPassword.guest.reset.enabled = false
        resetPasswordController.questans()
        assertEquals(302, resetPasswordController.response.status)
        Holders?.config.ssbPassword.guest.reset.enabled = oldIsResetSsbPasswordEnabled
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void testQuestionsWithUser() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.request.setParameter("j_username", "RES004")
        resetPasswordController.questans()
        assertEquals(302, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void validateAnswerWithoutRequestPage() {
        resetPasswordController.validateAnswer()
        assertEquals(302, resetPasswordController.response.status)
    }

    @Test
    void validateAnswerWithRequestPage() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        resetPasswordController.validateAnswer()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
    }


    @Test
    void validateAnswerWithWithQuestions() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setParameter("username", PERSON_RESP001)
        Map questionsInfoMap = resetPasswordService.getQuestionInfoByLoginId(PERSON_RESP001)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("questions", questionsInfoMap.get(PERSON_RESP001))
        resetPasswordController.validateAnswer()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void validateAnswerWithWithQuestionsAndEmptyAnswer() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setParameter("username", PERSON_RESP001)
        resetPasswordController.request.setParameter("answer1", " ")
        resetPasswordController.request.setParameter("answer2", " ")
        resetPasswordController.request.setParameter("answer3", " ")
        Map questionsInfoMap = resetPasswordService.getQuestionInfoByLoginId(PERSON_RESP001)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("questions", questionsInfoMap.get(PERSON_RESP001))
        resetPasswordController.validateAnswer()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void validateAnswerWithQuestionsAndAnswer() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setParameter("username", PERSON_RESP001)
        resetPasswordController.request.setParameter("answer1", "red")
        resetPasswordController.request.setParameter("answer2", "scott")
        resetPasswordController.request.setParameter("answer3", "Dog")
        Map questionsInfoMap = resetPasswordService.getQuestionInfoByLoginId(PERSON_RESP001)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("questions", questionsInfoMap.get(PERSON_RESP001))
        resetPasswordController.validateAnswer()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void validateAnswerWithQuestionsAndWrongAnswer() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setParameter("username", PERSON_RESP001)
        resetPasswordController.request.setParameter("answer1", "blue")
        resetPasswordController.request.setParameter("answer2", "tiger")
        resetPasswordController.request.setParameter("answer3", "Cat")
        Map questionsInfoMap = resetPasswordService.getQuestionInfoByLoginId(PERSON_RESP001)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("questions", questionsInfoMap.get(PERSON_RESP001))
        resetPasswordController.validateAnswer()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void validateAnswerWithQuestionsAndWrongAnswerAccountDisabled() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "questans")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900705")
        resetPasswordController.request.setParameter("username", PERSON_RESP005)
        resetPasswordController.request.setParameter("answer1", "blue")
        resetPasswordController.request.setParameter("answer2", "tiger")
        resetPasswordController.request.setParameter("answer3", "Cat")
        Map questionsInfoMap = resetPasswordService.getQuestionInfoByLoginId(PERSON_RESP005)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("questions", questionsInfoMap.get(PERSON_RESP005))
        resetPasswordController.validateAnswer()
        assertEquals(302, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("requestPage")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void testChangePassword() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "changeexpiredpassword")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.changePassword()
        assertEquals(200, resetPasswordController.response.status)
    }

    @Test
    void testRecovery() {
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("token", "QUFBcms0QUFSQUFBY2liQUFB")
        resetPasswordController.recovery()
        assertEquals(302, resetPasswordController.response.status)
    }


    @Test
    void testRecoveryWithoutToken() {
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.recovery()
        assertTrue(true)
    }


   @Test
    void testValidateCodeWithoutRequestPage() {
        resetPasswordController.validateCode()
        assertEquals(302, resetPasswordController.response.status)
    }


    @Test
    void testValidateCode() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "recovery")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("recoverycode", "WNZP2LJZ")
        resetPasswordController.request.setParameter("nonPidmId", "tessreg03@ssb.com")
        resetPasswordController.validateCode()
        assertEquals(200, resetPasswordController.response.status)
    }


    @Test
    void testValidateCodeWithError() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "recovery")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("recoverycode", "TEST")
        resetPasswordController.request.setParameter("nonPidmId", "TEST@ssb.com")
        resetPasswordController.validateCode()
        assertEquals(200, resetPasswordController.response.status)
    }


    @Test
    void testResetPin() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", "111111")
        resetPasswordController.request.setParameter("repassword", "111111")
        resetPasswordController.resetPin()
        assertEquals(302, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void testResetPinNoMatchingPassword() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "resetpin")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", "111111")
        resetPasswordController.request.setParameter("repassword", "222222")
        resetPasswordController.resetPin()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void testResetPinWithEmptyPassword() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "resetpin")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", " ")
        resetPasswordController.request.setParameter("repassword", "222222")
        resetPasswordController.resetPin()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void testResetPinWithEmptyConfirmPassword() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "resetpin")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", "111111")
        resetPasswordController.request.setParameter("repassword", "  ")
        resetPasswordController.resetPin()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void testResetPinWithEmptyConfirmPasswordWithMatch() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "resetpin")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", "111111")
        resetPasswordController.request.setParameter("repassword", "111111")
        resetPasswordController.resetPin()
        assertEquals(302, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void testResetPinWithValidateFalse() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "resetpin")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900700")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", "abcd")
        resetPasswordController.request.setParameter("repassword", "abcd")
        resetPasswordController.resetPin()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void testResetPinWithNonPidm() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "resetpin")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("nonPidmId", "tuessb02@ssb.com")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", "111111")
        resetPasswordController.request.setParameter("repassword", "111111")
        resetPasswordController.resetPin()
        assertEquals(302, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }
    @Test
    void testResetPinWithEmptyPasswordAndConfirmPassword() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "resetpin")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("pidm", "900702")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", "  ")
        resetPasswordController.request.setParameter("repassword", "  ")
        resetPasswordController.resetPin()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("pidm")
    }


    @Test
    void testChangeExpiredPasswordWithoutRequestPage() {
         RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("usersPidm", "900702")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", " 111111")
        resetPasswordController.changeExpiredPassword()
        assertEquals(302, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("usersPidm")
    }


    @Test
    void testChangeExpiredPasswordWithRequestPage() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "changeexpiredpassword")
         RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("usersPidm", "900702")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("password", " 111111")
        resetPasswordController.request.setParameter("repassword", " 222222")
        resetPasswordController.changeExpiredPassword()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("usersPidm")
    }

   @Test
    void testChangeExpiredPasswordWithConfirmPassword() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("requestPage", "changeexpiredpassword")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("usersPidm", "900702")
        resetPasswordController.request.setContextPath("http://abc:8080")
        resetPasswordController.request.setParameter("oldpassword", "   ")
        resetPasswordController.request.setParameter("password", "111111")
        resetPasswordController.request.setParameter("repassword", "111111")
        resetPasswordController.changeExpiredPassword()
        assertEquals(200, resetPasswordController.response.status)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("usersPidm")
    }


    public String getRowIDEncoded(def proxyPIDM) {
        def rowId
        db.eachRow("SELECT GPBELTR.rowid FROM GPBELTR, GPBPRXY WHERE GPBPRXY_PROXY_IDM = GPBELTR_PROXY_IDM AND GPBELTR_PROXY_IDM=?", [proxyPIDM]) { row ->
            rowId = row.ROWID
        }
        return new String(Base64.encodeBase64(rowId.getBytes()))
    }
    public void enableUser() {
            db.executeUpdate("Update GOBTPAC set  GOBTPAC_PIN_DISABLED_IND='N' WHERE GOBTPAC_PIDM='900700' ")
            db.commit()
    }
}

