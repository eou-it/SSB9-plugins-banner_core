/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import groovy.sql.Sql
import org.apache.log4j.Logger
import java.sql.SQLException
import java.util.regex.Pattern

/**
 * Created by IntelliJ IDEA.
 * User: Vijendra.Rao
 * Date: 31/10/11
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */
class ResetPasswordService {

    static transactional = true
    private final Logger log = Logger.getLogger(getClass())
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring
    def authenticationDataSource           // injected by Spring
    static final String PIDM = "pidm"
    static final String QSTN_NO = "qstn_no"
    static final String GET_SPRIDEN_QUERY = "SELECT GOBANSR_PIDM FROM gobansr, spriden WHERE UPPER(SPRIDEN_ID) = ? AND SPRIDEN_PIDM = GOBANSR_PIDM AND SPRIDEN_CHANGE_IND IS NULL"
    static final String GET_SYSTEM_DEF_QUESTIONS_QUERY = "SELECT GOBANSR_NUM,GOBANSR_PIDM,GOBANSR_QSTN_DESC,GOBQSTN_DESC, GOBANSR_ANSR_SALT FROM gobansr, spriden, gobqstn WHERE UPPER(SPRIDEN_ID) = ? AND SPRIDEN_PIDM = GOBANSR_PIDM AND SPRIDEN_CHANGE_IND IS NULL AND GOBANSR_GOBQSTN_ID = GOBQSTN_ID"
    static final String GET_USER_DEF_QUESTION_QUERY = "SELECT ga.GOBANSR_NUM,ga.GOBANSR_PIDM,ga.GOBANSR_QSTN_DESC, ga.GOBANSR_ANSR_SALT FROM gobansr ga where ga.GOBANSR_GOBQSTN_ID is NULL and ga.GOBANSR_PIDM = (select DISTINCT sp.SPRIDEN_PIDM  from spriden sp where UPPER(sp.SPRIDEN_ID) = ? AND sp.SPRIDEN_CHANGE_IND IS NULL) order by ga.GOBANSR_NUM"
    static final String GET_NO_QUESTIONS_QUERY = "SELECT GUBPPRF_NO_OF_QSTNS FROM gubpprf"
    static final String IS_ANSWERED_PROCEDURE = "{call gspcrpt.P_SALTEDHASH(?,?,?)}"
    static final String GET_ANSWER_FOR_QUESTION_NO_PIDM_QUERY = "select GOBANSR_ANSR_DESC from gobansr where GOBANSR_PIDM = ? AND GOBANSR_NUM=? "
    static final String GET_ANSWERSALT_FOR_QUESTION_NO_PIDM_QUERY = "select GOBANSR_ANSR_SALT from gobansr where GOBANSR_PIDM =? AND GOBANSR_NUM=?"
    static final String IS_PIDM_USER_QUERY = "SELECT SPRIDEN_ID FROM spriden WHERE UPPER(SPRIDEN_ID)=?"
    static final String IS_NON_PIDM_USER_QUERY = "SELECT GPBPRXY_EMAIL_ADDRESS FROM gpbprxy WHERE UPPER(GPBPRXY_EMAIL_ADDRESS) = ?"
    static final String GENERATE_RESET_PASSWORD_URL_PROCEDURE = "{call gokauth.p_reset_guest_passwd(?,?,?,?)}"
    static final String GET_NON_PIDM_IDM_QUERY = "select gpbprxy_proxy_idm from gpbprxy where  gpbprxy_email_address = ?"
    static final String VALIDATE_TOKEN_QUERY = "SELECT GPBPRXY_EMAIL_ADDRESS, GPBELTR_CTYP_EXP_DATE, GPBPRXY_PIN_DISABLED_IND FROM gpbeltr, gpbprxy WHERE GPBPRXY_PROXY_IDM = GPBELTR_PROXY_IDM AND gpbeltr.ROWID =? "
    static final String URL_INVALID_MESSAGE = "net.hedtech.banner.resetpassword.guest.url.invalid.message"
    static final String URL_EXPIRED_MESSAGE = "net.hedtech.banner.resetpassword.guest.url.expired.message"
    static final String VALIDATE_RECOVER_CODE_QUERY = "SELECT * FROM gpbprxy WHERE UPPER(GPBPRXY_EMAIL_ADDRESS) =? AND GPBPRXY_SALT=? "
    static final String VALIDATE = "validate"
    static final String ERROR = "error"
    static final String RECOVERYCODE_INVALID_MESSAGE ="net.hedtech.banner.resetpassword.reciverycode.invalid.message"
    static final String ACCOUNT_DISABLED_QUERY = "SELECT NVL(GOBTPAC_PIN_DISABLED_IND,'N') DISABLED_IND FROM gobtpac,spriden  WHERE GOBTPAC_PIDM = spriden_pidm and spriden_change_ind is null and spriden_id = ?"
    static final String INDICATOR_N="N"
    static final String INDICATOR_Y="Y"
    static final String PIDM_ACCOUNT_DISABLED_QUERY = "SELECT NVL(GOBTPAC_PIN_DISABLED_IND,'N') DISABLED_IND FROM gobtpac   WHERE GOBTPAC_PIDM =  ?"
    static final String PREFERENCE_REUSE_QUERY = "SELECT GUBPPRF_REUSE_DAYS FROM gubpprf"
    static final String THIRD_PARTY_ACCESS_RULE_PROCEDURE= "{call gb_third_party_access_rules.p_validate_pinrules(?,?,?,?)}"
    static final String NUMBER_PATTERN = "[^0-9]*[0-9]+[^0-9]*"
    static final String CHARACTER_PATTERN = "[^A-Z]*[^a-z]*[A-Za-z]+[^A-Z]*[^a-z]*"
    static final String LOGIN_ATTEMPT_MESSAGE = "ERROR: loginAttempt failed :"
    static final String UPDATE_GUEST_PASSWORD_PROCEDURE = "{call gokauth.p_update_guest_passwd (?,?)}"
    static final String UPDATE_GUEST_PASSWORD_FAILED = "ERROR : Update of Guest password failed :"
    static final String LOGIN_ATTEMPT_PROCEDURE = "{call gokauth.p_login_attempt(?)}"
    static final String LOG_RESET_PASSWORD_URL_MESSAGE_1 = "Calling gokauth.p_reset_guest_passwd procedure"
    static final String LOG_RESET_PASSWORD_URL_MESSAGE_2 = "generateResetPasswordURL Success:"
    static final String LOG_RESET_PASSWORD_URL_MESSAGE_3 = " Reply"
    static final String LOG_RESET_PASSWORD_URL_ERROR_MESSAGE = "ERROR: Generate reset password URL"

