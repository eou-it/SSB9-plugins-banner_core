/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.utility

import grails.spring.BeanBuilder
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.dbcp.BasicDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext

class PreferredNameServiceIntegrationTests extends BaseIntegrationTestCase   {
    def params
    int pidm
    def usage
    def preferredNameService
    public final String LF30= "LF30"
    public final String L30= "L30"
    public final String L60= "L60"
    public final String FL30= "FL30"
    public final String FL= "FL"
    public final String FMIL= "FMIL"
    public final String FML= "FML"
    public final String LF= "LF"
    public final String LFMI= "LFMI"
    public final String LFM= "LFM"
    public final String LFIMI30= "LFIMI30"
    public final String DEFAULT= "DEFAULT"
    public final String LEGAL= "LEGAL"
    public final int PIDM = 30689

    @Before
    public void setUp() {
        ApplicationContext testSpringContext = createUnderlyingSsbDataSourceBean()
        dataSource.underlyingSsbDataSource =  testSpringContext.getBean("underlyingSsbDataSource")
        formContext = ['GUAGMNU']
        super.setUp()
        pidm = PIDM
        usage = DEFAULT
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void getPreferredNameDefaultUsage(){
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Jerryone L Lewis", defaultName
    }

    @Test
    public void getPreferredNameFLUsage(){
        usage = FL
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Jerryone Lewis", defaultName
    }

    @Test
    public void getPreferredNameLF30Usage(){
        usage = LF30
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Lewis, Jerryone", defaultName
    }

    @Test
    public void getPreferredNameL30Usage(){
        usage = L30
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Lewis", defaultName
    }

    @Test
    public void getPreferredNameL60Usage(){
        usage = FL30
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Jerryone Lewis", defaultName
    }

    @Test
    public void getPreferredNameFMILUsage(){
        usage = FMIL
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Jerryone L. Lewis", defaultName
    }

    @Test
    public void getPreferredNameLFMIUsage(){
        usage = LFMI
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Lewis, Jerryone L.", defaultName
    }

    @Test
    public void getPreferredNameInvalidPIDM(){
        usage = LFMI
        pidm = 11111
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getName(params)
        println defaultName
        assertEquals "*ERROR* Name with rule LFMI not found for ID: UNKNOWN", defaultName
    }

    @Test
    public void getPreferredNameInvalidUsage(){
        usage = "junk"
        params = [pidm:pidm, usage:usage]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Jerryone L Lewis", defaultName
    }

    @Test
    public void getPreferredNameNoUsage(){
        params = [pidm:pidm]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Jerryone L Lewis", defaultName
    }
    @Test
    public void getPreferredNameWithNameParams(){
        usage = FMIL
        params = [usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS", usedata:"Y"]
        String defaultName = preferredNameService.getName(params)
        assertEquals "JERRYONE M. LEWIS", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsAndPIDM(){
        usage = FMIL
        params = [pidm:PIDM, usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS", usedata:"Y"]
        String defaultName = preferredNameService.getName(params)
        assertEquals "JERRYONE M. LEWIS", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsAndWithoutPIDM(){
        usage = FMIL
        params = [usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS", usedata:"Y"]
        String defaultName = preferredNameService.getName(params)
        assertEquals "JERRYONE M. LEWIS", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsAndWithoutUseData(){
        usage = DEFAULT
        params = [pidm:pidm,usage:usage, usedata:"N"]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Jerryone L Lewis", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsWithNoUseDataParams(){
        usage = FMIL
        params = [usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS",usedata:"Y"]
        String defaultName = preferredNameService.getName(params)
        assertEquals "JERRYONE M. LEWIS", defaultName
    }

    @Test
    public void getPreferredNameWithNameParamsWithUseDataParams(){

        params = [firstname:"VARUN", mi:"S", lastname:"SANKAR",produtname:"Payroll",appname:"Taxes",pageName:"W%",usedata:"Y"]
        String defaultName = preferredNameService.getName(params)
        assertEquals "SANKAR, VARUN S.", defaultName
    }
    @Test
    public void getPreferredNameWithpidmsWithUseDataParams(){

        params = [pidm:pidm,produtname:"Payroll",appname:"Taxes",pageName:"W%",usedata:"N"]
        String defaultName = preferredNameService.getName(params)
        assertEquals "Lewis, Jerryone L.", defaultName
    }

    @Test
    public void getPreferredNameWithNameAndMaxLength(){
        usage = FMIL
        params = [usage:usage, firstname:"JERRYONE", mi:"MIDDLE", lastname:"LEWIS", usedata:"Y", maxlength:10]
        String defaultName = preferredNameService.getName(params)
        assertEquals "J. LEWIS", defaultName
    }

    @Test
    public void getUsageDefault(){
        String defaultName = preferredNameService.getUsage("W4","Header")
        assertEquals "DEFAULT", defaultName
    }
    private ApplicationContext createUnderlyingSsbDataSourceBean() {
        def bb = new BeanBuilder()
        bb.beans {
            underlyingSsbDataSource(BasicDataSource) {
                maxActive = 5
                maxIdle = 2
                defaultAutoCommit = "false"
                driverClassName = "${Holders.config.bannerSsbDataSource.driver}"
                url = "${Holders.config.bannerSsbDataSource.url}"
                password = "${Holders.config.bannerSsbDataSource.password}"
                username = "${Holders.config.bannerSsbDataSource.username}"
            }
        }
        ApplicationContext testSpringContext = bb.createApplicationContext()
        return testSpringContext
    }
}
