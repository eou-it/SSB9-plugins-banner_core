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
/**
 Banner Automator Version: 0.1.1
 Generated: Mon Jan 03 15:56:54 CST 2011
 */
package com.sungardhe.banner.testing

import org.hibernate.annotations.Type
import javax.persistence.*

/**
 * Term Code Validation Table
 */
@Entity
@Table(name = "STVTERM")
@NamedQueries(value = [
@NamedQuery(name = "Term.fetchPreviousTerm",
query = """FROM Term a
           WHERE a.code = ( SELECT MAX(b.code)
                            FROM Term b
                            WHERE b.code < :term)""")
])

class Term implements Serializable {

  /**
   * Surrogate ID for STVTERM
   */
  @Id
  @Column(name = "STVTERM_SURROGATE_ID")
  @SequenceGenerator(name = "STVTERM_SEQ_GEN", allocationSize = 1, sequenceName = "STVTERM_SURROGATE_ID_SEQUENCE")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STVTERM_SEQ_GEN")
  Long id

  /**
   * This field identifies the term code referenced in the Catalog, Recruiting, Admissions, Gen. Student, Registration, Student Billing and Acad. Hist. Modules. Reqd. value: 999999 - End of Time.
   */
  @Column(name = "STVTERM_CODE", nullable = false, unique = true, length = 6)
  String code

  /**
   * This field specifies the term associated with the term code. The term is identified by the academic year and term number and is formatted YYYYTT.
   */
  @Column(name = "STVTERM_DESC", nullable = false, length = 30)
  String description

  /**
   * This field identifies the term start date and is formatted DD-MON-YY.
   */
  @Column(name = "STVTERM_START_DATE", nullable = false)
  Date startDate

  /**
   * This field identifies the term end date and is fomatted DD-MON-YY.
   */
  @Column(name = "STVTERM_END_DATE", nullable = false)
  Date endDate

  /**
   * This field identifies the financial aid processing start and end years (e.g. The financial aid processing year 1988 - 1989 is formatted 8889.).
   */
  @Column(name = "STVTERM_FA_PROC_YR", length = 4)
  String financialAidProcessingYear

  /**
   * This field identifies the most recent date a record was created or updated.
   */
  @Column(name = "STVTERM_ACTIVITY_DATE")
  Date lastModified

  /**
   * This field identifies the financial aid award term.
   */
  @Column(name = "STVTERM_FA_TERM", length = 1)
  String financialAidTerm

  /**
   * This field identifies the financial aid award beginning period.
   */
  @Column(name = "STVTERM_FA_PERIOD", precision = 2)
  Integer financialAidPeriod

  /**
   * This field identifies the financial aid award ending period.
   */
  @Column(name = "STVTERM_FA_END_PERIOD", precision = 2)
  Integer financialEndPeriod

  /**
   * Housing Start Date.
   */
  @Column(name = "STVTERM_HOUSING_START_DATE", nullable = false)
  Date housingStartDate

  /**
   * Housing End Date.
   */
  @Column(name = "STVTERM_HOUSING_END_DATE", nullable = false)
  Date housingEndDate

  /**
   * System Required Indicator
   */
  @Type(type = "yes_no")
  @Column(name = "STVTERM_SYSTEM_REQ_IND")
  Boolean systemReqInd

  /**
   * SUMMER INDICATOR: Indicates a summer term to financial aid.
   */
  @Type(type = "yes_no")
  @Column(name = "STVTERM_FA_SUMMER_IND")
  Boolean financeSummerIndicator

  /**
   * Column for storing last modified by for STVTERM
   */
  @Column(name = "STVTERM_USER_ID", length = 30)
  String lastModifiedBy

  /**
   * Optimistic Lock Token for STVTERM
   */
  @Version
  @Column(name = "STVTERM_VERSION", nullable = false, precision = 19)
  Long version

  /**
   * Column for storing data origin for STVTERM
   */
  @Column(name = "STVTERM_DATA_ORIGIN", length = 30)
  String dataOrigin

  // TODO:  Determine the appropriate name for acyr_code
  /**
   * Foreign Key : FK1_STVTERM_INV_STVACYR_CODE
   */
  @ManyToOne
  @JoinColumns([
  @JoinColumn(name = "STVTERM_ACYR_CODE", referencedColumnName = "stvacyr_code")
  ])
  AcademicYear academicYear

  // TODO:  Determine the appropriate name for trmt_code
  /**
   * Foreign Key : FK1_STVTERM_INV_STVTRMT_CODE
   */
  @ManyToOne
  @JoinColumns([
  @JoinColumn(name = "STVTERM_TRMT_CODE", referencedColumnName = "STVTRMT_CODE")
  ])
  TermType termType