    /**
     *
     * @param id
     * @return
     * @throws SQLException
     *
     * This method returns a map of questions related to a given PIDM user id. map contains 3 values.
     * 1) List of question associated
     * 2) Pdim IDM value associated
     * 3) No of questions configured in Preference table
     *
     * throws exception for any db related issues
     *
     */

    public Map getQuestionInfoByLoginId(id) throws SQLException{
        Map questionAnswerMap = new HashMap()
        List questions = new ArrayList()
        if(id == null)
            return questionAnswerMap

        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try{
            sql.eachRow(GET_SPRIDEN_QUERY,[id.toUpperCase()]){
                if(!questionAnswerMap.containsKey(id)){
                    questionAnswerMap.put(id+PIDM, it.GOBANSR_PIDM)
                }
            }
            questionAnswerMap.put(id+QSTN_NO, getNoOfQuestionsConfigured(sql))
            questions.addAll(getSystemDefinedQuestionsConfigForId(id,sql))
            questions.addAll(getUserDefinedQuestionsConfigForId(id,sql))
            questionAnswerMap.put(id, questions)
        }
        finally{
            sql.close()
        }

        return questionAnswerMap
    }

    /**
     *
     * @param id
     * @return questions list of Institution questions used by the user.
     * @throws SQLException
     *
     * This method returns a list of questions configured by the given PIDM user id as security questions.
     * throws exception for any db related issues
     *
     */
    private List getSystemDefinedQuestionsConfigForId(id,sql) throws SQLException{
        List questions = new ArrayList()
        sql.eachRow(GET_SYSTEM_DEF_QUESTIONS_QUERY,[id.toUpperCase()]){
            String[] question = [it.GOBANSR_NUM, it.GOBQSTN_DESC]
            questions.add(question)

        }
        return questions
    }

