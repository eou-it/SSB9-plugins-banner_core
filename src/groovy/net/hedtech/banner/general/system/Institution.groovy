/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package net.hedtech.banner.general.system

import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * Institutional Table
 */
@Entity
@Table(name = "GUBINST")
@NamedQueries(value = [
@NamedQuery(name = "Institution.findForKey",
query = """FROM  Institution a
		   WHERE a.key = 'INST'
	""")
])
class Institution implements Serializable {

    /**
     * Surrogate ID for GUBINST
     */
    @Id
    @Column(name = "GUBINST_SURROGATE_ID")
    @SequenceGenerator(name = "GUBINST_SEQ_GEN", allocationSize = 1, sequenceName = "GUBINST_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GUBINST_SEQ_GEN")
    Long id

    /**
     * KEY: Value of INST.
     */
    @Column(name = "GUBINST_KEY", nullable = false, length = 8)
    String key

    /**
     * OPERATING SYSTEM: UNX, VMS, MVS, AOS, NOS.
     */
    @Column(name = "GUBINST_OPERATING_SYSTEM_IND", nullable = false, length = 3)
    String operatingSystemIndicator

    /**
     * NAME: Name of Institution.
     */
    @Column(name = "GUBINST_NAME", nullable = false, length = 30)
    String name

    /**
     * None
     */
    @Column(name = "GUBINST_STREET_LINE1", length = 75)
    String streetLine1

    /**
     * None
     */
    @Column(name = "GUBINST_STREET_LINE2", length = 75)
    String streetLine2

    /**
     * None
     */
    @Column(name = "GUBINST_STREET_LINE3", length = 75)
    String streetLine3

    /**
     * None
     */
    @Column(name = "GUBINST_CITY", nullable = false, length = 50)
    String city

    /**
     * None
     */
    @Column(name = "GUBINST_STAT_CODE", length = 3)
    String statCode

    /**
     * None
     */
    @Column(name = "GUBINST_ZIP", length = 30)
    String zip

    /**
     * None
     */
    @Column(name = "GUBINST_PHONE_AREA", length = 6)
    String phoneArea

    /**
     * None
     */
    @Column(name = "GUBINST_PHONE", length = 12)
    String phone

    /**
     * None
     */
    @Column(name = "GUBINST_PHONE_EXT", length = 10)
    String phoneExt

    /**
     * NATION: Nation code.  Values are from the STVNATN form.
     */
    @Column(name = "GUBINST_NATN_CODE", length = 5)
    String natnCode

    /**
     * FINAID IND: Y = Non-BANNER Financial Aid system (SIGMA/SAM) is installed.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_FINAID_IND", length = 1)
    Boolean finaidIndicator

    /**
     * ACTIVITY DATE: Date the record was created or last updated.
     */
    @Column(name = "GUBINST_activity_date")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * STUDENT INSTALLED: Y = BANNER Student system is installed.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_STUDENT_INSTALLED", length = 1)
    Boolean studentInstalled

    /**
     * ALUMNI  INSTALLED: Y = BANNER Alumni system is installed.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_ALUMNI_INSTALLED", length = 1)
    Boolean alumniInstalled

    /**
     * FINANCE INSTALLED: Y = BANNER Finance system is installed.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_FINANCE_INSTALLED", length = 1)
    Boolean financeInstalled

    /**
     * H. R.   INSTALLED: Y = BANNER H.R. system is installed.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_HUMANRE_INSTALLED", length = 1)
    Boolean hrInstalled

    /**
     * This column is now defunct. 08/30/89
     */
    @Column(name = "GUBINST_BYPASS_ALT_VAL", length = 1)
    String bypassAltVal

    /**
     * POSCTRL INSTALLED: Y = BANNER Position Control system is installed.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_POSBUD_INSTALLED", length = 1)
    Boolean posnctlInstalled

    /**
     * ACC REC INSTALLED: Y = BANNER A/R system is installed.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_BILCSH_INSTALLED", length = 1)
    Boolean arsysInstalled

    /**
     * SECURITY INDICATOR: Y = Security is turned on.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_SECURITY_IND", length = 1)
    Boolean securityIndicator

    /**
     * FINAID  INSTALLED: Y = BANNER Financial Aid system is installed.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_FINAID_INSTALLED", length = 1)
    Boolean finaidInstalled

    /**
     * COURTS  INSTALLED: Y = BANNER Courts system is installed.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_COURTS_INSTALLED", length = 1)
    Boolean courtsInstalled

    /**
     * INSTITUTION TYPE: H = Higher Education, G = Government, K = K-12, C = Commercial.
     */
    @Column(name = "GUBINST_HIGHED_GOVT_IND", length = 1)
    String highedGovtIndicator

