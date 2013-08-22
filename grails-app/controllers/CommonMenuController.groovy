/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
import org.springframework.security.core.context.SecurityContextHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.converters.JSON
import net.hedtech.banner.menu.Menu
import org.apache.log4j.Logger

class CommonMenuController {
    def menuService
    def selfServiceMenuService
    def personalPreferenceService
    private final log = Logger.getLogger(getClass())

    static final String BANNER_INB_URL = "bannerInbUrl"
    static final String BANNER_TITLE = "Banner"
    static final String MENU_TYPE_BANNER = "Banner"
    static final String MENU_TYPE_PERSONAL = "Personal"
    static final String MENU_TYPE_SELF_SERVICE = "SelfService"
    static final String MY_BANNER_TITLE = "My Banner"
    static final String BANNER_SELF_SERVICE_TITLE = "Banner Self-Service"
    static final String PERSONAL_COMBINED_MENU_LIST = "personalCombinedMenuList"
    static final String COMBINED_MENU_LIST = "combinedMenuList"


    def data = {
        if(request.parameterMap["q"]){
            search()
        } else if(request.parameterMap["s"]){
            getMenuStructure()
        } else {
            list()
        }
    }

    def getMenuStructure = {

        def path
        def subMenu
        def nodes = []
        def finalList = []
        def finalMenu

        path = request.parameterMap["s"][0]
        nodes = path.split('/')
        def root = nodes[0]
        nodes.each { a ->

            if(a == BANNER_TITLE){
                subMenu = getSubMenuData(root, root, root)

            } else {
                subMenu = getSubMenuData(a, root, a)
            }

            finalList.add(subMenu)
        }

        subMenu = [ name:"root", caption:"root", items: finalList ]

        finalMenu = [ data: subMenu ]
        // Support JSON-P callback
        if( params.callback ) {
            render text: "${params.callback} && ${params.callback}(${finalMenu as JSON});", contentType: "text/javascript"
        } else {
            render finalMenu as JSON
        }
    }

    def list = {
        def mnuName
        def mnuType
        def caption
        def subMenu
        def finalMenu

        if (request.parameterMap["menu"])
            mnuName = request.parameterMap["menu"][0]

        if(request.parameterMap["type"])
            mnuType = request.parameterMap["type"][0]

        if(request.parameterMap["caption"])
            caption = request.parameterMap["caption"][0]


        subMenu = getSubMenuData(mnuName, mnuType, caption)
        finalMenu = [ data: subMenu ]

        // Support JSON-P callback
        if( params.callback ) {
            render text: "${params.callback} && ${params.callback}(${finalMenu as JSON});", contentType: "text/javascript"
        } else {
            render finalMenu as JSON
        }


    }
    private def getSubMenuData(def mnuName,def mnuType,def caption ){

        def subMenu
        def adminList
        def personalList
        def selfServiceList

        def finalList = []

        if (mnuName){

            if (mnuType == MENU_TYPE_BANNER){

                if (mnuName == BANNER_TITLE)
                    adminList = getMenuData()
                else
                    adminList = getMenuData(mnuName)

                finalList = adminList

            } else if (mnuType == MENU_TYPE_PERSONAL){

                if (mnuName == MY_BANNER_TITLE)
                    personalList = getPersonalMenuData(null)
                else
                    personalList = getPersonalMenuData(mnuName)

                finalList = personalList
            } else {

                if (mnuName == BANNER_SELF_SERVICE_TITLE)
                    selfServiceList = getSelfServiceMenuData(null)
                else
                    selfServiceList = getSelfServiceMenuData(mnuName)

                finalList = selfServiceList
            }

            subMenu = [name:mnuName,caption:caption , items: finalList]

        } else {
            finalList =  rootMenu()
            subMenu = [ name:"root", caption:"root", items: finalList ]
        }

        return subMenu
    }

