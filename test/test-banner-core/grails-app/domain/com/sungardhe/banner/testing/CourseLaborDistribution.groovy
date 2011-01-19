/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.testing

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.persistence.SequenceGenerator
import javax.persistence.Transient
import javax.persistence.GenerationType

/**
 * Course Labor Distribution Table
 *
 */
@Entity
@Table(name = "SV_GTVZIPC")
class CourseLaborDistribution implements Serializable {

    /**
     * Surrogate ID for SCRCLBD
     */
    @Id
    @SequenceGenerator(name = "SCRCLBD_SEQ_GEN", allocationSize = 1, sequenceName = "SCRCLBD_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCRCLBD_SEQ_GEN")
    @Column(name = "SCRCLBD_SURROGATE_ID")
    Long id
    /**
     * COURSE NUMBER: The course number associated with the subject for the course.
     */
    @Column(name = "SCRCLBD_CRSE_NUMB", nullable = false, unique = true, length = 5)
    String courseNumber

    /**
     * SEQUENCE NUMBER: Sequence number of the course labor distribution information.
     */
    @Column(name = "SCRCLBD_SEQ_NO", nullable = false, unique = true, precision = 3)
    Integer sequenceNumber

    /**
     * PERCENTAGE: Percent of pay to be applied to this labor distribution.
     */
    @Column(name = "SCRCLBD_PERCENT", nullable = false, precision = 5, scale = 2)
    Double percent

    /**
     * ACTIVITY DATE: Date of last activity (insert or update) on the record.
     */
    @Column(name = "SCRCLBD_ACTIVITY_DATE")
    Date lastModified

    /**
     * USER ID: The Oracle ID of the user who changed the record.
     */
    @Column(name = "SCRCLBD_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row.
     */
    @Column(name = "SCRCLBD_DATA_ORIGIN")
    String dataOrigin

    /**
     * CHART OF ACCOUNTS CODE: The primary identification code for a chart of accounts.Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_COAS_CODE", length = 1)
    String chartOfAccountsCode

    /**
     * ACCOUNT INDEX: An index code used to default the account distribution to which the position should be charged. Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_ACCI_CODE", length = 6)
    String accountIndexCode

    /**
     * FUND: The fund code to which a position should be charged. Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_FUND_CODE", length = 6)
    String fundCode

    /**
     * ORGANIZATION: The organization to which a position should be charged. Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_ORGN_CODE", length = 6)
    String organizationCode

    /**
     * ACCOUNT: The account code to which a position should be charged. Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_ACCT_CODE", length = 6)
    String accountCode

    /**
     * PROGRAM: The program code to which a position should be charged. Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_PROG_CODE", length = 6)
    String programCode

    /**
     * ACTIVITY: The activity code to which a position should be charged. Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_ACTV_CODE", length = 6)
    String activityCode

    /**
     * LOCATION: The location code to which a position should be charged. Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_LOCN_CODE", length = 6)
    String locationCode

    /**
     * PROJECT: The project code within the Finance Cost Accounting Module. Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_PROJ_CODE", length = 8)
    String projectCode

    /**
     * COST TYPE: The cost type within the Finance Cost Accounting Module. Valued only if Banner Finance is installed.
     */
    @Column(name = "SCRCLBD_CTYP_CODE", length = 2)
    String ctypCode

    /**
     * EXTERNAL ACCOUNT CODE: A free-form account code to allow updates to an accounting system other than Banner Finance.
     */
    @Column(name = "SCRCLBD_ACCT_EXTERNAL_CDE", length = 60)
    String accountExternalCde

    /**
     * Optimistic Lock Token for SCRCLBD
     */
    @Version
    @Column(name = "SCRCLBD_VERSION", nullable = false, precision = 19)
    Long version


    /**
     * Transient variable for termTo value
     */
    @Transient
    String termTo


    public String toString() {
        "CourseLaborDistribution[id=$id, " +
		         "   courseNumber=$courseNumber, " +
                 "   sequenceNumber=$sequenceNumber," +
                 "   percent=$percent,      " +
                 "   lastModified=$lastModified, " +
                 "   lastModifiedBy=$lastModifiedBy, " +
                 "   dataOrigin=$dataOrigin,   " +
                 "   chartOfAccountsCode=$chartOfAccountsCode,  " +
                 "   accountIndexCode=$accountIndexCode, " +
                 "   fundCode=$fundCode,          " +
                 "   organizationCode=$organizationCode, " +
                 "   accountCode=$accountCode, " +
                 "   programCode=$programCode, " +
                 "   activityCode=$activityCode, " +
                 "   locationCode=$locationCode, " +
                 "   projectCode=$projectCode,  " +
                 "   ctypCode=$ctypCode,    " +
                 "   accountExternalCde=$accountExternalCde,  " +
                 "   version=$version]"
    }


    static constraints = {
        courseNumber(nullable: false, maxSize: 5)
        sequenceNumber(nullable: false, min:new Integer(-999), max:new Integer(999))
        percent(nullable: false, scale:2, min:new Double(-999.99), max:new Double(999.99))
        chartOfAccountsCode(nullable: true, maxSize: 1)
        accountIndexCode(nullable: true, maxSize: 6)
        fundCode(nullable: true, maxSize: 6)
        organizationCode(nullable: true, maxSize: 6)
        accountCode(nullable: true, maxSize: 6)
        programCode(nullable: true, maxSize: 6)
        activityCode(nullable: true, maxSize: 6)
        locationCode(nullable: true, maxSize: 6)
        projectCode(nullable: true, maxSize: 8)
        ctypCode(nullable: true, maxSize: 2)
        accountExternalCde(nullable: true, maxSize: 60)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        termTo(nullable:true)
    }

}