    /**
     *
     * @param id
     * @return questions list of User defined questions.
     * @throws SQLException
     *
     * This method returns a list of user defined questions configured by the given PIDM user id as security questions.
     * throws exception for any db related issues
     *
     */
    private List getUserDefinedQuestionsConfigForId(id,sql) throws SQLException{
        List questions = new ArrayList()
        sql.eachRow(GET_USER_DEF_QUESTION_QUERY,[id.toUpperCase()]){
            String[] question = [it.GOBANSR_NUM, it.GOBANSR_QSTN_DESC]
            questions.add(question)

        }
        return questions
    }


    /**
     *
     * @return noOfQuestions
     * @throws SQLException
     *
     * This method will return Number of security questions configured in the system
     * Throws exception for any db related issues.
     *
     */
    private int getNoOfQuestionsConfigured(sql) throws SQLException{
        int noOfQuestions
        sql.eachRow(GET_NO_QUESTIONS_QUERY) {
            noOfQuestions = it.GUBPPRF_NO_OF_QSTNS
        }
        return noOfQuestions
    }

    /**
     *
     * @param userAnswer
     * @param pidm
     * @param questionNumber
     * @return
     * @throws SQLException
     *
     * This method will return true if all the security questions for a pidm user match. Else false
     * Throws exception for any db related issues.
     *
     */
    public boolean isAnswerMatched(userAnswer, pidm, questionNumber) throws SQLException{
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def answerSalt =  getAnswerSaltByQyestionNumberAndPidm(questionNumber, pidm, sql)
        def answer = getAnswerByQuestionNumberAndPidm(questionNumber, pidm, sql)
        boolean matchFlag = false
        try{
            sql.call IS_ANSWERED_PROCEDURE, [userAnswer.toLowerCase(), answerSalt, Sql.VARCHAR], { encryptedAnswer ->
                if(encryptedAnswer == answer)
                    matchFlag = true
            }
        }
        finally{
            sql.close()
        }
        matchFlag
    }

    /**
     *
     * @param questionNumber
     * @param pidm
     * @param sql
     * @return
     * @throws SQLException
     *
     * This method will return the answer for a given question for a given pidm user
     * throws exception for any db related issues
     *
     */
    private String getAnswerByQuestionNumberAndPidm(questionNumber, pidm, sql) throws SQLException{
        String answer
        sql.eachRow(GET_ANSWER_FOR_QUESTION_NO_PIDM_QUERY,[pidm,questionNumber]){
            answer = it.GOBANSR_ANSR_DESC
        }
        return answer
    }

    /**
     *
     * @param questionNumber
     * @param pidm
     * @param sql
     * @return
     * @throws SQLException
     *
     * This method will return the salt key for a given question for a given pidm user. And will be used for encrypting answer
     * throws exception for any db related issues
     *
     */
    private String getAnswerSaltByQyestionNumberAndPidm(questionNumber, pidm, sql) throws SQLException{
        String answerSalt
        sql.eachRow(GET_ANSWERSALT_FOR_QUESTION_NO_PIDM_QUERY,[pidm,questionNumber]){
            answerSalt = it.GOBANSR_ANSR_SALT
        }
        return answerSalt
    }

    /**
     *
     * @param pidm
     * @param newPassword
     * @return
     * @throws SQLException
     *
     * This method will reset password for a given pidm user with the new password
     * throws exception for any db related issues
     *
     */
    public void resetUserPassword(pidm, newPassword) throws SQLException{
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try{
            sql.call ("{call gb_third_party_access.p_update(p_pidm=>${pidm}, p_pin=>${newPassword})}")
            sql.commit()
        }
        finally{
            sql.close()
        }

    }

