package net.hedtech.banner.securityQA

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import net.hedtech.banner.service.ServiceBase
import org.apache.log4j.Logger

import java.sql.SQLException


class SecurityQAService extends ServiceBase {
    static transactional = true
    def sessionFactory

    private final Logger log = Logger.getLogger(getClass())

    def saveSecurityQAResponse(String pidm, String question1, String question2, String answer1, int question_num) {

        // need to do following validations
        /**
         *   1. if question 1 and qs 2 is not null -> error  "Please enter only one question."
         *   2. if ans 1 and ans2 is not null -> error  "Please enter only one answer."   -> not applicable
         *   3. if ans1 is not null and ( q1 is null and q2 is null) -> error "Please enter Security Question and Answer."
         *   4. if ans1 is null and ( q1 is not null or q2 is not null) -> error "Please enter Security Question and Answer."
         *   5. if ans1 is null and ans2 is null -> error "Please enter Security Question and Answer." -> not applicable
         *   6. q's should not have "<" / ">"  -> error   "Question may not contain the < or > characters."
         *   7. ans should not have "<" / ">" -> error    "Answer may not contain the < or > characters."
         *   8. if ans1 is not NULL and length of ans1 < GUBPPRF_ANSR_MIN_LENGTH and GUBPPRF_ANSR_MIN_LENGTH > 0 -> error  "Answer has to be %01% characters or more."
         *   9. if ans2 is not NULL and length of ans1 < GUBPPRF_ANSR_MIN_LENGTH and GUBPPRF_ANSR_MIN_LENGTH > 0 ->
         *       error  "Answer has to be %01% characters or more."  -> not applicable
         *   10. if q2 is not NULL and length of ans1 < GUBPPRF_QSTN_MIN_LENGTH and GUBPPRF_QSTN_MIN_LENGTH > 0 -> error  "Question has to be %01% characters or more."
         * */

        if ((question1 != null && !question1.equals("")) && (question2 != null && question2.equals(""))) {
            log.error("Please enter only one question.")
            throw new IllegalArgumentException("Please enter only one question.")
        }

        if ((answer1 != null && !answer1.equals("")) && (question1 == null || question1.equals("")) && (question2 == null || question2.equals(""))) {
            log.error("Please enter Security Question and Answer.")
            throw new IllegalArgumentException("Please enter Security Question and Answer.")
        }

        if ((answer1 == null || answer1.equals("")) && (question1 != null && !question1.equals("")) || (question2 != null && !question2.equals(""))) {
            log.error("Please enter Security Question and Answer.")
            throw new IllegalArgumentException("Please enter Security Question and Answer.")
        }

        if (question2.contains("<") || question2.contains(">")) {
            log.error("Question may not contain the < or > characters.")
            throw new IllegalArgumentException("Question may not contain the < or > characters.")
        }

        if (answer1.contains("<") || answer1.contains(">")) {
            log.error("Answer may not contain the < or > characters.")
            throw new IllegalArgumentException("Answer may not contain the < or > characters.")
        }

        def GUBPPRF_ANSR_MIN_LENGTH = getAnswerMinimumLength()
        if ((answer1 != null || !answer1.equals("")) && answer1.length() < GUBPPRF_ANSR_MIN_LENGTH && GUBPPRF_ANSR_MIN_LENGTH > 0) {
            log.error("Answer has to be %01% characters or more.")
            throw new IllegalArgumentException("Answer has to be %01% characters or more.")
        }

        def GUBPPRF_QSTN_MIN_LENGTH = getQuestionMinimumLength()
        if ((question2 != null || !question2.equals("")) && question2.length() < GUBPPRF_QSTN_MIN_LENGTH && GUBPPRF_QSTN_MIN_LENGTH > 0) {
            log.error("Answer has to be %01% characters or more.")
            throw new IllegalArgumentException("Answer has to be %01% characters or more.")
        }

        /**
         * if question_num is NULL then
         *    11. if for that pidm , q1 & q2 & question_num != record_q_num, chk the gobansrc if it has a record with it ->
         *       error   "Please select a unique question."   -> not applicable
         *    if q1 and ans1 is not NULL then update with q1 and ans1
         *    else update with q2 and ans1
         * else
         *    12. if for that pidm , q1 & q2, chk the gobansrc if it has a record with it ->
         *        error  "Please select a unique question." -> not applicable
         *    if q1 and ans1 is not NULL then update with q1 and ans1
         *    else update with q2 and ans1
         */

        if (question_num == null) {
            if ((question1 != null && !question1.equals("")) && (answer1 != null && !answer1.equals(""))) {
                // update
            } else {
                // update
            }
        } else {
            if ((question1 != null && !question1.equals("")) && (answer1 != null && !answer1.equals(""))) {
                //create
            } else {
                //create
            }
        }

    }

    private def getQuestionMinimumLength() {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select GUBPPRF_QSTN_MIN_LENGTH from GUBPPRF""")
            return row?.GUBPPRF_QSTN_MIN_LENGTH
        } catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        } finally {
            connection.close()
        }
    }

    private def getAnswerMinimumLength() {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select GUBPPRF_ANSR_MIN_LENGTH from GUBPPRF""")
            return row?.GUBPPRF_ANSR_MIN_LENGTH
        } catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        } finally {
            connection.close()
        }
    }

    public def getNumberOfQuestionsAnswered() {
        return 0
    }

    public def getUserDefinedQuestionFlag() {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select GUBPPRF_EDITQSTN_IND from GUBPPRF""")
            return row?.GUBPPRF_EDITQSTN_IND
        } catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        } finally {
            connection.close()
        }
    }


    public def getNumberOfQuestions() {
        def connection
        Sql sql
        def questions = [:]
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row =sql.firstRow("""select GUBPPRF_NO_OF_QSTNS from GUBPPRF""")
            return row?.GUBPPRF_NO_OF_QSTNS
        } catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        } finally {
            connection.close()
        }
    }
}
