/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
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
		def mnuPrf = getMnuPref()
        Sql sql
        def parent
        log.debug("Personal Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.execute("Begin gukmenu.p_bld_pers_menu('BAN9'); End;")
        log.debug("After gukmenu.p_bld_pers_menu sql.execute" )
        sql.eachRow("select * from gutpmnu,gubmodu,gubpage,gubobjs where  substr(gutpmnu_value,6,length(gutpmnu_value))  = gubpage_code (+) AND " +
            " gubobjs_name = substr(gutpmnu_value,6,length(gutpmnu_value)) AND gubobjs_ui_version IN ('A','C') AND gubpage_gubmodu_code  = gubmodu_code (+) order by gutpmnu_seq_no", {

        def mnu = new Menu()

        mnu.formName = it.gutpmnu_value.split("\\|")[1]

        mnu.pageName = it.gubpage_name
        mnu.caption = it.gutpmnu_label
        if (mnuPrf)
            mnu.caption = it.gutpmnu_label + " (" + mnu.formName + ")"
            mnu.pageCaption = it.gutpmnu_label
            mnu.level = it.gutpmnu_level
            mnu.type = it.gutpmnu_value.split("\\|")[0]
            mnu.module = it.gubmodu_name
            mnu.url = it.gubmodu_url
            mnu.seq = it.gutpmnu_seq_no
            mnu.parent = setParent(mnu.level,dataMap)
            mnu.captionProperty = mnuPrf
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
		def mnuPrf = getMnuPref()
        Sql sql
        log.debug("Process Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        log.debug(sql.useConnection.toString())
        sql.execute("Begin gukmenu.p_bld_prod_menu('BAN9'); End;")
        sql.eachRow("select * from gutmenu,gubmodu,gubpage,gubobjs where gutmenu_value  = gubpage_code (+) AND " +
            " gubobjs_name = gutmenu_value AND gubobjs_ui_version IN ('A','C') and gubpage_gubmodu_code  = gubmodu_code (+) " +
            " order by gutmenu_seq_no", {
            def mnu = new Menu()
            def clnMenu = true
            if (it.gutmenu_objt_code == "MENU")
                menuMap.add(it.gutmenu_value)
            if ((it.gutmenu_objt_code == "FORM") && (!menuMap.contains(it.gutmenu_prior_obj)))
                clnMenu = false
            if (clnMenu) {
                mnu.formName = it.gutmenu_value
                mnu.pageName = it.gubpage_name
                if (it.gutmenu_desc != null)  {
                    mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;")
                    mnu.pageCaption = mnu.caption
                    if (mnuPrf)
                        mnu.caption = mnu.caption + " (" + mnu.formName + ")"
                }
                mnu.level = it.gutmenu_level
                mnu.type = it.gutmenu_objt_code
                mnu.parent = it.gutmenu_prior_obj
                mnu.module = it.gubmodu_name
                mnu.url = it.gubmodu_url
                mnu.seq = it.gutmenu_seq_no
                mnu.captionProperty = mnuPrf
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
        boolean isMnuPref = false
        try {
            SecurityContextHolder.context?.authentication?.principal?.pidm

            if (menuAndToolbarPreferenceService.fetchMenuAndToolbarPreference().get(0).formnameDisplayIndicator  == 'B' ||
                menuAndToolbarPreferenceService.fetchMenuAndToolbarPreference().get(0).formnameDisplayIndicator  == 'Y')
                isMnuPref = true
        }
        catch (Exception e) {
            //If pidm is not defined
        }
        return isMnuPref
    }


    /**
    * This returns map of all menu item for searching in goto
    * @return Map of menu objects that a user has access
    */
    def gotoMenu( String searchVal ) {
        searchVal = searchVal.toUpperCase()
        def dataMap = []
        def mnuPrf = getMnuPref()
        Sql sql
        log.debug("Goto Menu started")
        sql = new Sql( sessionFactory.getCurrentSession().connection() )
        log.debug( sql.useConnection.toString() )
        sql.execute( "Begin gukmenu.p_bld_prod_menu; End;" )
        sql.eachRow("select distinct gutmenu_value,gutmenu_desc,gubpage_name, gubmodu_url " +
                " from gutmenu,gubmodu, gubpage,gubobjs where gutmenu_value  = gubpage_code (+) AND " +
                " gubobjs_name = gutmenu_value AND gubobjs_ui_version IN ('A','C')  and gubpage_gubmodu_code  = gubmodu_code (+) AND " +
                " (upper(gutmenu_value) like '%$searchVal%' OR upper(gutmenu_desc) like '%$searchVal%' OR upper(gubpage_name) like '%$searchVal%' )", {
            def mnu = new Menu()
            mnu.formName = it.gutmenu_value
            mnu.pageName = it.gubpage_name
            mnu.url = it.gubmodu_url
            mnu.captionProperty = mnuPrf
            if (it.gutmenu_desc != null) {
                mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;")
                mnu.pageCaption = mnu.caption
                if (getMnuPref())
                    mnu.caption = mnu.caption + " (" + mnu.formName + ")"
            }
            dataMap.add( mnu )
        });
        log.debug( "GotoMenu executed" )
        sql.connection.close()
        return dataMap
    }

}