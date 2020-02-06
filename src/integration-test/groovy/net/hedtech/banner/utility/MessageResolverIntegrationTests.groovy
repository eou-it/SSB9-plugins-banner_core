/*******************************************************************************
Copyright 2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.utility

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.i18n.LocaleContextHolder

@Integration
@Rollback
class MessageResolverIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        LocaleContextHolder.resetLocaleContext()
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testMessage() {
        def msg = MessageResolver.message("default.time.withSeconds.format")
        assertNotNull(msg)
        assertEquals(msg, "HH:mm:ss")

        msg = MessageResolver.message(null)
        assertEquals(msg, "")

        msg = MessageResolver.message("default.time.withSeconds.format", null, LocaleContextHolder.getLocale())
        assertEquals(msg, "HH:mm:ss")
    }
}
