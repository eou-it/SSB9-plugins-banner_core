/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase

import javax.sql.DataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication


import java.sql.SQLException

/**
 * Integration test for the self service Banner authentication provider.
 **/
class ChangeExpiredPasswordIntegrationTests extends BaseIntegrationTestCase {

    def resetPasswordService;
    def sql
    def selfServiceBannerAuthenticationProvider
    def conn
    Authentication auth
    public static final String PERSON_HOSWEB002 = 'HOSWEB002'
    def PERSON_HOSWEB002_PIDM = 32530
    GroovyRowResult row
    int minLength
    int maxLength
    String pinResetFormat
    def dataSource
    DataSource underlyingDataSource
    DataSource underlyingSsbDataSource
    static final String GUBPPRF_QUERY = "select GUBPPRF_MIN_LENGTH,GUBPPRF_MAX_LENGTH,GUBPPRF_NUM_IND,GUBPPRF_CHAR_IND from GUBPPRF"

    @Before
    public void setUp() {

        Holders.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false
        conn = dataSource.getSsbConnection()
        sql = new Sql(conn)
        row = sql.firstRow(GUBPPRF_QUERY)
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        sql?.rollback();
        sql?.close()
    }

    @Test
    void testOldPasswordSuccess() {
        def user = PERSON_HOSWEB002
        def oldPassword = 111111
        auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(user, oldPassword))
        assertNotEquals(auth, null)
    }

    @Test
    void testOldPasswordFailure() {
        def user = PERSON_HOSWEB002
        def oldPassword = 11111
        auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(user, oldPassword))
        assertEquals(auth, null)
    }

    @Test
    void testOldPasswordMinError() {
        minLength = row?.GUBPPRF_MIN_LENGTH
        maxLength = row?.GUBPPRF_MAX_LENGTH
        def newPassword = '555555555'
        def invalidNewPassword = newPassword.substring(0, minLength - 2)
        def pidm = PERSON_HOSWEB002_PIDM
        def validateResult = resetPasswordService.validatePassword(pidm, invalidNewPassword)
        if (validateResult.get("error") == true) {
            assertTrue(true)
        } else
            assertFalse(false)
    }

    @Test
    void testOldPasswordMaxError() {
        minLength = row?.GUBPPRF_MIN_LENGTH
        maxLength = row?.GUBPPRF_MAX_LENGTH
        def invalidNewPassword = '555555555'
        while (invalidNewPassword.length() <= maxLength) {
            invalidNewPassword = invalidNewPassword.concat(invalidNewPassword)
        }
        def pidm = PERSON_HOSWEB002_PIDM
        def validateResult = resetPasswordService.validatePassword(pidm, invalidNewPassword)
        if (validateResult.get("error") == true) {
            assertTrue(true)
        } else
            assertFalse(false)
    }

    @Test
    void testPinExpDays() {
        def pinExpDays = resetPasswordService.getPinExpDays()
        assertNotNull(pinExpDays)
    }

    @Test
    void testAlphanumericPassword() {
        def invalidNewPassword
        pinResetFormat = row?.GUBPPRF_CHAR_IND
        minLength = row?.GUBPPRF_MIN_LENGTH
        maxLength = row?.GUBPPRF_MAX_LENGTH
        def pidm = PERSON_HOSWEB002_PIDM
        if (pinResetFormat.equalsIgnoreCase("Y")) {
            invalidNewPassword = "545434"
            if (invalidNewPassword.length() >= minLength && invalidNewPassword.length() <= maxLength) {
                def validateResult = resetPasswordService.validatePassword(pidm, invalidNewPassword)
                if (validateResult.get("error") == true) {
                    assertEquals(validateResult.get("errorMessage"), '::At least one alpha character value is required.::')
                } else
                    assertFalse(false)
            }
        }
    }

    @Test
    void testNumericPassword() {
        def invalidNewPassword
        pinResetFormat = row?.GUBPPRF_NUM_IND
        minLength = row?.GUBPPRF_MIN_LENGTH
        maxLength = row?.GUBPPRF_MAX_LENGTH
        def pidm = PERSON_HOSWEB002_PIDM
        if (pinResetFormat.equalsIgnoreCase("Y")) {
            invalidNewPassword = "aaaaaa"
            if (invalidNewPassword.length() >= minLength && invalidNewPassword.length() <= maxLength) {
                def validateResult = resetPasswordService.validatePassword(pidm, invalidNewPassword)
                if (validateResult.get("error") == true) {
                    assertEquals(validateResult.get("errorMessage"), '::At least one numeric character is required.::')
                } else
                    assertFalse(false)
            }
        }
    }

    @Test
    void testChangeExpiredPassword() {

        def newPassword = 555555
        def pidm = PERSON_HOSWEB002_PIDM
        def pinExpDays = 7
        try {
            sql.call("{call gb_third_party_access.p_update(p_pidm=>${pidm}, p_pin=>${newPassword},p_pin_exp_date=>sysdate + ${pinExpDays} )}")

        } catch (SQLException sq) {
            assertFalse(false)
        }
    }
}


