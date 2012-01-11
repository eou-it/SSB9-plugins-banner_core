package com.sungardhe.banner.security

import groovy.sql.Sql
import org.apache.log4j.Logger
import java.sql.SQLException
import org.apache.commons.lang.RandomStringUtils

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

    def getQuestionInfoByLoginId(id) throws SQLException{
        Map questionAnswerMap = new HashMap()
        List questions = new ArrayList()
        if(id == null)
            return false

        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String query = "SELECT GOBANSR_NUM,GOBANSR_PIDM,GOBANSR_QSTN_DESC,GOBQSTN_DESC, GOBANSR_ANSR_SALT FROM gobansr, spriden, gobqstn WHERE SPRIDEN_ID = '${id}' AND SPRIDEN_PIDM = GOBANSR_PIDM AND SPRIDEN_CHANGE_IND IS NULL AND GOBANSR_GOBQSTN_ID = GOBQSTN_ID"
        try{
            sql.eachRow(query){
                String[] question = [it.GOBANSR_NUM, it.GOBQSTN_DESC]
                if(!questionAnswerMap.containsKey(id)){
                    questionAnswerMap.put(id+"pidm", it.GOBANSR_PIDM)
                }
                questions.add(question)

            }
        }
        finally{
            sql.close()
        }
        questionAnswerMap.put(id, questions)
        questionAnswerMap
    }


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

    def getAnswerByQuestionNumberAndPidm(questionNumber, pidm, sql) throws SQLException{
        def answer
        String query = "select GOBANSR_ANSR_DESC from gobansr where GOBANSR_PIDM ='${pidm}' AND GOBANSR_NUM='${questionNumber}'"
        sql.eachRow(query){
              answer = it.GOBANSR_ANSR_DESC
        }
        answer
    }

    def getAnswerSaltByQyestionNumberAndPidm(questionNumber, pidm, sql) throws SQLException{
        def answerSalt
        String query = "select GOBANSR_ANSR_SALT from gobansr where GOBANSR_PIDM ='${pidm}' AND GOBANSR_NUM='${questionNumber}'"
        sql.eachRow(query){
              answerSalt = it.GOBANSR_ANSR_SALT
        }
        answerSalt
    }

    def resetUserPassword(pidm, newPassword) throws SQLException{
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try{
            sql.call ("{call gb_third_party_access.p_update(p_pidm=>'${pidm}', p_pin=>'${newPassword}')}")
            sql.commit()
        }
        finally{
            sql.close()
        }

    }

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

    def isNonPidmUser(userId) throws SQLException{
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String queryGpbprxy = "SELECT GPBPRXY_EMAIL_ADDRESS FROM gpbprxy WHERE UPPER(GPBPRXY_EMAIL_ADDRESS) ='${userId?.toUpperCase()}'"
        String queryGeniden = "SELECT GENIDEN_ID FROM geniden WHERE UPPER(GENIDEN_ID) = '${userId?.toUpperCase()}'"

        if(sql.rows(queryGpbprxy).size() > 0){
            sql.close()
            true
        }
        else if(sql.rows(queryGeniden).size() > 0){
            sql.close()
            true
        }
        else{
            sql.close()
            false
        }

    }

    def generateResetPasswordURL(nonPidmId, baseUrl){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        try{
            String nonPidmIdm = getNonPidmIdm(nonPidmId)
            sql.call("""
                              DECLARE
                              lv_hold_rowid   gb_common.internal_record_id_type;
                              lv_pinhash      gpbprxy.gpbprxy_pin%TYPE;
                              lv_salt         gpbprxy.gpbprxy_salt%TYPE;
                              p_rowid          varchar(255);
                              p_email          varchar(255);
                              p_name           varchar(255);
                              p_recovery_code  varchar(255);
                              p_url             varchar(255);
                              p_idm             varchar(255);
                              p_base_url        varchar(1000);
                              c utl_smtp.connection;
                              PROCEDURE send_header(name VARCHAR2, header VARCHAR2) AS
                                BEGIN
                                  utl_smtp.write_data(c,name ||':'|| header || UTL_TCP.CRLF);
                              END;
                              begin
                              SELECT GENIDEN_GIDM into p_idm FROM geniden WHERE  GENIDEN_ID = '${nonPidmId}';
                              p_base_url := '${((String)baseUrl).substring(7)}';
                              lv_salt := gspcrpt.f_get_salt(26);
                              gspcrpt.p_saltedhash( lv_salt, lv_salt, lv_pinhash);
                              gp_gpbprxy.P_Update (p_proxy_idm          => p_idm,
                                                   p_pin                => lv_pinhash,
                                                   p_salt               => lv_salt,
                                                   p_pin_disabled_ind   => 'R',
                                                   p_pin_exp_date       => SYSDATE - 1,
                                                   p_inv_login_cnt      => 0);

                              gp_gpbeltr.P_Create (
                                 p_syst_code        => 'p_syst_code',
                                 p_ctyp_code        => 'PIN_RESET',
                                 p_ctyp_url         => NULL,
                                 p_ctyp_exp_date    => SYSDATE
                                                      + bwgkprxy.F_GetOption ('ACTION_VALID_DAYS'),
                                 p_ctyp_exe_date    => NULL,
                                 p_transmit_date    => NULL,
                                 p_proxy_idm        => p_idm,
                                 p_proxy_old_data   => NULL,
                                 p_proxy_new_data   => NULL,
                                 p_person_pidm      => NULL,
                                 p_user_id          => NULL,
                                 p_create_date      => SYSDATE,
                                 p_create_user      => NULL,
                                 p_rowid_out        => lv_hold_rowid);

                              -- Update action URL
                              gp_gpbeltr.P_Update (
                                 p_ctyp_url   =>   p_base_url||'?token='||twbkbssf.F_Encode (lv_hold_rowid),
                                 p_rowid      => lv_hold_rowid);

                              gb_common.P_Commit;

                              SELECT GPBPRXY_EMAIL_ADDRESS , GPBPRXY_FIRST_NAME ||' '|| GPBPRXY_LAST_NAME , GPBPRXY_SALT , GPBELTR_CTYP_URL into p_email, p_name, p_recovery_code, p_url FROM gpbeltr, gpbprxy WHERE GPBPRXY_PROXY_IDM = GPBELTR_PROXY_IDM AND gpbeltr.ROWID = ''||lv_hold_rowid||'';
                              c := utl_smtp.open_connection('mailhost.sct.com');
                              utl_smtp.helo(c, 'psoug.org');
                              utl_smtp.mail(c, 'Vijendra.Rao@sungard.com');
                              utl_smtp.rcpt(c, ''||p_email||'');
                              utl_smtp.open_data(c);
                              send_header('From', '"Admin" <'||twbkwbis.F_FetchWTparam ('PROXY_ACCESS_EMAIL_FROM')||'>');
                              send_header('To', '"'||p_name||'" <'||p_email||'>');
                              send_header('Subject', 'Reset Password Request');
                              utl_smtp.write_data(c, UTL_TCP.CRLF || p_url || ' (use code '|| p_recovery_code ||')');
                              utl_smtp.close_data(c);
                              utl_smtp.quit(c);

                        end;
            """)
        }
        catch(SQLException sqle){
             [error:sqle.getMessage()]
        }
        finally{
            sql.close()
        }
    }

    def getNonPidmIdm(nonPidmId){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String query = "SELECT GENIDEN_GIDM FROM geniden WHERE  GENIDEN_ID = '${nonPidmId}'"
        def id = null
        sql.rows(query).each {
            id = it.GENIDEN_GIDM
        }
        sql.close()
        id
    }


    def validateToken(recoveryCode){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String selectQuery = "SELECT GPBELTR_PROXY_IDM, GPBELTR_CTYP_EXP_DATE  FROM gpbeltr WHERE ROWID ='${recoveryCode}'"
        def nonPidmId = null;
        def expDate = null;
        def errorMessage = null;
        def result = [:]
        try{
            sql.rows(selectQuery).each {
                nonPidmId = it.GPBELTR_PROXY_IDM
                expDate = it.GPBELTR_CTYP_EXP_DATE
            }
            if(nonPidmId == null || expDate == null){
                errorMessage = "Invalid Token"
                [error:errorMessage]
            }
            else if(((Date)expDate).before(Calendar.getInstance().getTime())){
                errorMessage = "Sorry! Token Expired"
                [error:errorMessage]
            }
            else{
                sql.close()
                [nonPidmId: nonPidmId]
            }
        }
        catch(SQLException sqle){
            errorMessage = sqle.getMessage()
            sql.close()
            [error: errorMessage]
        }
    }

    def validateRecoveryCode(recoveryCode, nonPidmId){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String selectQuery = "SELECT * FROM gpbprxy WHERE GPBPRXY_PROXY_IDM ='${nonPidmId}' AND GPBPRXY_SALT='${recoveryCode}'"
        def result = [:]
        try{
            if(sql.rows(selectQuery).last().size() > 0){
                result.put("validate", true)
            }
            else{
                result.put("error", "Invalid Recovery Code")
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


}