    /**
     * Establishes the base currency for the installation.
     */
    @Column(name = "GUBINST_BASE_CURR_CODE", length = 4)
    String baseCurrCode

    /**
     * UTILITIES INSTALLED: Y = BANNER Utilities system is installed.
     */
    @Column(name = "GUBINST_UTILITIES_INSTALLED", length = 1)
    String utilitiesInstalled

    /**
     * None
     */
    @Column(name = "GUBINST_ZIP_DEFAULT_LENGTH", length = 22)
    BigDecimal zipDefaultLength

    /**
     * None
     */
    @Column(name = "GUBINST_CENTURY_PIVOT", length = 22)
    BigDecimal centuryPivot

    /**
     * None
     */
    @Column(name = "GUBINST_DATE_DEFAULT_FORMAT", length = 22)
    BigDecimal dateDefaultFormat

    /**
     * Contains the user defined name for the instance.
     */
    @Column(name = "GUBINST_INSTANCE_NAME", length = 8)
    String instanceName

    /**
     * None
     */
    @Column(name = "GUBINST_PTAX_INSTALLED", length = 1)
    String ptaxInstalled

    /**
     * None
     */
    @Column(name = "GUBINST_OCCTAX_INSTALLED", length = 1)
    String occtaxInstalled

    /**
     * None
     */
    @Column(name = "GUBINST_RECORDS_INSTALLED", length = 1)
    String recordsInstalled

    /**
     * None
     */
    @Column(name = "GUBINST_CASHREC_INSTALLED", length = 1)
    String cashrecInstalled

    /**
     * None
     */
    @Column(name = "GUBINST_MAX_OPEN_FORMS", length = 22)
    BigDecimal maximumOpenForms

    /**
     * None
     */
    @Column(name = "GUBINST_CHARMODE_GURJOBS_IND", length = 1)
    String charmodeGurjobsIndicator

    /**
     * None
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_WF_ENABLED_IND", nullable = false, length = 1)
    Boolean wfEnabledIndicator

    /**
     * Message Indicator: This field indicates if the institution has messaging on (Y) or off (N)
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_MESSAGE_ENABLED_IND", nullable = false, length = 1)
    Boolean messageEnabledIndicator

    /**
     * SQL Trace Indicator: This field indicates if the institution has SQL Trace on (Y) or off (N)
     */
    @Column(name = "GUBINST_SQLTRACE_ENABLED_IND", nullable = false, length = 1)
    String sqltraceEnabledIndicator

    /**
     * EXECUTE ONLINE COMMON MATCHING PROCESS: Y/N indicator to use Common Matching process when creating new person or non-person records from a form.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_ONLINE_MATCH_IND", nullable = false, length = 1)
    Boolean onlineMatchIndicator

    /**
     * ENABLE OR DISABLE SSN ID LOOKUP SEARCH PROCESS: Y/N indicator to enable or disable the capability to perform an SSN Search from a Banner Form ID field.  This indicator works in conjunction with security object SSN_SEARCH.
     */
    @Type(type = "yes_no")
    @Column(name = "GUBINST_SSN_SEARCH_ENABLED_IND", nullable = false, length = 1)
    Boolean ssnSearchEnabledIndicator

    /**
     * TELEPHONE COUNTRY CODE: Code designating the region or country.
     */
    @Column(name = "GUBINST_CTRY_CODE_PHONE", length = 4)
    String ctryCodePhone

    /**
     * HOUSE NUMBER: Building or lot number on a street or in an area.
     */
    @Column(name = "GUBINST_HOUSE_NUMBER", length = 10)
    String houseNumber

    /**
     * STREET LINE4: This field maintains line four of the street address.
     */
    @Column(name = "GUBINST_STREET_LINE4", length = 75)
    String streetLine4

    /**
     * SSN MAX LENGTH: SSN max length indicator.
     */
    @Column(name = "GUBINST_SSN_MAX_LENGTH", length = 22)
    BigDecimal ssnMaximumLength

    /**
     * Version column which is used as a optimistic lock token for GUBINST
     */
    @Version
    @Column(name = "GUBINST_VERSION", nullable = false, length = 19)
    Long version