    def rootMenu = {
        def finalMenu
        def subMenu
        def adminMenu
        def personalMenu
        def selfServiceMenu
        def adminList
        def personalList
        def selfServiceList
        def finalList = []

        adminList = getMenuData()
        //selfServiceList = getSelfServiceMenuData()
        selfServiceList = []
        personalList = getPersonalMenuData()
        adminMenu = [ name:BANNER_TITLE, caption:BANNER_TITLE, page:BANNER_TITLE ,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_BANNER+"&menu="+BANNER_TITLE+"&caption="+BANNER_TITLE,type: "MENU",items: null,menu:BANNER_TITLE]
        personalMenu = [ name:MY_BANNER_TITLE, caption:MY_BANNER_TITLE, page:MY_BANNER_TITLE ,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_PERSONAL+"&menu="+MY_BANNER_TITLE+"&caption="+MY_BANNER_TITLE,type: "MENU",items: null,menu:MY_BANNER_TITLE]
        selfServiceMenu = [ name:BANNER_SELF_SERVICE_TITLE, caption:BANNER_SELF_SERVICE_TITLE, page:BANNER_SELF_SERVICE_TITLE,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_SELF_SERVICE+"&menu="+BANNER_SELF_SERVICE_TITLE+"&caption="+BANNER_SELF_SERVICE_TITLE,type: "MENU", items: null,menu:BANNER_SELF_SERVICE_TITLE ]
        if (adminList.size() != 0)
            finalList.add(adminMenu)

        if (personalList.size() != 0)
            finalList.add(personalMenu)

        if(selfServiceList.size() != 0)
            finalList.add(selfServiceMenu)

        return finalList
    }

    def search = {

        def subMenu
        def finalMenu
        def adminList
        def selfServiceList
        def finalList = []
        def searchVal

        if(request.parameterMap["q"])
            searchVal = request.parameterMap["q"][0]
        if(searchVal){
            adminList = getAdminMenuSearchResults(searchVal)
            //selfServiceList = getSelfServiceMenuSearchResults(searchVal)
            finalList.addAll(adminList)
            //finalList.addAll(selfServiceList)
        }
        subMenu = [ name:"root", caption:"root", items: finalList ]

        finalMenu = [ data: subMenu ]
        // Support JSON-P callback
        if( params.callback ) {
            render text: "${params.callback} && ${params.callback}(${finalMenu as JSON});", contentType: "text/javascript"
        } else {
            render finalMenu as JSON
        }
    }

