/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.
 CONFIDENTIAL BUSINESS INFORMATION
 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.menu



import com.sungardhe.banner.menu.Menu
import groovy.sql.Sql
import oracle.jdbc.OracleTypes
import org.springframework.security.core.context.SecurityContextHolder
import com.sungardhe.banner.general.utility.MenuAndToolbarPreferenceService
import org.apache.log4j.Logger
import com.sungardhe.banner.security.FormContext

class MenuService {
  static transactional = true
  def menuAndToolbarPreferenceService
  def sessionFactory
  def personalDataMap = [:]
  private static final log = Logger.getLogger(getClass())


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
    def bannerMenu
    def dataMap = []
    Sql sql
    def mnuLabelPref = getMnuPref()
    FormContext.set(["GUAGMNU"])
    log.debug("Personal Menu started")
    log.debug("Before Connection Retrieval")
    sql = new Sql(sessionFactory.getCurrentSession().connection())
    log.debug("After Connection Retrieval")
    log.debug(sql.useConnection.toString())

    sql.execute("Begin gukmenu.p_bld_pers_menu; End;")
    log.debug("After gukmenu.p_bld_pers_menu sql.execute" )
    sql.eachRow("select * from gutpmnu,gubmodu,gubpage where  substr(gutpmnu_value,6,length(gutpmnu_value))  = gubpage_code (+) AND " +
            " gubpage_gubmodu_surrogate_id  = gubmodu_surrogate_id (+) order by gutpmnu_seq_no", {
      def mnu = new Menu()
      def str = it.gutpmnu_value.split("\\|")
      mnu.formName = str[1]
      mnu.pageName = it.gubpage_name
      if (mnuLabelPref == "N")
        mnu.caption = it.gutpmnu_label
      else
        mnu.caption = it.gutpmnu_label + "(" + mnu.formName + ")"
      mnu.level = it.gutpmnu_level
      mnu.type = str[0]
      mnu.module = it.gubmodu_name
      mnu.url = it.gubmodu_url
      personalDataMap.put(it.gutpmnu_seq_no, mnu)
      mnu.parent = setParent(mnu)
      dataMap.add(mnu)
    });

      log.debug("Personal Menu executed" )
    sql.connection.close()
    return dataMap
  }


  def setParent(Menu mnu) {
    String parent
    if (mnu.level == 1)
      return parent
    personalDataMap.each {
      if (it.value.level == (mnu.level - 1) && (it.value.type == "MENU")) {
        parent = it.value.formName
      }
    }
    return parent
  }
  /**
   * This is returns map of all menu items based on user access
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


  private def processMenu() {
    def bannerMenu
    def dataMap = []
    def addmenu
    Sql sql
    def i = 0
    FormContext.set(["GUAGMNU"])
    def mnuLabelPref = getMnuPref()
    log.debug("Process Menu started")
    log.debug("Before Connection Retrieval")
    sql = new Sql(sessionFactory.getCurrentSession().connection())
    log.debug("After Connection Retrieval")
    log.debug(sql.useConnection.toString())
    sql.execute("Begin gukmenu.p_bld_prod_menu; End;")
    log.debug("After gukmenu.p_bld_prod_menu sql.execute" )


    sql.eachRow("select * from gutmenu,gubmodu,gubpage where gutmenu_value  = gubpage_code (+) AND " +
            " gubpage_gubmodu_surrogate_id  = gubmodu_surrogate_id (+)", {
      def mnu = new Menu()


      mnu.formName = it.gutmenu_value
      mnu.pageName = it.gubpage_name
      if (it.gutmenu_desc != null)
        if (mnuLabelPref == "N")
          mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;")
        else
          mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;") + "(" + mnu.formName + ")"
      mnu.level = it.gutmenu_level
      mnu.type = it.gutmenu_objt_code
      mnu.parent = it.gutmenu_prior_obj
      mnu.module = it.gubmodu_name
      mnu.url = it.gubmodu_url

      //Added for horizon
      if (mnu.level == 1)
        if (mnu.formName == "*HORIZON")
          addmenu = "Y"
        else
          addmenu = "N"
      if (addmenu == "Y") {
        dataMap.add(mnu)
      }
    });
    log.debug("ProcessMenu executed" )
    sql.connection.close()
    return dataMap
  }

  private def getMnuPref() {
    //return "Y"
     def prefs = menuAndToolbarPreferenceService.fetchMenuAndToolbarPreference()
     return prefs.get(0).formnameDisplayIndicator
  }

  def searchMenu(String menuName) {
    def mnuList = bannerMenu()
    def childMenu = []
    for (a in mnuList) {
      if (a.formName.find(menuName) == menuName || a.caption.find(menuName) == menuName) {
        a.path = a.pageName + ".zul"
        childMenu.add(a)
      }
    }
    return childMenu
  }

}