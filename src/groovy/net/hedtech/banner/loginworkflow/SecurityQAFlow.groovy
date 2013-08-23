package net.hedtech.banner.loginworkflow

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.context.ApplicationContext

import java.sql.SQLException

class SecurityQAFlow implements PostLoginWorkflow {

    def sessionFactory
    private final log = Logger.getLogger(getClass())

    //def securityQAService

    public boolean showPage(request) {
        def session = request.getSession();
        String isDone = session.getAttribute("securityqadone")
        boolean displayPage = false
        if(isDone != "true"){
            def noOfQuestions = getNumberOfQuestions()
            if(getDisableForgetPinIndicator().equals("N") && noOfQuestions > 0 && !isUserAlreadyAnsweredSecurityQA(noOfQuestions)) {
                displayPage = true
            }
        }

        return displayPage
    }

    public String getControllerUri() {
        return "/ssb/securityQA"
    }

    private String getDisableForgetPinIndicator(){
        def connection
        Sql sql
        try{
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select GUBPPRF_DISABLE_FORGET_PIN_IND from GUBPPRF""")
            return row?.GUBPPRF_DISABLE_FORGET_PIN_IND
        }catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }finally{
            connection.close()
        }
    }

    private def getNumberOfQuestions(){
        def connection
        Sql sql
        try{
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select GUBPPRF_NO_OF_QSTNS from GUBPPRF""")
            return row?.GUBPPRF_NO_OF_QSTNS
        }catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }finally{
            connection.close()
        }
    }

    private boolean isUserAlreadyAnsweredSecurityQA(noOfQuestions){
        /*ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        ctx.getBean("userAgreementFlow")*/
       /* if(noOfQuestions > securityQAService.getNumberOfQuestionsAnswered()) {
            return false
        }*/

        //return true
        return false
    }
}