    /**
     *
     * @param pidm_id
     * @return
     * @throws SQLException
     *
     * This method return true if the given username is pidm user else false
     * throws exception for any db related issues
     *
     */
    public boolean isPidmUser(pidm_id) throws SQLException{
        boolean isPidmUser = false
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try{
            if(sql.rows(IS_PIDM_USER_QUERY,[pidm_id.toUpperCase()]).size() > 0){
                isPidmUser = true
            }
        }
        finally{
            sql.close()
        }
        return isPidmUser
    }

    /**
     *
     * @param userId
     * @return
     * @throws SQLException
     *
     * This method return true if the given username is non-pidm user else false
     * throws exception for any db related issues.
     *
     */
    public boolean isNonPidmUser(userId) throws SQLException{
        boolean  isNonPidmUser = false
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try{
            if(sql.rows(IS_NON_PIDM_USER_QUERY,[userId.toUpperCase()]).size() > 0){
                isNonPidmUser = true
            }
        } finally {
            sql.close()
        }
        return isNonPidmUser
    }

    /**
     *
     * @param nonPidmId
     * @param baseUrl
     * @return
     *
     * This method will generate reset password URL for the non-pidm user which is used later for resetting the password
     *
     */
    public def generateResetPasswordURL(nonPidmId, baseUrl){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def successFlag
        def replyFlag
        log.debug (LOG_RESET_PASSWORD_URL_MESSAGE_1)
        try {
            sql.call( GENERATE_RESET_PASSWORD_URL_PROCEDURE,
                    [ nonPidmId,
                      baseUrl,
                      Sql.VARCHAR,
                      Sql.VARCHAR
                    ]
            ) { out_success,out_reply ->
                successFlag = out_success
                replyFlag = out_reply
                log.debug( LOG_RESET_PASSWORD_URL_MESSAGE_2 + successFlag + LOG_RESET_PASSWORD_URL_MESSAGE_3 + replyFlag )
            }

        } catch (e) {
            log.error( LOG_RESET_PASSWORD_URL_ERROR_MESSAGE + e)
            throw e
        } finally {
            sql?.close()
        }
    }

    /**
     *
     * @param nonPidmId
     * @return
     *
     * This method will return the non-pidm IDM for a given guest email address
     *
     */
    public String getNonPidmIdm(nonPidmId){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String id = null
        try {
            sql.rows(GET_NON_PIDM_IDM_QUERY,[nonPidmId]).each {
                id = it.gpbprxy_proxy_idm
            }
        } finally {
            sql.close()
        }
        return id
    }

    /**
     *
     * @param recoveryCode
     * @return
     *
     * This method will validate the token in a given reset password url. Return non-pidm idm if token is valid else appropriate error message
     *
     */
    public Map validateToken(recoveryCode){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def nonPidmId = null;
        def expDate = null;
        def disabledInd = null;
        String errorMessage = null;

        try{
            sql.rows(VALIDATE_TOKEN_QUERY,[recoveryCode]).each {
                nonPidmId = it.GPBPRXY_EMAIL_ADDRESS
                expDate = it.GPBELTR_CTYP_EXP_DATE
                disabledInd = it.GPBPRXY_PIN_DISABLED_IND
            }
            if(nonPidmId == null || expDate == null || disabledInd == null){
                errorMessage = URL_INVALID_MESSAGE
                [error:errorMessage]
            }
            else if(((Date)expDate).before(Calendar.getInstance().getTime())){
                errorMessage = URL_EXPIRED_MESSAGE
                [error:errorMessage]
            }
            else if(disabledInd.toString().toUpperCase() == INDICATOR_N){
                errorMessage = URL_EXPIRED_MESSAGE
                [error:errorMessage]
            }
            else{
                sql.close()
                [nonPidmId: nonPidmId]
            }
        }
        catch(SQLException sqle){
            if(sqle.getErrorCode() == 1410){
                errorMessage = URL_INVALID_MESSAGE
            }
            else{
                errorMessage = sqle.getMessage()
            }
            sql.close()
            [error: errorMessage]
        }
    }

