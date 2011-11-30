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

import groovy.sql.Sql
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication

class OptionMenuService {
    boolean transactional = false
    Sql sql
    def sessionFactory
    def grailsApplication                  // injected by Spring

    /**
     * This is returns list of all option menu items based on page id
     * @return List of option menu items
     */
    def optionMenuForPage(pageId) {

        def optionFormMenuList = []

        sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.eachRow("""
                     SELECT GUROPTM_SORT_SEQ, GUROPTM_NAME1_DESC||'['||GUROPTM_FORM_TO_BE_CALLED||']', GUROPTM_FORM_NAME,
                            GUROPTM_FORM_TO_BE_CALLED, GUBPAGE_NAME, GUBMODU_URL
                       FROM GUROPTM, GUBPAGE, GUBMODU
                      WHERE GUBPAGE_CODE = GUROPTM_FORM_TO_BE_CALLED
                        AND GUROPTM_FORM_NAME = (SELECT GUBPAGE_CODE
                                                 FROM GUBPAGE
                                                 WHERE GUBPAGE_NAME = ?
                                                )
                        AND GUROPTM_TRG_NAME = 'GOTO_FORM'
                        AND GUROPTM_TYPE_IND <> 'I'
                        AND GUROPTM_BLOCK_VALID is null
                        AND GUBPAGE_GUBMODU_CODE = GUBMODU_CODE (+)
                        ORDER BY GUROPTM_SORT_SEQ
                   """, [pageId]) {


            def optionMenu = new OptionMenu()
            optionMenu.seq = it[0]
            optionMenu.menuDesc = it[1]
            optionMenu.formName = it[2]
            optionMenu.calledFormName = it[3]
            optionMenu.pageName = it[4]
            optionMenu.url = it[5]

            optionFormMenuList.add(optionMenu)

        };

        optionFormMenuList
    }

     /**
     * This is returns list of all option menu items based on page id and block id
     * @return List of option menu items
     */
    def optionMenuForBlock(pageId, blockId) {

        ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        grailsApplication = (GrailsApplication) ctx.getBean("grailsApplication")

        def domainClass = grailsApplication.getArtefactByLogicalPropertyName("Domain", blockId.substring(0, blockId.indexOf("Block")))

        if (domainClass == null)
            return optionMenuForPage(pageId)

        def tableName = sessionFactory.getClassMetadata(domainClass?.getClazz())?.tableName.toUpperCase()

        if (tableName == null)
            return optionMenuForPage(pageId)

        //Start

        def optionBlockMenuList = []

        sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.eachRow("""
                     SELECT GUROPTM_SORT_SEQ, GUROPTM_NAME1_DESC||'['||GUROPTM_FORM_TO_BE_CALLED||']', GUROPTM_FORM_NAME,
                            GUROPTM_FORM_TO_BE_CALLED, GUBPAGE_NAME, GUBMODU_URL
                       FROM GUROPTM, GUBPAGE, GUBMODU
                      WHERE GUBPAGE_CODE = GUROPTM_FORM_TO_BE_CALLED
                        AND GUROPTM_FORM_NAME = (SELECT GUBPAGE_CODE
                                                 FROM GUBPAGE
                                                 WHERE GUBPAGE_NAME = ?
                                                )
                        AND GUROPTM_TRG_NAME = 'GOTO_FORM'
                        AND GUROPTM_TYPE_IND <> 'I'
                        AND GUROPTM_BLOCK_VALID = ?
                        AND GUBPAGE_GUBMODU_CODE = GUBMODU_CODE (+)
                        ORDER BY GUROPTM_SORT_SEQ
                   """, [pageId, tableName]) {

            def optionMenu = new OptionMenu()

            optionMenu.seq = it[0]
            optionMenu.menuDesc = it[1]
            optionMenu.formName = it[2]
            optionMenu.calledFormName = it[3]
            optionMenu.pageName = it[4]
            optionMenu.url = it[5]

            optionBlockMenuList.add(optionMenu)

        };

        if (optionBlockMenuList.size() == 0)
            optionMenuForPage(pageId)
            optionBlockMenuList  
    }
}