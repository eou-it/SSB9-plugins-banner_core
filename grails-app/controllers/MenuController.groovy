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
  def list
  def mnuLabel = "Banner"
  private static final log = Logger.getLogger(getClass())
  def data = {
    def i = 0
    def menuType
    def mnuParams
    FormContext.set(["GUAGMNU"])
    log.debug("Menu Controller DATA fetch")

    if (request.parameterMap["type"] != null) {
      menuType = request.parameterMap["type"][0]
      mnuLabel = "My Banner"
      mnuParams = "type=Personal"
    }
    if (request.parameterMap["pageName"] != null)
      list = getCrumb((request.parameterMap["pageName"][0]), menuType)
    else
    if (request.parameterMap["menuName"] != null)
      list = getMenuList(request.parameterMap["menuName"][0], menuType)
    else
      list = getFirstList(menuType)
    render(contentType: "text/xml") {
      NavigationEntries {
        for (a in list) {
          i += 1
          NavigationEntryValueObject(id: i, menu: a.menu, form: a.formName, path: a.pageName + ".zul", name: a.formName, caption: a.caption, type: a.type, url: a.url, parent: a.parent, params: mnuParams)
        }
      }
    }
  }
  /**
   * Driver for banner menu
   */
  private def getMenu() {
    FormContext.set(["GUAGMNU"])
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
    FormContext.set(["GUAGMNU"])
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
    FormContext.set(["GUAGMNU"])
    log.debug("Menu Controller getmenu")
    if (menuType == null)
      mnuList = getMenu()
    else
      mnuList = getPersonalMenu()
    def childMenu = []
    for (a in mnuList) {
      if (a.level == 1) {
        a.menu = getParent(mnuList, a, mnuLabel)
        childMenu.add(a)
      }
    }
    return childMenu
  }
  /**
   * Returns menu itesm for a specified menu
   */
  private def getMenuList(String menuName, String menuType) {
    def mnuList
    FormContext.set(["GUAGMNU"])
    log.debug("Menu Controller getmenulist")
    if (menuType == null)
      mnuList = getMenu()
    else
      mnuList = getPersonalMenu()
    def childMenu = []
    for (a in mnuList) {
      if (a.parent == menuName) {
        a.menu = getParent(mnuList, a, mnuLabel)
        childMenu.add(a)
      }
    }
    return childMenu
  }

  /**
   * This method returns simgle navigational entry as breadcrumb
   */
  private def getCrumb(String pageName, String menuType) {
    def mnuList
    FormContext.set(["GUAGMNU"])
    log.debug("Menu Controller getcrumb")
    if (menuType == null)
      mnuList = getMenu()
    else
      mnuList = getPersonalMenu()
    def childMenu = []
    for (a in mnuList) {
      if (a.pageName == pageName) {
        a.menu = getParent(mnuList, a, mnuLabel)
        childMenu.add(a)
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
    def parentMnu
    def menuFName = mnu.formName
    def caption = mnu.caption
    if (mnu.type == "MENU") {
      parentChain = caption
    }
    for (i in 1..(mnu.level)) {
      for (a in map) {
        if (a.formName == menuFName) {
          parentMnu = a
          break;
        }
      }
      menuFName = parentMnu.parent
      for (b in map) {
        if (b.formName == menuFName) {
          caption = b.caption
          break;
        }
      }
      if (parentChain == null) parentChain = caption
      else
      if (menuFName != null) parentChain = caption + "/" + parentChain
    }
    return rootMenu + "/" + parentChain
  }

  /**
   * This method derives the menu parent structure
   */
  private def getParentChain(List map, Menu mnu, String rootMenu) {
    def parentChain
    def parentMnu
    def menuFName = mnu.formName
    def caption = mnu.caption
    if (mnu.type == "MENU") {
      parentChain = caption
    }
    for (i in 1..(mnu.level)) {
      for (a in map) {
        if (a.formName == menuFName) {
          parentMnu = a
          break;
        }
      }
      menuFName = parentMnu.parent
      for (b in map) {
        if (b.formName == menuFName) {
          caption = b.caption
          break;
        }
      }
      if (parentChain == null) parentChain = caption
      else
      if (menuFName != null) parentChain = caption + "/" + parentChain
    }
    return rootMenu + "/" + parentChain
  }

}