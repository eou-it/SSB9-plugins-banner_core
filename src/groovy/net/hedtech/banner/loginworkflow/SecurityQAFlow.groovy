package net.hedtech.banner.loginworkflow

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.log4j.Logger

import java.sql.SQLException

class SecurityQAFlow implements PostLoginWorkflow {

    def sessionFactory
    private final log = Logger.getLogger(getClass())

    public boolean showPage(request) {
        def session = request.getSession();
        String isDone = session.getAttribute("securityqadone")
        boolean displayPage = false
        if(isDone != "true"){
            if(getDisableForgetPinIndicator().equals("N") && getNumberOfQuestions() > 0) {
                // todo: need to check if user has already entered the answers before
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
}
