package com.sungardhe.banner.forgotpin

import groovy.sql.Sql
import org.apache.log4j.Logger
import org.hibernate.Query
import oracle.jdbc.OracleConnection

/**
 * Created by IntelliJ IDEA.
 * User: Vijendra.Rao
 * Date: 31/10/11
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */
class ForgotPinService {

    static transactional = true
    private final Logger log = Logger.getLogger(getClass())
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring
    def authenticationDataSource           // injected by Spring

    def getQuestionInfoByLoginId(id){
        Map questionAnswerMap = new HashMap()
        List questions = new ArrayList();

        if(id == null)
            return false

        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        String query = "SELECT GOBANSR_NUM,GOBANSR_PIDM,GOBANSR_QSTN_DESC,GOBQSTN_DESC, GOBANSR_ANSR_SALT FROM gobansr, spriden, gobqstn WHERE SPRIDEN_ID = '${id}' AND SPRIDEN_PIDM = GOBANSR_PIDM AND SPRIDEN_CHANGE_IND IS NULL AND GOBANSR_GOBQSTN_ID = GOBQSTN_ID"
        sql.eachRow(query){
            String[] question = new String[2]
            question = [it.GOBANSR_NUM, it.GOBQSTN_DESC]
//            question[0] = it.GOBANSR_NUM
//            question[1] = it.GOBQSTN_DESC
            if(!questionAnswerMap.containsKey(id)){
                questionAnswerMap.put(id+"pidm", it.GOBANSR_PIDM)
            }
            questions.add(question)

        }
        sql.close()
        questionAnswerMap.put(id, questions)
        questionAnswerMap
    }


    def isAnswerMatched(userAnswer, pidm, questionNumber){
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def answerSalt =  getAnswerSaltByQyestionNumberAndPidm(questionNumber, pidm, sql)
        def answer = getAnswerByQuestionNumberAndPidm(questionNumber, pidm, sql)
        def matchFlag = false
        sql.call "{call gspcrpt.P_SALTEDHASH(?,?,?)}", [userAnswer, answerSalt, Sql.VARCHAR], { encryptedAnswer ->
           if(encryptedAnswer == answer)
                matchFlag = true
        }
        sql.close()
        matchFlag
    }

    def getAnswerByQuestionNumberAndPidm(questionNumber, pidm, sql){
        def answer
        String query = "select GOBANSR_ANSR_DESC from gobansr where GOBANSR_PIDM ='${pidm}' AND GOBANSR_NUM='${questionNumber}'"
        sql.eachRow(query){
              answer = it.GOBANSR_ANSR_DESC
        }
        answer
    }

    def getAnswerSaltByQyestionNumberAndPidm(questionNumber, pidm, sql){
        def answerSalt
        String query = "select GOBANSR_ANSR_SALT from gobansr where GOBANSR_PIDM ='${pidm}' AND GOBANSR_NUM='${questionNumber}'"
        sql.eachRow(query){
              answerSalt = it.GOBANSR_ANSR_SALT
        }
        answerSalt
    }

    def resetUserPassword(pidm, newPassword) {
        Sql sql = new Sql(dataSource.getUnproxiedConnection())
        def rowsUpdated = sql.call ("{call gb_third_party_access.p_update(p_pidm=>'${pidm}', p_pin=>'${newPassword}')}")
        sql.close()
        rowsUpdated
    }
}