    /**
     * Last Modified By column for GUBINST
     */
    @Column(name = "GUBINST_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data Origin column for GUBINST
     */
    @Column(name = "GUBINST_DATA_ORIGIN", length = 30)
    String dataOrigin


    public static Institution fetchByKey() {
        return Institution.withSession {session ->
            session.getNamedQuery('Institution.findForKey').list()[0]
        }
    }


    public String toString() {
        "Institution" +
                "[id=$id, " +
                "key=$key, " +
                "operatingSystemIndicator=$operatingSystemIndicator," +
                "name=$name," +
                "streetLine1=$streetLine1, " +
                "streetLine2=$streetLine2, " +
                "streetLine3=$streetLine3, " +
                "city=$city, " +
                "statCode=$statCode, " +
                "zip=$zip, " +
                "phoneArea=$phoneArea, " +
                "phone=$phone, " +
                "phoneExt=$phoneExt, " +
                "natnCode=$natnCode, " +
                "finaidIndicator=$finaidIndicator, " +
                "lastModified=$lastModified, " +
                "studentInstalled=$studentInstalled, " +
                "alumniInstalled=$alumniInstalled, " +
                "financeInstalled=$financeInstalled, " +
                "hrInstalled=$hrInstalled, " +
                "bypassAltVal=$bypassAltVal, " +
                "posnctlInstalled=$posnctlInstalled, " +
                "arsysInstalled=$arsysInstalled, " +
                "securityIndicator=$securityIndicator, " +
                "finaidInstalled=$finaidInstalled, " +
                "courtsInstalled=$courtsInstalled, " +
                "highedGovtIndicator=$highedGovtIndicator, " +
                "baseCurrCode=$baseCurrCode, " +
                "utilitiesInstalled=$utilitiesInstalled, " +
                "zipDefaultLength=$zipDefaultLength, " +
                "centuryPivot=$centuryPivot, " +
                "dateDefaultFormat=$dateDefaultFormat, " +
                "instanceName=$instanceName, " +
                "ptaxInstalled=$ptaxInstalled, " +
                "occtaxInstalled=$occtaxInstalled, " +
                "recordsInstalled=$recordsInstalled, " +
                "cashrecInstalled=$cashrecInstalled, " +
                "maximumOpenForms=$maximumOpenForms, " +
                "charmodeGurjobsIndicator=$charmodeGurjobsIndicator, " +
                "wfEnabledIndicator=$wfEnabledIndicator, " +
                "messageEnabledIndicator=$messageEnabledIndicator, " +
                "sqltraceEnabledIndicator=$sqltraceEnabledIndicator, " +
                "onlineMatchIndicator=$onlineMatchIndicator, " +
                "ssnSearchEnabledIndicator=$ssnSearchEnabledIndicator, " +
                "ctryCodePhone=$ctryCodePhone, " +
                "houseNumber=$houseNumber, " +
                "streetLine4=$streetLine4, " +
                "ssnMaximumLength=$ssnMaximumLength, " +
                "version=$version, " +
                "lastModifiedBy=$lastModifiedBy, " +
                "dataOrigin=$dataOrigin]"
    }


    static constraints = {
        key(nullable: false, maxSize: 8)
        operatingSystemIndicator(nullable: false, maxSize: 3)
        name(nullable: false, maxSize: 30)
        streetLine1(nullable: true, maxSize: 75)
        streetLine2(nullable: true, maxSize: 75)
        streetLine3(nullable: true, maxSize: 75)
        city(nullable: false, maxSize: 50)
        statCode(nullable: true, maxSize: 3)
        zip(nullable: true, maxSize: 30)
        phoneArea(nullable: true, maxSize: 6)
        phone(nullable: true, maxSize: 12)
        phoneExt(nullable: true, maxSize: 10)
        natnCode(nullable: true, maxSize: 5)
        finaidIndicator(nullable: true)
        studentInstalled(nullable: true)
        alumniInstalled(nullable: true)
        financeInstalled(nullable: true)
        hrInstalled(nullable: true)
        bypassAltVal(nullable: true, maxSize: 1)
        posnctlInstalled(nullable: true)
        arsysInstalled(nullable: true)
        securityIndicator(nullable: true)
        finaidInstalled(nullable: true)
        courtsInstalled(nullable: true)
        highedGovtIndicator(nullable: true, maxSize: 1)
        baseCurrCode(nullable: true, maxSize: 4)
        utilitiesInstalled(nullable: true, maxSize: 1)
        zipDefaultLength(nullable: true)
        centuryPivot(nullable: true)
        dateDefaultFormat(nullable: true)
        instanceName(nullable: true, maxSize: 8)
        ptaxInstalled(nullable: true, maxSize: 1)
        occtaxInstalled(nullable: true, maxSize: 1)
        recordsInstalled(nullable: true, maxSize: 1)
        cashrecInstalled(nullable: true, maxSize: 1)
        maximumOpenForms(nullable: true)
        charmodeGurjobsIndicator(nullable: true, maxSize: 1)
        wfEnabledIndicator(nullable: false)
        messageEnabledIndicator(nullable: false)
        sqltraceEnabledIndicator(nullable: false, maxSize: 1)
        onlineMatchIndicator(nullable: false)
        ssnSearchEnabledIndicator(nullable: false)
        ctryCodePhone(nullable: true, maxSize: 4)
        houseNumber(nullable: true, maxSize: 10)
        streetLine4(nullable: true, maxSize: 75)
        ssnMaximumLength(nullable: true)
        dataOrigin(nullable: true, maxSize: 30)

        /**
         * Please put all the custom tests in this protected section to protect the code
         * from being overwritten on re-generation
         */
        /*PROTECTED REGION ID(gubinst_custom_constraints) ENABLED START*/

        /*PROTECTED REGION END*/
    }


    boolean equals(o) {
        if (this.is(o)) return true;

        if (!(o instanceof Institution)) return false;

        Institution that = (Institution) o;

        if (alumniInstalled != that.alumniInstalled) return false;
        if (arsysInstalled != that.arsysInstalled) return false;
        if (baseCurrCode != that.baseCurrCode) return false;
        if (bypassAltVal != that.bypassAltVal) return false;
        if (cashrecInstalled != that.cashrecInstalled) return false;
        if (centuryPivot != that.centuryPivot) return false;
        if (charmodeGurjobsIndicator != that.charmodeGurjobsIndicator) return false;
        if (city != that.city) return false;
        if (courtsInstalled != that.courtsInstalled) return false;
        if (ctryCodePhone != that.ctryCodePhone) return false;
        if (dataOrigin != that.dataOrigin) return false;
        if (dateDefaultFormat != that.dateDefaultFormat) return false;
        if (finaidIndicator != that.finaidIndicator) return false;
        if (finaidInstalled != that.finaidInstalled) return false;
        if (financeInstalled != that.financeInstalled) return false;
        if (highedGovtIndicator != that.highedGovtIndicator) return false;
        if (houseNumber != that.houseNumber) return false;
        if (hrInstalled != that.hrInstalled) return false;
        if (id != that.id) return false;
        if (instanceName != that.instanceName) return false;
        if (key != that.key) return false;
        if (lastModified != that.lastModified) return false;
        if (lastModifiedBy != that.lastModifiedBy) return false;
        if (maximumOpenForms != that.maximumOpenForms) return false;
        if (messageEnabledIndicator != that.messageEnabledIndicator) return false;
        if (name != that.name) return false;
        if (natnCode != that.natnCode) return false;
        if (occtaxInstalled != that.occtaxInstalled) return false;
        if (onlineMatchIndicator != that.onlineMatchIndicator) return false;
        if (operatingSystemIndicator != that.operatingSystemIndicator) return false;
        if (phone != that.phone) return false;
        if (phoneArea != that.phoneArea) return false;
        if (phoneExt != that.phoneExt) return false;
        if (posnctlInstalled != that.posnctlInstalled) return false;
        if (ptaxInstalled != that.ptaxInstalled) return false;
        if (recordsInstalled != that.recordsInstalled) return false;
        if (securityIndicator != that.securityIndicator) return false;
        if (sqltraceEnabledIndicator != that.sqltraceEnabledIndicator) return false;
        if (ssnMaximumLength != that.ssnMaximumLength) return false;
        if (ssnSearchEnabledIndicator != that.ssnSearchEnabledIndicator) return false;
        if (statCode != that.statCode) return false;
        if (streetLine1 != that.streetLine1) return false;
        if (streetLine2 != that.streetLine2) return false;
        if (streetLine3 != that.streetLine3) return false;
        if (streetLine4 != that.streetLine4) return false;
        if (studentInstalled != that.studentInstalled) return false;
        if (utilitiesInstalled != that.utilitiesInstalled) return false;
        if (version != that.version) return false;
        if (wfEnabledIndicator != that.wfEnabledIndicator) return false;
        if (zip != that.zip) return false;
        if (zipDefaultLength != that.zipDefaultLength) return false;

        return true;
    }


    int hashCode() {
        int result

        result = (id != null ? id.hashCode() : 0)

        result = 31 * result + (alumniInstalled != null ? alumniInstalled.hashCode() : 0)
        result = 31 * result + (arsysInstalled != null ? arsysInstalled.hashCode() : 0)
        result = 31 * result + (baseCurrCode != null ? baseCurrCode.hashCode() : 0)
        result = 31 * result + (bypassAltVal != null ? bypassAltVal.hashCode() : 0)
        result = 31 * result + (cashrecInstalled != null ? cashrecInstalled.hashCode() : 0)
        result = 31 * result + (centuryPivot != null ? centuryPivot.hashCode() : 0)
        result = 31 * result + (charmodeGurjobsIndicator != null ? charmodeGurjobsIndicator.hashCode() : 0)
        result = 31 * result + (city != null ? city.hashCode() : 0)
        result = 31 * result + (courtsInstalled != null ? courtsInstalled.hashCode() : 0)
        result = 31 * result + (ctryCodePhone != null ? ctryCodePhone.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (dateDefaultFormat != null ? dateDefaultFormat.hashCode() : 0)
        result = 31 * result + (finaidIndicator != null ? finaidIndicator.hashCode() : 0)
        result = 31 * result + (finaidInstalled != null ? finaidInstalled.hashCode() : 0)
        result = 31 * result + (financeInstalled != null ? financeInstalled.hashCode() : 0)
        result = 31 * result + (highedGovtIndicator != null ? highedGovtIndicator.hashCode() : 0)
        result = 31 * result + (houseNumber != null ? houseNumber.hashCode() : 0)
        result = 31 * result + (hrInstalled != null ? hrInstalled.hashCode() : 0)
        result = 31 * result + (instanceName != null ? instanceName.hashCode() : 0)
        result = 31 * result + (key != null ? key.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (maximumOpenForms != null ? maximumOpenForms.hashCode() : 0)
        result = 31 * result + (messageEnabledIndicator != null ? messageEnabledIndicator.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (natnCode != null ? natnCode.hashCode() : 0)
        result = 31 * result + (occtaxInstalled != null ? occtaxInstalled.hashCode() : 0)
        result = 31 * result + (onlineMatchIndicator != null ? onlineMatchIndicator.hashCode() : 0)
        result = 31 * result + (operatingSystemIndicator != null ? operatingSystemIndicator.hashCode() : 0)
        result = 31 * result + (phone != null ? phone.hashCode() : 0)
        result = 31 * result + (phoneArea != null ? phoneArea.hashCode() : 0)
        result = 31 * result + (phoneExt != null ? phoneExt.hashCode() : 0)
        result = 31 * result + (posnctlInstalled != null ? posnctlInstalled.hashCode() : 0)
        result = 31 * result + (ptaxInstalled != null ? ptaxInstalled.hashCode() : 0)
        result = 31 * result + (recordsInstalled != null ? recordsInstalled.hashCode() : 0)
        result = 31 * result + (securityIndicator != null ? securityIndicator.hashCode() : 0)
        result = 31 * result + (sqltraceEnabledIndicator != null ? sqltraceEnabledIndicator.hashCode() : 0)
        result = 31 * result + (ssnMaximumLength != null ? ssnMaximumLength.hashCode() : 0)
        result = 31 * result + (ssnSearchEnabledIndicator != null ? ssnSearchEnabledIndicator.hashCode() : 0)
        result = 31 * result + (statCode != null ? statCode.hashCode() : 0)
        result = 31 * result + (streetLine1 != null ? streetLine1.hashCode() : 0)
        result = 31 * result + (streetLine2 != null ? streetLine2.hashCode() : 0)
        result = 31 * result + (streetLine3 != null ? streetLine3.hashCode() : 0)
        result = 31 * result + (streetLine4 != null ? streetLine4.hashCode() : 0)
        result = 31 * result + (studentInstalled != null ? studentInstalled.hashCode() : 0)
        result = 31 * result + (utilitiesInstalled != null ? utilitiesInstalled.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (wfEnabledIndicator != null ? wfEnabledIndicator.hashCode() : 0)
        result = 31 * result + (zip != null ? zip.hashCode() : 0)
        result = 31 * result + (zipDefaultLength != null ? zipDefaultLength.hashCode() : 0)
        return result
    }
/**
 * Please put all the custom tests in this protected section to protect the code
 * from being overwritten on re-generation
 */
    /*PROTECTED REGION ID(gubinst_custom_methods) ENABLED START*/

    /*PROTECTED REGION END*/
}

