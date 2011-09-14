/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package com.sungardhe.banner.menu

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql

class MenuServiceIntegrationTests extends BaseIntegrationTestCase {

    def menuService

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        dataSetup()
    }

    void testBannerMenu() {
        def map
        map = menuService.bannerMenu()
        assertNotNull map

        def mnu = map.find {it -> it.formName == "SCACRSE"}

        assertNotNull mnu
        assertNotNull mnu.url
        assertNotNull mnu.caption
        assert mnu.formName == "SCACRSE"
        assert mnu.pageName == "basicCourseInformation"
    }

    void testPersonalMenu() {
        String mnu = menuService.personalMenu()
        assertNotNull mnu

    }

    void testGotoMenu() {
        String mnu = menuService.gotoMenu('SCA')
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