/** *******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 ********************************************************************************* */
package com.sungardhe.banner.menu

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

    def bannerMenu(def menuName, def menu) {

        processMenu(menuName, menu)
    }

    /**
     * This is returns map of all personal items based on user access
     * @return Map of menu objects that a user has access
     */

    private def processMenu(def menuName, def menu) {
        def dataMap = []
        def firstMenu = "Banner";
        menuName = toggleSeparator(menuName);

        Sql sql
        if (log.isDebugEnabled()) log.debug("Process Menu started for nenu:" + menuName)
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        if (log.isDebugEnabled()) log.debug("SQL Connection:" + sql.useConnection.toString())

        menuName = menuName ?: "bmenu.P_MainMnu"

        def sqlQuery = "select * from twgrmenu where  twgrmenu_name = '" + menuName + "' and twgrmenu_enabled = 'Y' and twgrmenu_source_ind = (select nvl( 'B',nvl( max(twgrmenu_source_ind ),'B')) from twgrmenu where twgrmenu_name = '" + menuName + "' and twgrmenu_source_ind='L') order by twgrmenu_sequence";

        def randomSequence = RandomUtils.nextInt(1000);

        sql.eachRow(sqlQuery, {

            def mnu = new SelfServiceMenu()

            mnu.formName = toggleSeparator(it.twgrmenu_url)
            mnu.pageName = it.twgrmenu_submenu_ind == "Y" ? null : toggleSeparator(it.twgrmenu_url)
            mnu.name = it.twgrmenu_url_text
            mnu.caption = toggleSeparator(it.twgrmenu_url_text)
            mnu.pageCaption = mnu.caption
            mnu.type = it.twgrmenu_submenu_ind == "Y" ? 'MENU' : 'FORM'
            mnu.menu = menu ? menu : firstMenu
            mnu.url = it.twgrmenu_db_link_ind == "Y" ? ConfigurationHolder.config.banner8.SS.url + it.twgrmenu_url : toggleSeparator(it.twgrmenu_url)
            mnu.seq = randomSequence + "-" + it.twgrmenu_sequence.toString()
            mnu.captionProperty = false
            mnu.parent = ''

            dataMap.add(mnu)

        });

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