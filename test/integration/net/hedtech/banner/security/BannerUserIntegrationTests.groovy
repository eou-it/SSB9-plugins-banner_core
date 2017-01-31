/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.core.Authentication
import org.springframework.util.SerializationUtils

class BannerUserIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider
    def conn
    def grailsApplication
    public static final String PERSON = 'HOSWEB002'
    def testUser
    Sql sqlObj

    @Before
    public void setUp() {
        conn = dataSource.getSsbConnection()
        sqlObj = new Sql(conn)
        selfServiceBannerAuthenticationProvider = Holders.applicationContext.getBean("selfServiceBannerAuthenticationProvider")
        testUser = existingUser(PERSON, 123456)
        enableUser (sqlObj, testUser.pidm)
    }

    @After
    public void tearDown() {
        sqlObj?.close()
        conn?.close()
    }

    @Test
    void testSerializationForBannerUser() {
        BannerUser bannerUser= selfServiceBannerAuthenticationProvider.authenticate( new TestBannerUserAuthenticationRequest( testUser ) ).principal
        SerializationUtils.serialize(bannerUser)
    }

    private def existingUser (userId, newPin) {
        def existingUser = [ name: userId]

        def testAuthenticationRequest = new TestBannerUserAuthenticationRequest(existingUser)
        existingUser['pidm'] = selfServiceBannerAuthenticationProvider.getPidm(testAuthenticationRequest, sqlObj )
        sqlObj.commit()
        sqlObj.call ("{call gb_third_party_access.p_update(p_pidm=>${existingUser.pidm}, p_pin=>${newPin})}")
        sqlObj.commit()
        existingUser.pin = newPin
        return existingUser
    }
    private void enableUser(Sql db, pidm) {
           db.executeUpdate("update gobtpac set gobtpac_pin_disabled_ind='N' where gobtpac_pidm=$pidm")
           db.commit()
    }

}
class TestBannerUserAuthenticationRequest implements Authentication {

    def user

    public TestBannerUserAuthenticationRequest( user ) {
        this.user = user
    }

    public Collection getAuthorities() { [] }
    public Object getCredentials() { user.pin }
    public Object getDetails() { user }
    public Object getPrincipal() { user }
    public boolean isAuthenticated() { false }
    public void setAuthenticated( boolean b ) { }
    public String getName() { user.name }
    public Object getPidm() { user.pidm }
    public Object getOracleUserName() { user.oracleUserName }
}