    private def getMenuData(def mnuName){
        def menuType
        def mnuParams
        def list
        def finalList = []

        if (mnuName != null) {
            list = getMenuList(mnuName)
            list.each {it -> it.menu = getParent(getMenu(),it,BANNER_TITLE)}

            list.each {a ->
                if (a.type == "MENU")
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_BANNER+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)

                if (a.type == "FORM" ){
                    if (a.uiVersion =="banner8admin")
                        finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getBannerInbUrl() + "?otherParams=launch_form="+a.page+"+ban_args={{params}}+ban_mode=xe",type: "PAGE",menu:a.menu)
                    else
                        finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: a.url +"banner.zul?page="+a.page + "&pageName="+ a.caption +"&global_variables={{params}}",type: "PAGE",menu:a.menu)

                }
            }

        }
        else {
            list = getFirstLevelMenuList()
            list.each {it -> it.menu = getParent(getMenu(),it, BANNER_TITLE)}

            list.each {a ->
                if (a.type == "MENU")
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_BANNER+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)

                if (a.type == "FORM" ){
                    if (a.uiVersion =="banner8admin")
                        finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getBannerInbUrl() + "?otherParams=launch_form="+a.page+"+ban_args={{params}}+ban_mode=xe",type: "PAGE",menu:a.menu)
                    else
                        finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: a.url +"banner.zul?page="+a.page+"&global_variables={{params}}",type: "PAGE",menu:a.menu)

                }
            }

        }
        return finalList
    }

    /**
     * Driver for banner menu
     */
    private def getMenu() {
        def list

        log.debug("Menu Controller getmenu")
        if (session[COMBINED_MENU_LIST] == null) {
            list = menuService.bannerCombinedMenu()
            session[COMBINED_MENU_LIST] = list
        }
        else {
            list = session[COMBINED_MENU_LIST]
        }
        return list
    }


    /**
     * Returns first menu item for a specified menu
     */
    private def getFirstLevelMenuList() {
        def mnuList
        def childrenList =[]
        log.debug("Menu Controller getmenu")
        mnuList = getMenu()
        def childMenu = mnuList.findAll{a -> a.level == 1 }
        if (childMenu.size() == 0)  {
            Menu mnu = new Menu()
            childMenu.add(mnu)
        }
        return childMenu
    }
    /**
     * Returns menu itesm for a specified menu
     */
    private def getMenuList(def menuName) {
        def mnuList
        def childMenu  =[]
        log.debug("Menu Controller getmenulist")
        mnuList = getMenu()

        for (it in mnuList) {
            if(it.name == menuName){
                childMenu = getChildren(mnuList, it)
                break;
            }
        }
        return childMenu
    }
    /**
     * This method derives the menu parent structure
     */
    private def getParent(List map, Menu mnu, String rootMenu) {
        def parentChain
        def level = mnu.level
        def temp = map.findAll{ it -> it.seq <= mnu.seq }
        temp.reverseEach {
            if (  it.level < level )  {
                level = it.level
                if (parentChain == null)
                    parentChain = it.formName
                else
                if (it.caption  != null)
                    parentChain = it.formName + "/" + parentChain
            }
        }
        if (parentChain != null)
            return rootMenu + "/" + parentChain
        else
            return rootMenu
    }
    /**
     * This method derives the child menu structure
     */
    private def getChildren(List map, Menu mnu) {
        def children
        def temp = map.findAll{ it -> it.seq > mnu.seq && it.parent == mnu.name}
        return temp
    }
    /**
     * Driver for personal menu
     */
    private def getPersonalMenu() {
        def list
        log.debug("Menu Controller getmenu")
        def pidm
        try {
            pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        }
        catch (Exception e) {
            pidm = null
            log.debug("Non logged in user.")
        }

        def personalMenuList = pidm ? PERSONAL_COMBINED_MENU_LIST + pidm : PERSONAL_COMBINED_MENU_LIST
        if (session[personalMenuList] == null) {
            list = menuService.personalCombinedMenu()
            session[personalMenuList] = list
        }
        else {
            list = session[personalMenuList]
        }
        return list
    }

    /**
     * Returns first menu item for a specified menu
     */
    private def getFirstLevelPersonalMenuList() {
        def mnuList
        log.debug("Menu Controller getmenulist")
        mnuList = getPersonalMenu()
        def childMenu = mnuList.findAll{a -> a.level == 1 }
        if (childMenu.size() == 0)  {
            Menu mnu = new Menu()
            childMenu.add(mnu)
        }
        return childMenu
    }
    /**
     * Returns menu itesm for a specified menu
     */
    private def getPersonalMenuList(def menuName) {
        def mnuList
        def childMenu  =[]
        log.debug("Menu Controller getmenulist")
        mnuList = getPersonalMenu()

        for (it in mnuList) {
            if(it.name == menuName){
                childMenu = getChildren(mnuList, it)
                break;
            }
        }
        return childMenu
    }
    private def getPersonalMenuData(def mnuName){
        def finalList = []
        def list
        if (mnuName != null) {
            list = getPersonalMenuList(mnuName)
            list.each {it -> it.menu = getParent(getPersonalMenu(),it,MY_BANNER_TITLE)}

            list.each {a ->
                if (a.type == "MENU")
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_PERSONAL+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)

                if (a.type == "FORM" ){
                    if (a.uiVersion =="banner8admin")
                        finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getBannerInbUrl() + "?otherParams=launch_form="+a.page+"+ban_args={{params}}+ban_mode=xe",type: "PAGE",menu:a.menu)
                    else
                        finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: a.url +"banner.zul?page="+a.page + "&pageName="+ a.caption + "&global_variables={{params}}",type: "PAGE",menu:a.menu)

                }
            }
        }
        else {
            list = getFirstLevelPersonalMenuList()
            list.each {it -> it.menu = getParent(getPersonalMenu(),it,MY_BANNER_TITLE)}

            list.each {a ->
                if (a.type == "MENU")
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_PERSONAL+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)

                if (a.type == "FORM" ){
                    if (a.uiVersion =="banner8admin")
                        finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getBannerInbUrl() + "?otherParams=launch_form="+a.page+"+ban_args={{params}}+ban_mode=xe",type: "PAGE",menu:a.menu)
                    else
                        finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: a.url +"banner.zul?page="+a.page + "&pageName="+ a.caption +"&global_variables={{params}}",type: "PAGE",menu:a.menu)

                }
            }
        }
        return finalList
    }
    /**
     * Driver for banner menu
     */
    private def getSelfServiceMenu( menuName, menuTrail, pidm ) {
        if (log.isDebugEnabled()) log.debug("Menu Controller getmenu")

        def currentMenu = menuName ? menuName : BANNER_SELF_SERVICE_TITLE
        currentMenu = pidm ? currentMenu + pidm : currentMenu
        if (session[currentMenu] == null) {
            session[currentMenu] = selfServiceMenuService.combinedMenu(menuName, menuTrail, pidm)
        }
        return session[currentMenu]
    }


    private def getSelfServiceMenuData(def mnuName){
        def list
        def menuName
        def menu
        def finalList = []

        if (mnuName != null) {
            menuName = mnuName
        }
        def pidm

        try {
            pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) log.debug("Non User has logged in")
            pidm = null
        }

        list = getSelfServiceMenu(menuName, menu, pidm)

        list.each {a ->
            if (a.type == "MENU"){
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() + "/commonMenu?type="+MENU_TYPE_SELF_SERVICE+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)
            }  else {
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url:a.url,type: "PAGE",menu:a.menu)
            }

        }

        return finalList
    }


    private def getAdminMenuSearchResults(searchVal){

        def subMenu
        def finalMenu
        def finalList = []

        def list = menuService.gotoCombinedMenu(searchVal)

        list.each {it -> it.menu = getParent(getMenu(),it,BANNER_TITLE)}

        list.each {a ->
            if (a.type == "MENU")
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() +"/commonMenu?type="+MENU_TYPE_BANNER+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)

            if (a.type == "FORM" ){
                if (a.uiVersion =="banner8admin")
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getBannerInbUrl() + "?otherParams=launch_form="+a.page+"+ban_args={{params}}+ban_mode=xe",type: "PAGE",menu:a.menu)
                else
                    finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: a.url +"banner.zul?page="+a.page + "&pageName="+ a.caption +"&global_variables={{params}}",type: "PAGE",menu:a.menu)

            }
        }
        return finalList
    }

    private def getSelfServiceMenuSearchResults(searchVal){
        def subMenu
        def finalMenu
        def finalList = []
        def pidm

        try {
            pidm = SecurityContextHolder?.context?.authentication?.principal?.pidm
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) log.debug("Non User has logged in")
            pidm = null
        }
        def list = selfServiceMenuService.gotoCombinedMenu(searchVal, pidm)
        list.each {a ->
            if (a.type == "MENU"){
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url: getServerURL() + "/commonMenu?type="+MENU_TYPE_SELF_SERVICE+"&menu="+a.name+"&caption="+a.caption,type: "MENU",menu:a.menu)
            }  else {
                finalList.add(name:a.name,page:a.page,caption:a.caption,parent:a.uiVersion,url:a.url,type: "PAGE",menu:a.menu)
            }
        }

        return finalList
    }

    def getServerURL() {
        boolean includePort = true
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = (new org.springframework.security.web.PortResolverImpl()).getServerPort(request)
        String contextPath = request.getContextPath();
        boolean inHttp = "http".equals(scheme.toLowerCase());
        boolean inHttps = "https".equals(scheme.toLowerCase());

        if (inHttp && (serverPort == 80)) {
            includePort = false;
        } else if (inHttps && (serverPort == 443)) {
            includePort = false;
        }
        String redirectUrl = scheme + "://" + serverName + ((includePort) ? (":" + serverPort) : "") + contextPath;
        return redirectUrl
    }

    def getBannerInbUrl(){
        def bannerInbUrl

        if(!servletContext.getAttribute("bannerInbUrl")){

            if (!isSsbEnabled()){
                bannerInbUrl = personalPreferenceService.fetchPersonalPreference("MAGELLAN","SERVER_DESIGNATION","INB")[0]
                servletContext.setAttribute("bannerInbUrl", bannerInbUrl.value)
            }
        }

        bannerInbUrl = servletContext.getAttribute("bannerInbUrl")

        return bannerInbUrl
    }

    private def isSsbEnabled() {
        ConfigurationHolder.config.ssbEnabled instanceof Boolean ? ConfigurationHolder.config.ssbEnabled : false
    }

}
