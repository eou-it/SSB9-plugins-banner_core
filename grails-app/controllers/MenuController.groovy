
class MenuController {

	/**
	 * Returns menu object
	 */
    def menuService
	def data = {
      def list
      def i =0

      if (request.parameterMap["menuName"] != null)
          list = getMenuList(request.parameterMap["menuName"][0])
      else
          list = getAllMenu()
      render(contentType:"text/xml"){
          NavigationEntries{
            for(a in list){
              i +=1
              NavigationEntryValueObject(  id :i,menu :a.menu, form :a.formName, path : a.pageName , name : a.pageName, caption : a.caption , type : a.type,   url :a.url, parent :a.parent)
            }
          }
      }
    }

      private def getAllMenu () {
        def list = menuService.bannerMenu ()
        return list
      }


      private def getMenuList(String menuName) {

        def list = menuService.bannerMenu ()
        def childMenu =[]
        def maxLevel = 0
        def currLevel = 0
        def tempList = []
        def tempList1 = []
        for (a in list) {
             if (a.parent == menuName ) {
                childMenu.add(a)
                currLevel = a.level + 1
                if ( a.type == "MENU")
                  tempList.add(a.formName)
             }
             if (a.level > maxLevel)
              maxLevel = a.level
           }

        for (i in (currLevel..maxLevel)) {
           for (a in list) {
             if (a.parent in tempList) {
               childMenu.add(a)
               if (a.type == "MENU")  {
                 tempList1.add (a.formName)
               }
             }
           }
           if (tempList1.size() > 0) {
              tempList = tempList1
              tempList1 = []
           }
           else
              break;
        }
        return childMenu
      }
}
