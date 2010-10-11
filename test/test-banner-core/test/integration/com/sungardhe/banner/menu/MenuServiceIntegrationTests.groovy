/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.menu

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql

class MenuServiceIntegrationTests extends BaseIntegrationTestCase {

  def menuService

  protected void setUp() {
    formContext = ['SCACRSE']
    super.setUp()
    dataSetup()
  }

  void testBannerMenu() {
    def map
    map = menuService.bannerMenu()
    assertNotNull map
    def mnu = new Menu()
    for (i in 1..(map.size())) {
      if (map.get(i).formName == "SCACRSE") {
        mnu = map.get(i)
        break;
      }
    }
    assertNotNull mnu.url
    assertNotNull mnu.caption
    assert mnu.formName == "SCACRSE"
    assert mnu.pageName == "basicCourseInformation"
  }

  void testPersonalMenu() {
    String mnu = menuService.personalMenu()
    assertNotNull mnu

  }


  void testGetFormName() {
    def pageName
    pageName = menuService.getFormName("basicCourseInformation")
    assertNotNull pageName
    assert pageName == "SCACRSE"
  }

  private def dataSetup() {
    def sql = new Sql(sessionFactory.getCurrentSession().connection())
    def courseName
    sql.eachRow("select * from gurmenu where gurmenu_user_id = user and gurmenu_name = '*PERSONAL' and GURMENU_OBJ_NAME='SCACRSE'", {courseName = it.GURMENU_OBJ_NAME })
    if (courseName != 'SCACRSE')
      sql.executeInsert("Insert into gurmenu ( GURMENU_NAME,GURMENU_OBJ_NAME, GURMENU_SORT_SEQ, GURMENU_USER_ID, GURMENU_ACTIVITY_DATE, GURMENU_DESC)  VALUES ('*PERSONAL','SCACRSE',1,user,sysdate,'Basic Course Information')")

  }
}