/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

/**
 * SelfService controller returns menu as XML format
 * Request parameters
 *  menuName current menu
 */
class SelfServiceMenuController {

    def selfServiceMenuService
    def mnuLabel = "Banner"
    private final log = Logger.getLogger(getClass())

    def data = {
        def menuType
        def mnuParams
        def list
        def currentMenu
        def menuName
        def menu

        if (request.parameterMap["menuName"] != null) {
            menuName = request.parameterMap["menuName"][0]
        }
        if (request.parameterMap["menu"] != null) {
            menu = request.parameterMap["menu"][0]
        }

        def pidm

        try {
            pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        }
        catch (Exception e) {
            //non logged in user
            pidm = null
        }

        list = getMenu(menuName, menu, pidm )

        def sw = new StringWriter()
        def xml = new groovy.xml.MarkupBuilder(sw)
        xml.NavigationEntries {
            list.each { a ->
                def pageName = a.pageName ? a.pageName : "null"
                NavigationEntryValueObject(id: a.seq, menu: a.menu, form: a.formName, path: pageName + ".zul", name: a.name, caption: a.caption, type: a.type, url: a.url, parent: a.parent, params: mnuParams, captionProperty: a.captionProperty, pageCaption: a.pageCaption)
            }
        }
        render(text: sw.toString(), contentType: "text/xml", encoding: "UTF-8")
    }


    /**
     * Driver for banner menu
     */
    private def getMenu( menuName, menuTrail, pidm ) {
        if (log.isDebugEnabled()) log.debug("Menu Controller getmenu")

        def currentMenu = menuName ? menuName : "Banner"
        currentMenu = pidm ? currentMenu + pidm : currentMenu

        if (session[currentMenu] == null) {
            session[currentMenu] = selfServiceMenuService.bannerMenu(menuName, menuTrail, pidm)
        }

        return session[currentMenu]
    }
}