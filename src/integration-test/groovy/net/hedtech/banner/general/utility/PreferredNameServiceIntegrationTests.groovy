/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.utility

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import static groovy.test.GroovyAssert.shouldFail

@Integration
@Rollback
class PreferredNameServiceIntegrationTests extends BaseIntegrationTestCase   {
    def params
    int pidm
    def usage
    def dataSource
    def sqlObj
    def conn
    def preferredNameService
    public final String LF30= "LF30"
    public final String L30= "L30"
    public final String FL30= "FL30"
    public final String FL= "FL"
    public final String FMIL= "FMIL"

    public final String LFMI= "LFMI"
    public final String DEFAULT= "DEFAULT"


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        conn = dataSource.getSsbConnection()
        sqlObj = new Sql( conn )
        pidm =  getPidmBySpridenId('SJGRIM')
        usage = DEFAULT
    }

    @After
    public void tearDown() {
        super.tearDown()
        sqlObj.close()
        conn.close()
    }

    @Test
    public void getPreferredNameDefaultUsage(){
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Warren Zevon Grim", defaultName
    }

    @Test
    public void getPreferredNameFLUsage(){
        usage = FL
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Warren Grim", defaultName
    }

    @Test
    public void getPreferredNameLF30Usage(){
        usage = LF30
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Grim, Warren", defaultName
    }

    @Test
    public void getPreferredNameL30Usage(){
        usage = L30
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Grim", defaultName
    }

    @Test
    public void getPreferredNameL60Usage(){
        usage = FL30
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Warren Grim", defaultName
    }

    @Test
    public void getPreferredNameFMILUsage(){
        usage = FMIL
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Warren Z. Grim", defaultName
    }

    @Test
    public void getPreferredNameLFMIUsage(){
        usage = LFMI
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Grim, Warren Z.", defaultName
    }

    @Test
    public void getPreferredNameInvalidPIDM(){
        usage = LFMI
        pidm = 11111
        params = [pidm:pidm, usage:usage]
        String defaultName
        shouldFail(ApplicationException) {
            try {
                 defaultName = preferredNameService.getPreferredName(params)
                } catch (ApplicationException ae) {
                    assert MessageHelper.message("net.hedtech.banner.preferredname.invalid.pidm"), ae.message
                    throw ae
                }
        }
    }

    @Test
    public void getPreferredNameInvalidUsage(){
        usage = "junk"
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Warren Zevon Grim", defaultName
    }

    @Test
    public void getPreferredNameNoUsage(){
        params = [pidm:pidm]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Warren Zevon Grim", defaultName
    }
    @Test
    public void getPreferredNameWithNameParams(){
        usage = FMIL
        params = [usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS", usedata:"Y"]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "JERRYONE M. LEWIS", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsAndPIDM(){
        usage = FMIL
        params = [pidm:pidm, usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS", usedata:"Y"]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "JERRYONE M. LEWIS", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsAndWithoutPIDM(){
        usage = FMIL
        params = [usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS", usedata:"Y"]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "JERRYONE M. LEWIS", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsAndWithoutUseData(){
        usage = DEFAULT
        params = [pidm:pidm,usage:usage, usedata:"N"]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Warren Zevon Grim", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsWithNoUseDataParams(){
        usage = FMIL
        params = [usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS",usedata:"Y"]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "JERRYONE M. LEWIS", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsWithUseDataParams(){

        params = [firstname:"VARUN", mi:"S", lastname:"SANKAR",productname:"Student",appname:"testApp",pagename:"testPage",usedata:"Y"]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "VARUN S SANKAR", defaultName
    }
    @Test
    public void getPreferredNameWithpidmsWithUseDataParams(){

        params = [pidm:pidm,productname:"Student",appname:"testApp",pagename:"testPage",usedata:"N"]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "Warren Zevon Grim", defaultName
    }

    @Test
    public void getPreferredNameWithNameAndMaxLength(){
        usage = FMIL
        params = [usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS", usedata:"Y", maxlength:10]
        String defaultName = preferredNameService.getPreferredName(params)
        assertEquals "J. LEWIS", defaultName
    }

    @Test
    public void getUsageDefault(){
        String defaultName = preferredNameService.getUsage("Student","testApp","testPage","testSection")
        assertEquals DEFAULT, defaultName
    }

    @Test
    public void getUsageDefaultWithProductNameAndAppName(){
        Holders?.config?.productName = "Student";
        Holders?.config?.banner?.applicationName = "testApp";
        String defaultName = preferredNameService.getUsage()
        assertEquals DEFAULT, defaultName
    }


    private def getPidmBySpridenId(def spridenId) {
        def query = "SELECT SPRIDEN_PIDM pidm FROM SPRIDEN WHERE SPRIDEN_ID=$spridenId"
        def pidmValue = sqlObj?.firstRow(query)?.pidm
        pidmValue
    }


}
