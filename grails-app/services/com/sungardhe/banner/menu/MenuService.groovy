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

class MenuService {
  boolean transactional = false
  Sql sql
  def sessionFactory

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
    sql = new Sql(sessionFactory.getCurrentSession().connection())
    sql.execute("Begin gukmenu.p_bld_pers_menu; End;")
    sql.eachRow("select * from gutpmnu,gubmodu,gubpage where  substr(gutpmnu_value,6,length(gutpmnu_value))  = gubpage_code (+) AND " +
            " gubpage_gubmodu_surrogate_id  = gubmodu_surrogate_id (+) ", {
      def mnu = new Menu()
      def str = it.gutpmnu_value.split("\\|")
      mnu.formName = str[1]
      mnu.pageName = it.gubpage_name
      mnu.caption = it.gutpmnu_label
      mnu.level = it.gutpmnu_level
      mnu.type = str[0]
      mnu.module = it.gubmodu_name
      mnu.url = it.gubmodu_url
      dataMap.add(mnu)
    });

    return dataMap
  }

  /**
   * This is returns map of all menu items based on user access
   * @param pageName
   * @return Form name
   */
  def getFormName(String pageName) {
    def formName
    sql = new Sql(sessionFactory.getCurrentSession().connection())
    sql.eachRow("select * from gubpage where gubpage_name = ?", [pageName]) {
      formName = it.gubpage_code
    }
    return formName
  }


  private def processMenu() {
    def bannerMenu
    def dataMap = []
    def i = 0
    sql = new Sql(sessionFactory.getCurrentSession().connection())
    sql.execute("Begin gukmenu.p_bld_prod_menu; End;")
    sql.eachRow("select * from gutmenu,gubmodu,gubpage where  gutmenu_value  = gubpage_code (+) AND " +
            " gubpage_gubmodu_surrogate_id  = gubmodu_surrogate_id (+)", {
      def mnu = new Menu()

      mnu.formName = it.gutmenu_value
      mnu.pageName = it.gubpage_name
      if (it.gutmenu_desc != null)
        mnu.caption = it.gutmenu_desc.replaceAll(/\&/, "&amp;")
      mnu.level = it.gutmenu_level
      mnu.type = it.gutmenu_objt_code
      mnu.parent = it.gutmenu_prior_obj
      mnu.module = it.gubmodu_name
      mnu.url = it.gubmodu_url
      dataMap.add(mnu)
    }
    );
    return dataMap
  }

}