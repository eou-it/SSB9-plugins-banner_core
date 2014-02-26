/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.menu

import groovy.sql.Sql
import org.apache.commons.lang.math.RandomUtils
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * Service for retrieving Banner menu item for Classic SSB.
 */

class SelfServiceMenuService {
    static transactional = true
    def sessionFactory
    private final log = Logger.getLogger(getClass())

    /**
     * This is returns map of all menu items based on user access
     * @return List representation of menu objects that a user has access
     */

    def bannerMenu(def menuName, def menuTrail, def facultyPidm) {

        processMenu(menuName, menuTrail, facultyPidm)
    }

    /**
     * This is returns map of all personal items based on user access
     * @return Map of menu objects that a user has access
     */

    private def processMenu(def menuName, def menuTrail, def pidm) {

        //assert facultyPidm

        def dataMap = []
        def firstMenu = "Banner";
        menuName = toggleSeparator(menuName);

        Sql sql
        if (log.isDebugEnabled()) log.debug("Process Menu started for nenu:" + menuName)
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        if (log.isDebugEnabled()) log.debug("SQL Connection:" + sql.useConnection.toString())

        menuName = menuName ?: "bmenu.P_MainMnu"
        def govroleCriteria
        def govroles = []
        def sqlQuery;
        if (pidm) {
            sql.eachRow("select govrole_student_ind, govrole_alumni_ind, govrole_employee_ind, govrole_faculty_ind, govrole_finance_ind ,govrole_friend_ind ,govrole_finaid_ind, govrole_bsac_ind from govrole where govrole_pidm = ? ", [pidm]) {
                if (it.govrole_student_ind == "Y" )  govroles.add ("STUDENT")
                if (it.govrole_faculty_ind == "Y" )  govroles.add ("FACULTY")
                if (it.govrole_employee_ind == "Y" )  govroles.add ("EMPLOYEE")
                if (it.govrole_alumni_ind == "Y" )  govroles.add ("ALUMNI")
                if (it.govrole_finance_ind == "Y" )  govroles.add ("FINANCE")
                if (it.govrole_finaid_ind == "Y" )  govroles.add ("FINAID")
                if (it.govrole_friend_ind == "Y" )  govroles.add ("FRIEND")
            }
            if (govroles.size() > 0) {

                govroles.each {
                                if (it == govroles.first())
                                    govroleCriteria = "('" + it.value +"'"
                                else
                                    govroleCriteria= govroleCriteria + " ,'" + it.value +"'"
                }
                govroleCriteria= govroleCriteria + ")"
            }
        }
        String pidmCondition = "twgrrole_pidm is NULL"
        if(pidm) {
            pidmCondition = "twgrrole_pidm = " + pidm
        }

        if (govroles.size() > 0)
            sqlQuery =  " select  TWGRMENU_NAME,TWGRMENU_SEQUENCE,TWGRMENU_URL_TEXT,TWGRMENU_URL	,TWGRMENU_URL_DESC,TWGRMENU_IMAGE,TWGRMENU_ENABLED, TWGRMENU_DB_LINK_IND, TWGRMENU_SUBMENU_IND,TWGRMENU_TARGET_FRAME, TWGRMENU_STATUS_TEXT,TWGRMENU_ACTIVITY_DATE ,TWGRMENU_URL_IMAGE,TWGRMENU_SOURCE_IND " +
                        " from twgrmenu   where  twgrmenu_name = ?  and " +
                        " twgrmenu_enabled = 'Y'  " +
                        " and (twgrmenu_url in (select  twgrwmrl_name from twgrwmrl ,twgrrole where twgrrole_pidm = " + pidm +
                        " and twgrrole_role = twgrwmrl_role " +
                        " and twgrmenu_source_ind =  (select nvl( max(twgrmenu_source_ind ),'B') from twgrmenu where  twgrmenu_name = ? and twgrmenu_source_ind='L') ) )" +
                        " union select  TWGRMENU_NAME,TWGRMENU_SEQUENCE	,TWGRMENU_URL_TEXT,TWGRMENU_URL	,TWGRMENU_URL_DESC,TWGRMENU_IMAGE,TWGRMENU_ENABLED, "+
                        " TWGRMENU_DB_LINK_IND, TWGRMENU_SUBMENU_IND,TWGRMENU_TARGET_FRAME, TWGRMENU_STATUS_TEXT,TWGRMENU_ACTIVITY_DATE ,TWGRMENU_URL_IMAGE,TWGRMENU_SOURCE_IND from twgrmenu " +
                        " where  twgrmenu_name = ?  " +
                        " and twgrmenu_enabled = 'Y' "+
                        " and (twgrmenu_url in (select  twgrwmrl_name from twgrwmrl ,govrole where govrole_pidm =  " + pidm + " and " +
                        " twgrwmrl_role in " +govroleCriteria +" and twgrmenu_source_ind = "+
                        " ( select nvl( max(twgrmenu_source_ind ),'B') from twgrmenu        where  twgrmenu_name = ? " +
                        " and twgrmenu_source_ind='L') )) or (twgrmenu_name = ? and twgrmenu_db_link_ind = 'N' and twgrmenu_enabled = 'Y' and twgrmenu_source_ind = " +
                        " (  select nvl( max(twgrmenu_source_ind ),'B') from twgrmenu  where  twgrmenu_name = ?  " +
                        " and twgrmenu_source_ind='L')) ORDER BY twgrmenu_sequence "


        else
            sqlQuery = "select * from twgrmenu  where  twgrmenu_name = ? and ? = ? and   twgrmenu_enabled = 'Y'  and (twgrmenu_url in (select  twgrwmrl_name from twgrwmrl ,twgrrole where " + pidmCondition + " and twgrrole_role = twgrwmrl_role   and twgrmenu_source_ind =  (select nvl( max(twgrmenu_source_ind ),'B')   from twgrmenu where  twgrmenu_name = ? and twgrmenu_source_ind='L' and ? = ?) )  or twgrmenu_db_link_ind = 'N')  order by twgrmenu_sequence"

        def randomSequence = RandomUtils.nextInt(1000);
		
        sql.eachRow(sqlQuery, [menuName, menuName, menuName, menuName, menuName, menuName]) {

            def mnu = new SelfServiceMenu()
            mnu.formName = toggleSeparator(it.twgrmenu_url)
            mnu.pageName = it.twgrmenu_submenu_ind == "Y" ? null : toggleSeparator(it.twgrmenu_url)
            mnu.name = it.twgrmenu_url_text
            mnu.caption = toggleSeparator(it.twgrmenu_url_text)
            mnu.pageCaption = mnu.caption
            mnu.type = it.twgrmenu_submenu_ind == "Y" ? 'MENU' : 'FORM'
            mnu.menu = menuTrail ? menuTrail : firstMenu

            mnu.url = it.twgrmenu_db_link_ind == "Y" ? ConfigurationHolder?.config?.banner8?.SS?.url + it.twgrmenu_url : toggleSeparator(it.twgrmenu_url)
            mnu.seq = randomSequence + "-" + it.twgrmenu_sequence.toString()
            mnu.captionProperty = false
            mnu.parent = ''

            dataMap.add(mnu)

        };

        if (log.isDebugEnabled()) log.debug("ProcessMenu executed for Menu name:" + menuName)
        sql.connection.close()
        return dataMap

    }

    /**
     * Converts ~ to _ and _ to ~.
     * Aurora uses _ as Separators and will conflict with the menu names.
     * @param stringText
     * @return
     */

    private String toggleSeparator(String stringText) {
        if (stringText == null) return null;

        def oldSeparator = "_"
        def newSeparator = "~"
        stringText = stringText.contains(oldSeparator) ? stringText.replaceAll(oldSeparator, newSeparator) : stringText.replaceAll(newSeparator, oldSeparator)
    }

}
