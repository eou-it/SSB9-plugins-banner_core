import com.sungardhe.banner.menu.Menu

class MenuController {

	/**
	 * Returns menu object
	 */
    def menuService
    def list
	def data = {
      def i =0

      if (request.parameterMap["menuName"] != null)
          list = getMenuList(request.parameterMap["menuName"][0])
      else
          list = getFirstList()
      render(contentType:"text/xml"){
          NavigationEntries{
            for(a in list){
              i +=1
              NavigationEntryValueObject(  id :i,menu :a.menu, form :a.formName, path : a.pageName + ".zul" , name : a.formName, caption : a.caption , type : a.type,   url :a.url, parent :a.parent)
            }
          }
      }
    }

      private def getMenu () {
        if (session["menuList"] ==  null) {
          list = menuService.bannerMenu ()
          session["menuList"] = list
        }
        else
        {
          list = session["menuList"]
        }
        return list
      }

      private def getFirstList() {
        def mnuList = getMenu ()
        def childMenu =[]
        for (a in mnuList) {
             if (a.level == 1 ) {
                a.menu = getParent(mnuList,a)
                childMenu.add(a)
             }
           }
        return childMenu
      }

      private def getMenuList(String menuName) {

        def mnuList = getMenu ()
        def childMenu =[]
        for (a in mnuList) {
             if (a.parent == menuName ) {
                a.menu = getParent(mnuList,a)
                
                childMenu.add(a)
             }
           }
        return childMenu
      }


      private def getParent(List map, Menu mnu) {
        def parentChain
        def parentMnu
        def menuFName = mnu.formName
        def caption = mnu.caption
        if (mnu.type == "MENU") {
          parentChain = menuFName
 	    parentChain = caption
	    }
          for (i in 1..(mnu.level)) {
            for (a in map){
              if (a.formName == menuFName)  {
                parentMnu = a
                break;
              }
            }
          menuFName = parentMnu.parent
          caption = parentMnu.caption
          if (parentChain == null) parentChain = caption
          else
          if (menuFName != null) parentChain = caption + "/" + parentChain
        }
        return "Banner" + "/" + parentChain
      }
}
