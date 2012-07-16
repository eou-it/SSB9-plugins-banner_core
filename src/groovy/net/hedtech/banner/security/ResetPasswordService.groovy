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

    def getQuestionInfoByLoginId(id) throws SQLException{
        Map questionAnswerMap = new HashMap()
        List questions = new ArrayList()
        if(id == null)
            return false

        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String query = "SELECT GOBANSR_NUM,GOBANSR_PIDM,GOBANSR_QSTN_DESC,GOBQSTN_DESC, GOBANSR_ANSR_SALT FROM gobansr, spriden, gobqstn WHERE SPRIDEN_ID = '${id}' AND SPRIDEN_PIDM = GOBANSR_PIDM AND SPRIDEN_CHANGE_IND IS NULL AND GOBANSR_GOBQSTN_ID = GOBQSTN_ID"
        String prefQuery = "SELECT GUBPPRF_NO_OF_QSTNS FROM gubpprf"
        try{
            sql.eachRow(query){
                String[] question = [it.GOBANSR_NUM, it.GOBQSTN_DESC]
                if(!questionAnswerMap.containsKey(id)){
                    questionAnswerMap.put(id+"pidm", it.GOBANSR_PIDM)
                }
                questions.add(question)

            }
            sql.eachRow(prefQuery) {
                questionAnswerMap.put(id+"qstn_no", it.GUBPPRF_NO_OF_QSTNS)
            }
        }
        finally{
            sql.close()
        }
        questionAnswerMap.put(id, questions)
        questionAnswerMap
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
    def isAnswerMatched(userAnswer, pidm, questionNumber) throws SQLException{
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def answerSalt =  getAnswerSaltByQyestionNumberAndPidm(questionNumber, pidm, sql)
        def answer = getAnswerByQuestionNumberAndPidm(questionNumber, pidm, sql)
        def matchFlag = false
        try{
            sql.call "{call gspcrpt.P_SALTEDHASH(?,?,?)}", [userAnswer, answerSalt, Sql.VARCHAR], { encryptedAnswer ->
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
    def getAnswerByQuestionNumberAndPidm(questionNumber, pidm, sql) throws SQLException{
        def answer
        String query = "select GOBANSR_ANSR_DESC from gobansr where GOBANSR_PIDM ='${pidm}' AND GOBANSR_NUM='${questionNumber}'"
        sql.eachRow(query){
              answer = it.GOBANSR_ANSR_DESC
        }
        answer
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
    def getAnswerSaltByQyestionNumberAndPidm(questionNumber, pidm, sql) throws SQLException{
        def answerSalt
        String query = "select GOBANSR_ANSR_SALT from gobansr where GOBANSR_PIDM ='${pidm}' AND GOBANSR_NUM='${questionNumber}'"
        sql.eachRow(query){
              answerSalt = it.GOBANSR_ANSR_SALT
        }
        answerSalt
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
    def resetUserPassword(pidm, newPassword) throws SQLException{
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
    def isPidmUser(pidm_id) throws SQLException{
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String query = "SELECT SPRIDEN_ID FROM spriden WHERE SPRIDEN_ID='${pidm_id}'"
        if(sql.rows(query).size() > 0){
            sql.close()
            true
        }
        else{
            sql.close()
            false
        }
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
    def isNonPidmUser(userId) throws SQLException{
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String queryGpbprxy = "SELECT GPBPRXY_EMAIL_ADDRESS FROM gpbprxy WHERE UPPER(GPBPRXY_EMAIL_ADDRESS) ='${userId?.toUpperCase()}'"

        if(sql.rows(queryGpbprxy).size() > 0){
            sql.close()
            true
        }
        else{
            sql.close()
            false
        }

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
    def generateResetPasswordURL(nonPidmId, baseUrl){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def successFlag
        def replyFlag
        log.debug ("Calling gokauth.p_reset_guest_passwd")
        try {
        sql.call( "{call gokauth.p_reset_guest_passwd(?,?,?,?)}",
            [ nonPidmId,
              baseUrl,
              Sql.VARCHAR,
              Sql.VARCHAR
            ]
            ) { out_success,out_reply ->
            successFlag = out_success
            replyFlag = out_reply
            log.debug( "generateResetPasswordURL Success:" + successFlag + " Reply" + replyFlag )
        }

        } catch (e) {
           log.error("ERROR: loginAttempt $e")
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
    def getNonPidmIdm(nonPidmId){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String query = "select gpbprxy_proxy_idm from gpbprxy where  gpbprxy_email_address = '${nonPidmId}'"
        def id = null
        sql.rows(query).each {
            id = it.gpbprxy_proxy_idm
        }
        sql.close()
        id
    }

    /**
     *
     * @param recoveryCode
     * @return
     *
     * This method will validate the token in a given reset password url. Return non-pidm idm if token is valid else appropriate error message
     *
     */
    def validateToken(recoveryCode){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String selectQuery = "SELECT GPBPRXY_EMAIL_ADDRESS, GPBELTR_CTYP_EXP_DATE, GPBPRXY_PIN_DISABLED_IND FROM gpbeltr, gpbprxy WHERE GPBPRXY_PROXY_IDM = GPBELTR_PROXY_IDM AND gpbeltr.ROWID ='${recoveryCode}'"
        def nonPidmId = null;
        def expDate = null;
        def disabledInd = null;
        def errorMessage = null;
        def result = [:]
        try{
            sql.rows(selectQuery).each {
                nonPidmId = it.GPBPRXY_EMAIL_ADDRESS
                expDate = it.GPBELTR_CTYP_EXP_DATE
                disabledInd = it.GPBPRXY_PIN_DISABLED_IND
            }
            if(nonPidmId == null || expDate == null || disabledInd == null){
                    errorMessage = "net.hedtech.banner.resetpassword.guest.url.invalid.message"
                [error:errorMessage]
            }
            else if(((Date)expDate).before(Calendar.getInstance().getTime())){
                errorMessage = "net.hedtech.banner.resetpassword.guest.url.expired.message"
                [error:errorMessage]
            }
            else if(disabledInd.toString().toUpperCase() == "N"){
                errorMessage = "net.hedtech.banner.resetpassword.guest.url.expired.message"
                [error:errorMessage]
            }
            else{
                sql.close()
                [nonPidmId: nonPidmId]
            }
        }
        catch(SQLException sqle){
             if(sqle.getErrorCode() == 1410){
                errorMessage = "net.hedtech.banner.resetpassword.guest.url.invalid.message"
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
    def validateRecoveryCode(recoveryCode, nonPidmId){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String selectQuery = "SELECT * FROM gpbprxy WHERE UPPER(GPBPRXY_EMAIL_ADDRESS) ='${nonPidmId.toString().toUpperCase()}' AND GPBPRXY_SALT='${recoveryCode}'"
        def result = [:]
        try{
            if(sql.rows(selectQuery).size() > 0){
                result.put("validate", true)
            }
            else{
                result.put("error", "net.hedtech.banner.resetpassword.reciverycode.invalid.message")
            }
        }
        catch(SQLException sqle){
            result.put("error", sqle.getMessage())
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
    def resetNonPidmPassword (nonPidmId, passwd  ) {
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try {
        sql.call("""
        declare
            p_proxyIDM VARCHAR2(255);
            p_pin1 VARCHAR2(255) :=  ${passwd};
            lv_salt VARCHAR(255);
            lv_pinhash  VARCHAR(255);
            p_email VARCHAR(255) := ${nonPidmId};
        begin
            SELECT gpbprxy_proxy_idm into p_proxyIDM FROM gpbprxy WHERE GPBPRXY_EMAIL_ADDRESS = p_email;
            lv_salt := gspcrpt.F_Get_Salt (LENGTH (p_pin1));
            gspcrpt.P_SaltedHash (p_pin1, lv_salt, lv_pinhash);
            gp_gpbprxy.P_Update (
                p_proxy_idm          => p_proxyIDM,
                p_pin_disabled_ind   => 'N',
                p_pin_exp_date       => SYSDATE + bwgkprxy.F_GetOption ('PIN_LIFETIME_DAYS'),
                p_pin                => lv_pinhash,
                p_inv_login_cnt      => 0,
                p_salt               => lv_salt);
                gb_common.P_Commit;
        end;
           """
        )
        }
        catch(SQLException sqle){
             log.error(sqle.message)
        }
        finally{
            sql.close()
        }
    }

    /**
     *
     * @param id
     * @return
     *
     * This method will check whether the PIDM account is disabled or not. Returns true if account is disabled else false
     */
    def isAccountDisabled(id){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String pidmQuery = "SELECT NVL(GOBTPAC_PIN_DISABLED_IND,'N') DISABLED_IND FROM gobtpac,spriden  WHERE GOBTPAC_PIDM = spriden_pidm and spriden_change_ind is null and spriden_id = '${id}'"
        def disabledInd = "N"
        try{
            sql.eachRow(pidmQuery){
                disabledInd = it.DISABLED_IND
            }
        }
        finally{
            sql.close()
        }
        if(disabledInd.toUpperCase() == "Y")    true
        else    false
    }


    /**
     *
     * @param id
     * @return
     *
     * This method will check whether the PIDM account is disabled or not. Returns true if account is disabled else false
     */
    def isPidmAccountDisabled(id){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String pidmQuery = "SELECT NVL(GOBTPAC_PIN_DISABLED_IND,'N') DISABLED_IND FROM gobtpac   WHERE GOBTPAC_PIDM =  ${id}"
        def disabledInd = "N"
        try{
            sql.eachRow(pidmQuery){
                disabledInd = it.DISABLED_IND
            }
        }
        finally{
            sql.close()
        }
        if(disabledInd.toUpperCase() == "Y")    true
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
    def loginAttempt(pidm) {
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try {
            sql.call("{call gokauth.p_login_attempt(${pidm})}")

        } catch (e) {
           log.error("ERROR: loginAttempt $e")
           throw e
        } finally {
            sql?.close()
        }
    }

    def validatePassword(pidm, password){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def errorMessage = "";
        String prefQuery = "SELECT GUBPPRF_REUSE_DAYS FROM gubpprf"
        def passwordReuseDays = 0;
        try{
            sql.eachRow(prefQuery){row ->
                passwordReuseDays = row.GUBPPRF_REUSE_DAYS
            }
            sql.call( "{call gb_third_party_access_rules.p_validate_pinrules(?,?,?,?)}",
            [ pidm,
              password,
              (passwordReuseDays == 0)? "N" : "Y",
              Sql.VARCHAR
            ]
            ) { error_message ->
                errorMessage = error_message;
            }
        }
        finally{
            sql?.close();
        }
        (errorMessage == null || errorMessage?.toString()?.trim()?.length() == 0) ? [error: false] : [error: true, errorMessage: errorMessage];
    }

    def containsNumber(inputString){
        Pattern.matches("[^0-9]*[0-9]+[^0-9]*", inputString)
    }

    def containsCharacters(inputString){
        Pattern.matches("[^A-Z]*[^a-z]*[A-Za-z]+[^A-Z]*[^a-z]*", inputString)
    }

}
