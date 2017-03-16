/*******************************************************************************
Copyright 2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.utility

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.i18n.LocaleContextHolder

/**
 * This integration test class is used to test all methods in MessageResolver.
 *
 * Created by arunu on 11/7/2016.
 */
class MessageResolverIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
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
