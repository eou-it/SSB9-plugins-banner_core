/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.
 CONFIDENTIAL BUSINESS INFORMATION
 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
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
                            GUROPTM_FORM_TO_BE_CALLED, GUBPAGE_NAME
                       FROM GUROPTM, GUBPAGE
                      WHERE GUBPAGE_CODE = GUROPTM_FORM_TO_BE_CALLED
                        AND GUROPTM_FORM_NAME = (SELECT GUBPAGE_CODE
                                                 FROM GUBPAGE
                                                 WHERE GUBPAGE_NAME = ?
                                                )
                        AND GUROPTM_TRG_NAME = 'GOTO_FORM'
                        AND GUROPTM_TYPE_IND <> 'I'
                        AND GUROPTM_BLOCK_VALID is null
                        ORDER BY GUROPTM_SORT_SEQ
                   """, [pageId]) {


            def optionMenu = new OptionMenu()
            optionMenu.seq = it[0]
            optionMenu.menuDesc = it[1]
            optionMenu.formName = it[2]
            optionMenu.calledFormName = it[3]
            optionMenu.pageName = it[4]

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
                            GUROPTM_FORM_TO_BE_CALLED, GUBPAGE_NAME
                       FROM GUROPTM, GUBPAGE
                      WHERE GUBPAGE_CODE = GUROPTM_FORM_TO_BE_CALLED
                        AND GUROPTM_FORM_NAME = (SELECT GUBPAGE_CODE
                                                 FROM GUBPAGE
                                                 WHERE GUBPAGE_NAME = ?
                                                )
                        AND GUROPTM_TRG_NAME = 'GOTO_FORM'
                        AND GUROPTM_TYPE_IND <> 'I'
                        AND GUROPTM_BLOCK_VALID = ?
                        ORDER BY GUROPTM_SORT_SEQ
                   """, [pageId, tableName]) {

            def optionMenu = new OptionMenu()

            optionMenu.seq = it[0]
            optionMenu.menuDesc = it[1]
            optionMenu.formName = it[2]
            optionMenu.calledFormName = it[3]
            optionMenu.pageName = it[4]

            optionBlockMenuList.add(optionMenu)

        };

        if (optionBlockMenuList.size() == 0)
            optionMenuForPage(pageId)
            optionBlockMenuList  
    }
}