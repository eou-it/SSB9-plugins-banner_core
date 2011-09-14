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
 * Academic Year Validation Table.
 */
@Entity
@Table(name = "STVACYR")
class AcademicYear implements Serializable {

  /**
   * Surrogate ID for STVACYR
   */
  @Id
  @Column(name = "STVACYR_SURROGATE_ID")
  @SequenceGenerator(name = "STVACYR_SEQ_GEN", allocationSize = 1, sequenceName = "STVACYR_SURROGATE_ID_SEQUENCE")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STVACYR_SEQ_GEN")
  Long id

  /**
   * Identifies the abbreviation for the beginning/ ending periods for academic year referenced in the General Student, Academic History, Degree Audit Modules. Format CCYY (e.g. 1995-1996 coded 1996).
   */
  @Column(name = "STVACYR_CODE", nullable = false, unique = true, length = 4)
  String code

  /**
   * This field specifies the academic year associated with the academic year code.
   */
  @Column(name = "STVACYR_DESC", length = 30)
  String description

  /**
   * This field identifies the most current date a record was created or updated.
   */
  @Column(name = "STVACYR_ACTIVITY_DATE")
  Date lastModified

  /**
   * The system required indicator
   */
  @Type(type = "yes_no")
  @Column(name = "STVACYR_SYSREQ_IND")
  Boolean sysreqInd

  /**
   * Column for storing last modified by for STVACYR
   */
  @Column(name = "STVACYR_USER_ID", nullable = false, length = 30)
  String lastModifiedBy

  /**
   * Optimistic Lock Token for STVACYR
   */
  @Version
  @Column(name = "STVACYR_VERSION", nullable = false, precision = 19)
  Long version

  /**
   * Column for storing data origin for STVACYR
   */
  @Column(name = "STVACYR_DATA_ORIGIN", length = 30)
  String dataOrigin



  public String toString() {
    "AcademicYear[id=$id, code=$code, description=$description, lastModified=$lastModified, sysreqInd=$sysreqInd, lastModifiedBy=$lastModifiedBy, version=$version, dataOrigin=$dataOrigin]"
  }

  static constraints = {
    code(nullable: false, maxSize: 4)
    description(nullable: true, maxSize: 30)
    sysreqInd(nullable: true, maxSize: 1)
    lastModified(nullable: true)
    lastModifiedBy(nullable: true, maxSize: 30)
    dataOrigin(nullable: true, maxSize: 30)
  }


  boolean equals(o) {
    if (this.is(o)) return true
    if (!(o instanceof AcademicYear)) return false
    AcademicYear that = (AcademicYear) o
    if (code != that.code) return false
    if (dataOrigin != that.dataOrigin) return false
    if (description != that.description) return false
    if (id != that.id) return false
    if (lastModified != that.lastModified) return false
    if (lastModifiedBy != that.lastModifiedBy) return false
    if (sysreqInd != that.sysreqInd) return false
    if (version != that.version) return false
    return true
  }


  int hashCode() {
    int result
    result = (id != null ? id.hashCode() : 0)
    result = 31 * result + (code != null ? code.hashCode() : 0)
    result = 31 * result + (description != null ? description.hashCode() : 0)
    result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
    result = 31 * result + (sysreqInd != null ? sysreqInd.hashCode() : 0)
    result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
    result = 31 * result + (version != null ? version.hashCode() : 0)
    result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
    return result
  }

  //Read Only fields that should be protected against update
  public static readonlyProperties = ['code']
  /**
   * Please put all the custom methods/code in this protected section to protect the code
   * from being overwritten on re-generation
   */
  /*PROTECTED REGION ID(academicyear_custom_methods) ENABLED START*/

  /*PROTECTED REGION END*/
}
