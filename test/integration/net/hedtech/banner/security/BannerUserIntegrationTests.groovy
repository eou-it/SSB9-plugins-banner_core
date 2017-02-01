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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.util.SerializationUtils

class BannerUserIntegrationTests extends BaseIntegrationTestCase {

    def selfServiceBannerAuthenticationProvider
    def conn
    public static final String PERSON = 'HOSWEB002'
    Sql sqlObj
    def PERSON_PIDM
    def PERSON_PASSWORD= 111111

    @Before
    public void setUp() {
        conn = dataSource.getSsbConnection()
        sqlObj = new Sql(conn)
        PERSON_PIDM =  getPidmBySpridenId(PERSON)
        existingUser(PERSON_PIDM,PERSON_PASSWORD)
        enableUser (PERSON_PIDM)
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        sqlObj?.close()
        conn?.close()
    }

    @Test
    void testSerializationForBannerUser() {
        BannerUser bannerUser= selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken(PERSON, PERSON_PASSWORD) ).principal
        SerializationUtils.serialize(bannerUser)
    }

    private def existingUser (pidm, newPin) {
        sqlObj.call ("{call gb_third_party_access.p_update(p_pidm=>${pidm}, p_pin=>${newPin})}")
        sqlObj.commit()
    }

    private void enableUser(pidm) {
        sqlObj.executeUpdate("update gobtpac set gobtpac_pin_disabled_ind='N' where gobtpac_pidm=$pidm")
        sqlObj.commit()
    }

    private def getPidmBySpridenId(def spridenId) {
        def query = "SELECT SPRIDEN_PIDM pidm FROM SPRIDEN WHERE SPRIDEN_ID=$spridenId"
        def pidmValue = sqlObj?.firstRow(query)?.pidm
        pidmValue
    }

}

