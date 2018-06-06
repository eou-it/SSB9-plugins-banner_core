/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.ui

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * Integration test for the SanitizeMarkdownTagLib class.
 **/
class SanitizeMarkdownTagLibIntegrationTests extends BaseIntegrationTestCase {
    def grailsApplication
    def taglib


    @Before
    void setUp(){
        formContext = ['GUAGMNU']
        super.setUp()
        taglib = grailsApplication.mainContext.getBean(SanitizeMarkdownTagLib.class.name)
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testTagLibWithHyperLinkText() {
        def thisTagArgs = [text:"A [link](http://ellucian.com)."]
        assertEquals '<p>A <a href="http://ellucian.com">link</a>.</p>',taglib.renderHtml(thisTagArgs).toString()
    }


    @Test
    void testTagLibWithHeading() {
        def thisTagArgs = [text:"# DemoText"]
        assertEquals '<h1>DemoText</h1>',taglib.renderHtml(thisTagArgs).toString()
    }


    @Test
    void testTagLibWithEmptyParam() {
        def thisTagArgs = [:]
        assertEquals '',taglib.renderHtml(thisTagArgs).toString()
    }


}
