package com.sungardhe.banner.menu
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

import org.apache.log4j.Logger

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

        if (request.parameterMap["menuName"] != null) {
            menuName = request.parameterMap["menuName"][0]
        }

        list = getMenu(menuName)

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

    private def getMenu(def menuName) {
        def list
        if (log.isDebugEnabled()) log.debug("Menu Controller getmenu")

        def currentMenu = menuName ? menuName : "Banner"

        if (session[currentMenu] == null) {
            list = selfServiceMenuService.bannerMenu(menuName)
            session[currentMenu] = list
        }
        else {
            list = session[currentMenu]
        }
        return list
    }

}