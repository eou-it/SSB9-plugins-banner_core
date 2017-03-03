/* ****************************************************************************
Copyright 2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.security

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class BannerAccessDecisionVoterIntegrationTests extends BaseIntegrationTestCase {

    private SelfServiceBannerAuthenticationProvider provider
    def conn
    Sql sqlObj
    def dataSource
    def testUser
    public static final String PERSON_HOSWEB002 = 'HOSWEB002'
    def auth

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        Holders.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false
        conn = dataSource.getSsbConnection()
        sqlObj = new Sql(conn)
        provider = Holders.applicationContext.getBean("selfServiceBannerAuthenticationProvider")
        testUser = existingUser(PERSON_HOSWEB002, 123456)
        enableUser(sqlObj, testUser.pidm)
    }

    @After
    public void tearDown() {
        super.tearDown()

    }

    @Test
    void testExtractUrl() {
        def bannerAccessDecisionVoter = new BannerAccessDecisionVoter()
        auth = provider.authenticate(new TestAuthenticationRequest(testUser))
        bannerAccessDecisionVoter.isUserAuthorized("home")
    }

    private def existingUser(userId, newPin) {
        def existingUser = [name: userId]

        def testAuthenticationRequest = new TestAuthenticationRequest(existingUser)
        existingUser['pidm'] = provider.getPidm(testAuthenticationRequest, sqlObj)
        sqlObj.commit()
        sqlObj.call("{call gb_third_party_access.p_update(p_pidm=>${existingUser.pidm}, p_pin=>${newPin})}")
        sqlObj.commit()
        existingUser.pin = newPin
        return existingUser
    }

    private void enableUser(Sql db, pidm) {
        db.executeUpdate("update gobtpac set gobtpac_pin_disabled_ind='N' where gobtpac_pidm=$pidm")
        db.commit()
    }
}
