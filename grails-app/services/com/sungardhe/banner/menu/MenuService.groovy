/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.
 CONFIDENTIAL BUSINESS INFORMATION
 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.menu

import org.springframework.security.core.context.SecurityContextHolder

import groovy.sql.Sql
import org.apache.log4j.Logger

class MenuService {
    static transactional = true
    def menuAndToolbarPreferenceService
    def sessionFactory
    private final log = Logger.getLogger(getClass())


    /**
    * This is returns map of all menu items based on user access
    * @return List representation of menu objects that a user has access
    */
    def bannerMenu() {
        def map = processMenu()
        return map

    }
    /**
    * This is returns map of all personal menu items based on user access
    * @return List representation of personal menu objects that a user has access
    */
    def personalMenu() {
        def map = personalMenuMap()
        return map
    }

    /**
    * This is returns map of all personal items based on user access
    * @return Map of menu objects that a user has access
    */


    def personalMenuMap() {
        def dataMap = []
        Sql sql
        def parent
        log.debug("Personal Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.execute("Begin gukmenu.p_bld_pers_menu; End;")
        log.debug("After gukmenu.p_bld_pers_menu sql.execute" )
        sql.eachRow("select * from gutpmnu,gubmodu,gubpage,gubobjs where  substr(gutpmnu_value,6,length(gutpmnu_value))  = gubpage_code (+) AND " +
            " gubobjs_name = substr(gutpmnu_value,6,length(gutpmnu_value)) AND gubobjs_ban9_flag = 'A' AND gubpage_gubmodu_surrogate_id  = gubmodu_surrogate_id (+) order by gutpmnu_seq_no", {

        def mnu = new Menu()

        mnu.formName = it.gutpmnu_value.split("\\|")[1]

        mnu.pageName = it.gubpage_name
        mnu.caption = it.gutpmnu_label
        if (getMnuPref())
            mnu.caption = it.gutpmnu_label + " (" + mnu.formName + ")"
        mnu.level = it.gutpmnu_level
        mnu.type = it.gutpmnu_value.split("\\|")[0]
        mnu.module = it.gubmodu_name
        mnu.url = it.gubmodu_url
        mnu.seq = it.gutpmnu_seq_no
        mnu.parent = setParent(mnu.level,dataMap)
        dataMap.add(mnu)
        }
    );

    log.debug("Personal Menu executed" )
    sql.connection.close()
    return dataMap
    }


    def setParent(def level,def map) {
        String parent
        if (level == 1)
            return parent
        def notFound = true;
        map.reverseEach {
            if (  notFound && it.level < level )  {
                    parent= it.formName
                    notFound = false
            }
        }
        return parent
    }

    /**
    * This  returns form name for a given page name
    * @param pageName
    * @return Form name
    */
    def getFormName(String pageName) {
        def formName
        def sql
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow("select * from gubpage where gubpage_name = ?", [pageName]) {
            formName = it.gubpage_code
        }
        return formName
    }

    /**
    * This is returns map of all personal items based on user access
    * @return Map of menu objects that a user has access
    */
    private def processMenu() {
        def dataMap = []
        def menuMap = []
        Sql sql
        log.debug("Process Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        log.debug(sql.useConnection.toString())
        sql.execute("Begin gukmenu.p_bld_prod_menu; End;")
        sql.eachRow("select * from gutmenu,gubmodu,gubpage,gubobjs where gutmenu_value  = gubpage_code (+) AND " +
            " gubobjs_name = gutmenu_value AND gubobjs_ban9_flag = 'A' and gubpage_gubmodu_surrogate_id  = gubmodu_surrogate_id (+) " +
            " order by gutmenu_seq_no", {
            def mnu = new Menu()
            def clnMenu = true
            if (it.gutmenu_objt_code == "MENU")
                menuMap.add(it.gutmenu_value)
            println "Before - " + it.gutmenu_value || it.gubpage_name  ||it.gutmenu_prior_obj
            if ((it.gutmenu_objt_code == "FORM") && (!menuMap.contains(it.gutmenu_prior_obj)))
                clnMenu = false
            if (clnMenu) {
                mnu.formName = it.gutmenu_value
                mnu.pageName = it.gubpage_name
                if (it.gutmenu_desc != null)  {
                    mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;")
                    if (getMnuPref())
                        mnu.caption = mnu.caption + " (" + mnu.formName + ")"
                }
                mnu.level = it.gutmenu_level
                mnu.type = it.gutmenu_objt_code
                mnu.parent = it.gutmenu_prior_obj
                mnu.module = it.gubmodu_name
                mnu.url = it.gubmodu_url
                mnu.seq = it.gutmenu_seq_no
                dataMap.add(mnu)
            }
        });
    log.debug("ProcessMenu executed" )
    sql.connection.close()
    return dataMap
    }

    /**
    * This is returns map of all personal items based on user access
    * @return Map of menu objects that a user has access
    */
    private def getMnuPref() {
        return menuAndToolbarPreferenceService.fetchMenuAndToolbarPreference().get(0).formnameDisplayIndicator
    }

    /**
    * This returns map of all menu item for searching in goto
    * @return Map of menu objects that a user has access
    */
    def gotoMenu() {
        def dataMap = []
        Sql sql
        log.debug("Goto Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        log.debug(sql.useConnection.toString())
        sql.execute("Begin gukmenu.p_bld_prod_menu; End;")
        sql.eachRow("select distinct gutmenu_value,gutmenu_desc,gubpage_name " +
            " from gutmenu,gubpage,gubobjs where gutmenu_value  = gubpage_code (+) AND " +
            " gubobjs_name = gutmenu_value AND gubobjs_ban9_flag = 'A'  ", {
        def mnu = new Menu()
        mnu.formName = it.gutmenu_value
        mnu.pageName = it.gubpage_name
        if (it.gutmenu_desc != null)  {
            mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;")
            if (getMnuPref())
                mnu.caption = mnu.caption + " (" + mnu.formName + ")"
        }
        dataMap.add(mnu)
        });
        log.debug("GotoMenu executed" )
        sql.connection.close()
        return dataMap
    }
}