    /**
     *
     * @param recoveryCode
     * @param nonPidmId
     * @return
     *
     * This method will validate the recovery code entered by the user before resetting the password
     *
     */
    public Map validateRecoveryCode(recoveryCode, nonPidmId){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def result = [:]
        try{
            if(sql.rows(VALIDATE_RECOVER_CODE_QUERY,[nonPidmId.toString().toUpperCase(),recoveryCode]).size() > 0){
                result.put(VALIDATE, true)
            }
            else{
                result.put(ERROR, RECOVERYCODE_INVALID_MESSAGE)
            }
        }
        catch(SQLException sqle){
            result.put(ERROR, sqle.getMessage())
        }
        finally {
            sql.close()
        }
        result
    }

    /**
     *
     * @param nonPidmId
     * @param passwd
     * @return
     *
     * This method will reset the password for non-pidm (guest) user with the given new password.
     *
     */
    public void resetNonPidmPassword (nonPidmId, passwd  ) {
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try {
            sql.call(UPDATE_GUEST_PASSWORD_PROCEDURE,[nonPidmId,passwd])
            sql.commit()

        } catch (e) {
            log.error(UPDATE_GUEST_PASSWORD_FAILED + e)
            throw e
        } finally {
            sql?.close()
        }
    }

    /**
     *
     * @param id
     * @return
     *
     * This method will check whether the PIDM account is disabled or not. Returns true if account is disabled else false
     */
    public boolean isAccountDisabled(id){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def disabledInd = INDICATOR_N
        try{
            sql.eachRow(ACCOUNT_DISABLED_QUERY,[id]){
                disabledInd = it.DISABLED_IND
            }
        }
        finally{
            sql.close()
        }
        if(disabledInd.toUpperCase() == INDICATOR_Y)    true
        else    false
    }


    /**
     *
     * @param id
     * @return
     *
     * This method will check whether the PIDM account is disabled or not. Returns true if account is disabled else false
     */
    public boolean isPidmAccountDisabled(id){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def disabledInd = INDICATOR_N
        try{
            sql.eachRow(PIDM_ACCOUNT_DISABLED_QUERY,[id]){
                disabledInd = it.DISABLED_IND
            }
        }
        finally{
            sql.close()
        }
        if(disabledInd.toUpperCase() == INDICATOR_Y)    true
        else    false
    }
    /**
     *
     * @param pidm
     * @return
     *
     * This method will log an login attempt and throws error if no of attempts are exceeded.
     *
     */
    public void loginAttempt(pidm) {
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try {
            sql.call(LOGIN_ATTEMPT_PROCEDURE,[pidm])

        } catch (e) {
            log.error(LOGIN_ATTEMPT_MESSAGE + e)
            throw e
        } finally {
            sql?.close()
        }
    }

    public Map validatePassword(pidm, password){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def errorMessage = "";
        def passwordReuseDays = 0;
        if (pidm) {
            try{
                sql.eachRow(PREFERENCE_REUSE_QUERY){row ->
                    passwordReuseDays = row.GUBPPRF_REUSE_DAYS
                }
                sql.call( THIRD_PARTY_ACCESS_RULE_PROCEDURE,
                        [ pidm,
                          password,
                          (passwordReuseDays == 0)? INDICATOR_N : INDICATOR_Y,
                          Sql.VARCHAR
                        ]
                ) { error_message ->
                    errorMessage = error_message;
                }
            }
            finally{
                sql?.close();
            }
        }
        (errorMessage == null || errorMessage?.toString()?.trim()?.length() == 0) ? [error: false] : [error: true, errorMessage: errorMessage];
    }

    def containsNumber(inputString){
        Pattern.matches(NUMBER_PATTERN, inputString)
    }

    def containsCharacters(inputString){
        Pattern.matches(CHARACTER_PATTERN, inputString)
    }

}
