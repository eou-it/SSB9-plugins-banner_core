/** *****************************************************************************
 © 2013 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.policy

import groovy.sql.Sql
import org.apache.log4j.Logger
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
    private final log = Logger.getLogger(getClass())
    public void updateUsageIndicator(String pidm,String usageIndicator)
    {
        def connection
        Sql sql
        try{
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            sql.call ("{call gb_third_party_access.p_update(p_pidm=>${pidm}, p_usage_accept_ind=>${usageIndicator})}")
            sql.commit()
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
            return infoText
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
