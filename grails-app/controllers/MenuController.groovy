import com.sungardhe.banner.menu.Menu
import org.apache.log4j.Logger
import com.sungardhe.banner.security.FormContext

/**
 * Menu controller returns menu as XML format
 * Request parameters
 *  type Personal or banner menu
 *  pageName for returning single entry
 *  menuName current menu
 */
class MenuController {

    def menuService
    def mnuLabel = "Banner"
    private final log = Logger.getLogger(getClass())

    def data = {
        def menuType
        def mnuParams
        def list
        if (request.parameterMap["type"] != null) {
            menuType = request.parameterMap["type"][0]
            mnuLabel = "My Banner"
            mnuParams = "type=Personal"
        }
        if (request.parameterMap["pageName"] != null) {
            def seq
            if(request.parameterMap["seq"] != null)
               seq = request.parameterMap["seq"][0]
            list = getCrumb( (request.parameterMap["pageName"][0]), menuType, seq )
        }
        else
        if (request.parameterMap["menuName"] != null) {
            def seq1 = request.parameterMap["seq"][0]
            list = getMenuList(request.parameterMap["menuName"][0], menuType ,seq1 as int)
            session["menuCurrent"] = request.parameterMap["menuName"][0]
        }
        else
            list = getFirstList(menuType)
        render(contentType: "text/xml") {
        NavigationEntries {
        for (a in list) {
            NavigationEntryValueObject(id: a.seq, menu: a.menu, form: a.formName, path: a.pageName + ".zul", name: a.formName, caption: a.caption, type: a.type, url: a.url, parent: a.parent, params: mnuParams, captionProperty: a.captionProperty, pageCaption: a.pageCaption)
        }
      }
     }
    }
    /**
    * Driver for banner menu
    */
    private def getMenu() {
        def list
        log.debug("Menu Controller getmenu")
        if (session["menuList"] == null) {
            list = menuService.bannerMenu()
            session["menuList"] = list
        }
        else {
            list = session["menuList"]
        }
        return list
    }
    /**
    * Driver for personal menu
    */
    private def getPersonalMenu() {
        def list
        log.debug("Menu Controller getmenu")
        if (session["personalMenuList"] == null) {
            list = menuService.personalMenu()
            session["personalMenuList"] = list
        }
        else {
            list = session["personalMenuList"]
        }
        return list
    }
    /**
    * Returns first menu item for a specified menu
    */
    private def getFirstList(String menuType) {
        def mnuList
        log.debug("Menu Controller getmenu")
        if (menuType == null)
            mnuList = getMenu()
        else
            mnuList = getPersonalMenu()
        def childMenu = mnuList.findAll{a -> a.level == 1 }
        childMenu.each {a -> a.menu = getParent(mnuList, a, mnuLabel)}
        if (childMenu.size() == 0)  {
            Menu mnu = new Menu()
            childMenu.add(mnu)
        }
        return childMenu
    }
    /**
    * Returns menu itesm for a specified menu
    */
    private def getMenuList(def menuName, def menuType, int seq) {
        def mnuList
        def childMenu  =[]
        log.debug("Menu Controller getmenulist")
        if (menuType == null)
            mnuList = getMenu()
        else
            mnuList = getPersonalMenu()
        def level
        for (it in mnuList) {
            if ( level != null &&  it.level < level )
                break
            if (it.parent == menuName  && it.seq >= seq) {
                level = it.level
                it.menu = getParent(mnuList, it, mnuLabel)
                childMenu.add(it)
            }
        }
        return childMenu
    }

    /**
    * This method returns simgle navigational entry as breadcrumb
    */
    private def getCrumb(String pageName, String menuType, def seq) {
        def mnuList
        log.debug("Menu Controller getcrumb")
        if (menuType == null)
            mnuList = getMenu()
        else
            mnuList = getPersonalMenu()
        def childMenu
        if(seq){
            childMenu = mnuList.find{a ->
                (a.pageName == pageName && a.seq == seq as int)
            }
        }
        else {
            childMenu = mnuList.find{a -> a.pageName == pageName}
        }
        childMenu.each {a -> a.menu = getParent(mnuList, a, mnuLabel)}
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
}