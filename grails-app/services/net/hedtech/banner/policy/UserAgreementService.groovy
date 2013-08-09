/** *****************************************************************************
 © 2013 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.policy

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import java.sql.CallableStatement
import java.sql.SQLException

/**
 * UserAgreementService.
 *
 * Date: 7/22/13
 * Time: 2:55 PM
 */
class UserAgreementService {
    static transactional = true
    def sessionFactory

    public void updateUsageIndicator(String pidm,String usageIndicator)
    {
        def connection
        Sql sql
        try{
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            sql.call ("{call gb_third_party_access.p_update(p_pidm=>${pidm}, p_usage_accept_ind=>${usageIndicator})}")
            sql.commit()
            connection.close()
        }catch (SQLException ae) {
            sql.close()
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }


       /* def connection
        try {
            CallableStatement minMaxCalcStatement
            connection = sessionFactory.currentSession.connection()
            String minMaxCalc = "{ call gb_third_party_access.p_update(?,null,?,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null) }"
            minMaxCalcStatement = connection.prepareCall(minMaxCalc)
            minMaxCalcStatement.setInt(1, Integer.parseInt(pidm))
            minMaxCalcStatement.setString(3,usageIndicator)
            minMaxCalcStatement.executeUpdate()
        }
        catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }*/
    }

    public String getUsageIndicator(String pidm){
        def connection
        Sql sql
        try{
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select GOBTPAC_USAGE_ACCEPT_IND from GOBTPAC where GOBTPAC_PIDM = ${pidm}""")
            return row.GOBTPAC_USAGE_ACCEPT_IND
        }catch (SQLException ae) {
            sql.close()
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }
    }

    public String getTermsOfUsageDisplayStatus(){
        def connection
        Sql sql
        try{
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select TWGBWRUL_DISP_USAGE_IND from TWGBWRUL""")
            return row.TWGBWRUL_DISP_USAGE_IND
        }catch (SQLException ae) {
            sql.close()
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }
    }



    public String getTermsOfUseInfoText(){
        def connection
        Sql sql
        try{
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            String name = "twbkwbis.P_UsagePage"
            String label = "DEFAULT"
            def sqlQueryString = """select twgrinfo_text text from twgrinfo
    	    					    where  twgrinfo_name = ${name}
    	    					    and    twgrinfo_label = ${label}
    	    						and twgrinfo_source_ind = 'B'
    	       						"""

    		def infoText = ""
    		sql.rows(sqlQueryString).each {t -> infoText += t.text + "\n"}
    		if(infoText == "null\n") {
                infoText = ""
            }
            connection.close()
            return infoText
        }catch (SQLException ae) {
            connection.close()
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {connection.close()

            log.debug ae.stackTrace
            throw ae
        }

    }

}
