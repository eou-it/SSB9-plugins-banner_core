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

    def bannerMenu(def menuName) {

        processMenu(menuName)
    }

    /**
     * This is returns map of all personal items based on user access
     * @return Map of menu objects that a user has access
     */

    private def processMenu(def menuName) {
        def dataMap = []
        def parentMenuName = "Banner";
        def parentName = ""
        menuName = toggleSeparator(menuName);
        def twgrmenu_name
        def parentSequence = ""

        Sql sql
        if (log.isDebugEnabled()) log.debug("Process Menu started for nenu:" + menuName)
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        if (log.isDebugEnabled()) log.debug("SQL Connection:" + sql.useConnection.toString())


        if (menuName) {
            def row = sql.firstRow("select * from twgrmenu where twgrmenu_name = 'bmenu.P_MainMnu' and twgrmenu_source_ind = 'B' and twgrmenu_url_text = '" + menuName + "'")
            parentMenuName = parentMenuName + "/" + toggleSeparator(row.twgrmenu_url_text)
            parentName = toggleSeparator(row.twgrmenu_url_text)
            twgrmenu_name = row.twgrmenu_url
            parentSequence = row.twgrmenu_sequence
        }

        //def sqlQuery = menuName ? "select * from twgrmenu where twgrmenu_name = '" + twgrmenu_name + "' and twgrmenu_source_ind = 'B'" : "select * from twgrmenu where twgrmenu_name = 'bmenu.P_MainMnu' and twgrmenu_source_ind = 'B'"

        twgrmenu_name = twgrmenu_name?: "bmenu.P_MainMnu"

        def sqlQuery = "select * from twgrmenu where  twgrmenu_name = '" + twgrmenu_name + "' and twgrmenu_enabled = 'Y' and twgrmenu_source_ind = (select nvl( 'B',nvl( max(twgrmenu_source_ind ),'B')) from twgrmenu where twgrmenu_name = '" + twgrmenu_name + "' and twgrmenu_source_ind='L') order by twgrmenu_sequence";

        sql.eachRow(sqlQuery, {

            def mnu = new SelfServiceMenu()

            mnu.formName = toggleSeparator(it.twgrmenu_url)
            mnu.pageName = menuName ? toggleSeparator(it.twgrmenu_url) : null
            mnu.name = it.twgrmenu_url_text
            mnu.caption = toggleSeparator(it.twgrmenu_url_text)
            mnu.pageCaption = mnu.caption
            mnu.type = menuName ? 'FORM' : 'MENU'
            mnu.menu = parentMenuName
            mnu.url = menuName ? ConfigurationHolder.config.banner8.SS.url + it.twgrmenu_url : ""
            mnu.seq = parentSequence.toString() + "-" + it.twgrmenu_sequence.toString()
            mnu.captionProperty = false

            if (menuName)
                mnu.parent = toggleSeparator(parentName)

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