  public String toString() {
    "Term[id=$id, code=$code, description=$description, startDate=$startDate, endDate=$endDate, financialAidProcessingYear=$financialAidProcessingYear, lastModified=$lastModified, financialAidTerm=$financialAidTerm, financialAidPeriod=$financialAidPeriod, financialEndPeriod=$financialEndPeriod, housingStartDate=$housingStartDate, housingEndDate=$housingEndDate, systemReqInd=$systemReqInd, financeSummerIndicator=$financeSummerIndicator, lastModifiedBy=$lastModifiedBy, version=$version, dataOrigin=$dataOrigin]"
  }

  static constraints = {
    code(nullable: false, maxSize: 6)
    description(nullable: false, maxSize: 30)
    startDate(nullable: false, maxSize: 7)
    endDate(nullable: false, maxSize: 7)
    acyr_code(nullable: false, maxSize: 4)
    financialAidProcessingYear(nullable: true, maxSize: 4)
    financialAidTerm(nullable: true, maxSize: 1)
    financialAidPeriod(nullable: true, maxSize: 22)
    financialEndPeriod(nullable: true, maxSize: 22)
    housingStartDate(nullable: false, maxSize: 7)
    housingEndDate(nullable: false, maxSize: 7)
    systemReqInd(nullable: true, maxSize: 1)
    financeSummerIndicator(nullable: true, maxsize: 1)
    lastModified(nullable: true)
    lastModifiedBy(nullable: true, maxSize: 30)
    dataOrigin(nullable: true, maxSize: 30)
  }





  boolean equals(o) {
    if (this.is(o)) return true

    if (!(o instanceof Term)) return false

    Term term = (Term) o

    if (academicYear != term.academicYear) return false
    if (code != term.code) return false
    if (dataOrigin != term.dataOrigin) return false
    if (description != term.description) return false
    if (endDate != term.endDate) return false
    if (financialAidPeriod != term.financialAidPeriod) return false
    if (financialAidProcessingYear != term.financialAidProcessingYear) return false
    if (financialAidTerm != term.financialAidTerm) return false
    if (financialEndPeriod != term.financialEndPeriod) return false
    if (housingEndDate != term.housingEndDate) return false
    if (housingStartDate != term.housingStartDate) return false
    if (id != term.id) return false
    if (lastModified != term.lastModified) return false
    if (lastModifiedBy != term.lastModifiedBy) return false
    if (startDate != term.startDate) return false
    if (systemReqInd != term.systemReqInd) return false
    if (financeSummerIndicator != term.financeSummerIndicator) return false
    if (termType != term.termType) return false
    if (version != term.version) return false

    return true
  }


  int hashCode() {
    int result

    result = (id != null ? id.hashCode() : 0)
    result = 31 * result + (code != null ? code.hashCode() : 0)
    result = 31 * result + (description != null ? description.hashCode() : 0)
    result = 31 * result + (startDate != null ? startDate.hashCode() : 0)
    result = 31 * result + (endDate != null ? endDate.hashCode() : 0)
    result = 31 * result + (financialAidProcessingYear != null ? financialAidProcessingYear.hashCode() : 0)
    result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
    result = 31 * result + (financialAidTerm != null ? financialAidTerm.hashCode() : 0)
    result = 31 * result + (financialAidPeriod != null ? financialAidPeriod.hashCode() : 0)
    result = 31 * result + (financialEndPeriod != null ? financialEndPeriod.hashCode() : 0)
    result = 31 * result + (housingStartDate != null ? housingStartDate.hashCode() : 0)
    result = 31 * result + (housingEndDate != null ? housingEndDate.hashCode() : 0)
    result = 31 * result + (systemReqInd != null ? systemReqInd.hashCode() : 0)
    result = 31 * result + (financeSummerIndicator != null ? financeSummerIndicator.hashCode() : 0)
    result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
    result = 31 * result + (version != null ? version.hashCode() : 0)
    result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
    result = 31 * result + (academicYear != null ? academicYear.hashCode() : 0)
    result = 31 * result + (termType != null ? termType.hashCode() : 0)
    return result
  }

  //Read Only fields that should be protected against update
  public static readonlyProperties = ['code']
  /**
   * Please put all the custom methods/code in this protected section to protect the code
   * from being overwritten on re-generation
   */
  /*PROTECTED REGION ID(term_custom_methods) ENABLED START*/

  /*PROTECTED REGION END*/
}
