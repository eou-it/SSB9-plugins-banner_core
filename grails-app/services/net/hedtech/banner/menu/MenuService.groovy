/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.menu

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder

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
        def map = RequestContextHolder.currentRequestAttributes().request.session.getAttribute("menuList")
        if (!map)
            map = processMenu()
        map

    }
    /**
     * This is returns map of all personal menu items based on user access
     * @return List representation of personal menu objects that a user has access
     */
    def personalMenu() {
        def map = RequestContextHolder.currentRequestAttributes().request.session.getAttribute("personalMenuList")
        if (!map)
            map = personalMenuMap()
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
        log.debug("After gukmenu.p_bld_pers_menu sql.execute")
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
            mnu.parent = setParent(mnu.level, dataMap)
            mnu.captionProperty = mnuPrf
            dataMap.add(mnu)
        }
        );

        log.debug("Personal Menu executed")
        sql.connection.close()
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute("personalMenuList", dataMap)
        return dataMap
    }


    def setParent(def level, def map) {
        String parent
        if (level == 1)
            return parent
        def notFound = true;
        map.reverseEach {
            if (notFound && it.level < level) {
                parent = it.formName
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
                if (it.gutmenu_desc != null) {
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
        log.debug("ProcessMenu executed")
        sql.connection.close()
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute("menuList", dataMap)
        dataMap
    }

    /**
     * This is returns map of all personal items based on user access
     * @return Map of menu objects that a user has access
     */
    private def getMnuPref() {
        boolean isMnuPref = false
        try {
            SecurityContextHolder.context?.authentication?.principal?.pidm

            if (menuAndToolbarPreferenceService.fetchMenuAndToolbarPreference().get(0).formnameDisplayIndicator == 'B' ||
                    menuAndToolbarPreferenceService.fetchMenuAndToolbarPreference().get(0).formnameDisplayIndicator == 'Y')
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
    def gotoMenu(String searchVal) {
        searchVal = searchVal.toUpperCase()
        def dataMap = []
        def mnuPrf = getMnuPref()
        Sql sql
        log.debug("Goto Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        log.debug(sql.useConnection.toString())
        sql.execute( "Begin gukmenu.p_bld_prod_menu('BAN9'); End;" )
        def searchValWild = "%" + searchVal + "%"
        sql.eachRow("select distinct gutmenu_value,gutmenu_desc,gubpage_name, gubmodu_url  from gutmenu,gubmodu, gubpage,gubobjs where gutmenu_value  = gubpage_code (+) AND  gubobjs_name = gutmenu_value AND gubobjs_ui_version IN ('A','C')  and gubpage_gubmodu_code  = gubmodu_code (+) AND  (upper(gutmenu_value) like ? OR upper(gutmenu_desc) like ? OR upper(gubpage_name) like ?)", [searchValWild, searchValWild, searchValWild]) {
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
            dataMap.add(mnu)
        }
        log.debug("GotoMenu executed")
        sql.connection.close()
        return dataMap
    }

    /**
     * This returns map of all menu item for searching in goto
     * @return Map of menu objects that a user has access
     */
    def gotoCombinedMenu( String searchVal ) {
        searchVal = searchVal.toUpperCase()
        def dataMap = []
        def mnuPrf = getMnuPref()
        Sql sql
        log.debug("Goto Menu started")
        sql = new Sql( sessionFactory.getCurrentSession().connection() )
        log.debug( sql.useConnection.toString() )
        sql.execute( "Begin gukmenu.p_bld_prod_menu('MAG'); End;" )
        def searchValWild = "%" +searchVal +"%"
        sql.eachRow("select distinct gutmenu_value,gutmenu_level,gutmenu_seq_no,gubobjs_ui_version,gutmenu_prior_obj,gutmenu_objt_code,gutmenu_desc,gubpage_name, gubmodu_url  from gutmenu,gubmodu, gubpage,gubobjs where gutmenu_value  = gubpage_code (+) AND  gubobjs_name = gutmenu_value AND gubobjs_ui_version IN ('A','B','C')  and gubpage_gubmodu_code  = gubmodu_code (+) AND  (upper(gutmenu_value) like ? OR upper(gutmenu_desc) like ? OR upper(gubpage_name) like ?)",[searchValWild,searchValWild,searchValWild] ) {
            def mnu = new Menu()
            mnu.formName = it.gutmenu_value
            mnu.name = it.gutmenu_value
            mnu.page = (it.gubobjs_ui_version == "B") ? it.gutmenu_value : it.gubpage_name
            //mnu.page = it.gubpage_name
            mnu.menu = getFormName(it.gubpage_name)
            if (it.gutmenu_desc != null)  {
                mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;")
                if (mnuPrf)
                    mnu.caption = mnu.caption + " (" + mnu.name + ")"
            }
            mnu.level = it.gutmenu_level
            mnu.seq = it.gutmenu_seq_no
            mnu.type = it.gutmenu_objt_code
            mnu.parent = it.gutmenu_prior_obj
            mnu.url = it.gubmodu_url
            mnu.uiVersion = (it.gubobjs_ui_version == "B") ? "banner8admin" : "bannerXEadmin"
            dataMap.add( mnu )
        }
        log.debug( "GotoMenu executed" )
        sql.connection.close()
        return dataMap
    }
    /**
     * This is returns map of all menu items based on user access
     * @return List representation of menu objects that a user has access
     */
    def bannerCombinedMenu() {
        def map = processCombinedMenu()
        return map

    }
    /**
     * This is returns map of all personal items based on user access
     * @return Map of menu objects that a user has access
     */
    private def processCombinedMenu() {
        def dataMap = []
        def menuMap = []
        def mnuPrf = getMnuPref()
        Sql sql
        log.debug("Process Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        log.debug(sql.useConnection.toString())
        sql.execute("Begin gukmenu.p_bld_prod_menu('MAG'); End;")
        sql.eachRow("select * from gutmenu,gubmodu,gubpage,gubobjs where gutmenu_value  = gubpage_code (+) AND " +
                " gubobjs_name = gutmenu_value AND gubobjs_ui_version IN ('A','B','C') and gubpage_gubmodu_code  = gubmodu_code (+) " +
                " order by gutmenu_seq_no", {
            def mnu = new Menu()
            def clnMenu = true
            if (it.gutmenu_objt_code == "MENU")
                menuMap.add(it.gutmenu_value)
            if ((it.gutmenu_objt_code == "FORM") && (!menuMap.contains(it.gutmenu_prior_obj)))
                clnMenu = false
            if (clnMenu) {
                mnu.formName = it.gutmenu_value
                mnu.name = it.gutmenu_value
                mnu.page = (it.gubobjs_ui_version == "B") ? it.gutmenu_value : it.gubpage_name
                //mnu.page = it.gubpage_name
                mnu.menu = getFormName(it.gubpage_name)
                if (it.gutmenu_desc != null)  {
                    mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;")
                    if (mnuPrf)
                        mnu.caption = mnu.caption + " (" + mnu.name + ")"
                }
                mnu.level = it.gutmenu_level
                mnu.type = it.gutmenu_objt_code
                mnu.parent = it.gutmenu_prior_obj
                mnu.url = it.gubmodu_url
                mnu.seq = it.gutmenu_seq_no
                mnu.uiVersion = (it.gubobjs_ui_version == "B") ? "banner8admin" : "bannerXEadmin"
                dataMap.add(mnu)
            }
        });
        log.debug("ProcessMenu executed" )
        sql.connection.close()
        return dataMap
    }

    /**
     * This is returns map of all personal menu items based on user access
     * @return List representation of personal menu objects that a user has access
     */
    def personalCombinedMenu() {
        def map = personalCombinedMenuMap()
        return map
    }

    /**
     * This is returns map of all personal items based on user access
     * @return Map of menu objects that a user has access
     */


    def personalCombinedMenuMap() {
        def dataMap = []
        def menuMap = []
        def mnuPrf = getMnuPref()
        Sql sql
        def parent
        log.debug("Personal Menu started")
        sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.execute("Begin gukmenu.p_bld_pers_menu('MAG'); End;")
        log.debug("After gukmenu.p_bld_pers_menu sql.execute" )
        sql.eachRow("select * from gutpmnu,gubmodu,gubpage,gubobjs where  substr(gutpmnu_value,6,length(gutpmnu_value))  = gubpage_code (+) AND " +
                " gubobjs_name = substr(gutpmnu_value,6,length(gutpmnu_value)) AND gubobjs_ui_version IN ('A','B','C') AND gubpage_gubmodu_code  = gubmodu_code (+) order by gutpmnu_seq_no", {

            def mnu = new Menu()
            def page = it.gutpmnu_value.split("\\|")[1]
            def type = it.gutpmnu_value.split("\\|")[0]

            def clnMenu = true
            if ("MENU" == type)
                menuMap.add(page)
            if (("FORM" == type) && (!menuMap.contains(page)))
                clnMenu = false
            //if (mnuPrf) {
            mnu.formName = it.gutpmnu_value.split("\\|")[1]
            mnu.name = page
            mnu.page = (it.gubobjs_ui_version == "B") ? page : it.gubpage_name
            //mnu.page = it.gubpage_name
            mnu.menu = getFormName(it.gubpage_name)
            if (it.gutpmnu_label != null)
                mnu.caption = it.gutpmnu_label.replaceAll(/\&/, "&amp;")

            if (mnuPrf)
                mnu.caption = mnu.caption + " (" + mnu.name + ")"

            mnu.level = it.gutpmnu_level
            mnu.type = it.gubobjs_objt_code
            mnu.parent = it.gubmodu_name
            mnu.url = it.gubmodu_url
            mnu.module = it.gubmodu_name
            mnu.seq = it.gutpmnu_seq_no
            mnu.uiVersion = (it.gubobjs_ui_version == "B") ? "banner8admin" : "bannerXEadmin"
            dataMap.add(mnu)
            //}
        });

        log.debug("Personal Menu executed" )
        sql.connection.close()
        return dataMap
    }
}
