/*******************************************************************************
Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.utility

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import net.hedtech.banner.utility.MessageResolver

class MessageResolverTests extends BaseIntegrationTestCase {

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
public void testMessageResolver(){
        def messageString="";
        messageString=MessageResolver.message("default.time.withSeconds.format")
        assertEquals "HH:mm:ss", messageString
}

}
