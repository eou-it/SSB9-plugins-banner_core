/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
/**
 Banner Automator Version: 0.1.1
 Generated: Mon Jan 03 15:56:54 CST 2011
 */
package net.hedtech.banner.testing

import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import javax.persistence.Column
import javax.persistence.GenerationType
import javax.persistence.GeneratedValue
import javax.persistence.Version
import javax.persistence.SequenceGenerator

/**
 * Term Type Validation Table.
 */
@Entity
@Table(name = "STVTRMT")
class TermTypeForTesting implements Serializable {

  /**
   * Surrogate ID for STVTRMT
   */
  @Id
  @Column(name = "STVTRMT_SURROGATE_ID")
  @SequenceGenerator(name = "STVTRMT_SEQ_GEN", allocationSize = 1, sequenceName = "STVTRMT_SURROGATE_ID_SEQUENCE")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STVTRMT_SEQ_GEN")
  Long id

  /**
   * Type of term, eg.  2 - semester, 4 - quarter.
   */
  @Column(name = "STVTRMT_CODE", nullable = false, unique = true, length = 1)
  String code

  /**
   * Specifies the type of term associated with term type code.
   */
  @Column(name = "STVTRMT_DESC", nullable = false, length = 30)
  String description

  /**
   * Most recent date record was created or updated.
   */
  @Column(name = "STVTRMT_ACTIVITY_DATE")
  Date lastModified

  /**
   * Column for storing last modified by for STVTRMT
   */
  @Column(name = "STVTRMT_USER_ID", length = 30)
  String lastModifiedBy

  /**
   * Optimistic Lock Token for STVTRMT
   */
  @Version
  @Column(name = "STVTRMT_VERSION", nullable = false, precision = 19)
  Long version

  /**
   * Column for storing data origin for STVTRMT
   */
  @Column(name = "STVTRMT_DATA_ORIGIN")
  String dataOrigin



  public String toString() {
    "TermTypeForTesting[id=$id, code=$code, description=$description, lastModified=$lastModified, lastModifiedBy=$lastModifiedBy, version=$version, dataOrigin=$dataOrigin]"
  }

  static constraints = {
    code(nullable: false, maxSize: 1)
    description(nullable: false, maxSize: 30)
    lastModified(nullable: true)
    lastModifiedBy(nullable: true, maxSize: 30)
    dataOrigin(nullable: true, maxSize: 30)
  }


  boolean equals(o) {
    if (this.is(o)) return true

    if (!(o instanceof TermTypeForTesting)) return false

      TermTypeForTesting termType = (TermTypeForTesting) o

    if (code != termType.code) return false
    if (dataOrigin != termType.dataOrigin) return false
    if (description != termType.description) return false
    if (id != termType.id) return false
    if (lastModified != termType.lastModified) return false
    if (lastModifiedBy != termType.lastModifiedBy) return false
    if (version != termType.version) return false

    return true
  }


  int hashCode() {
    int result

    result = (id != null ? id.hashCode() : 0)
    result = 31 * result + (code != null ? code.hashCode() : 0)
    result = 31 * result + (description != null ? description.hashCode() : 0)
    result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
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
  /*PROTECTED REGION ID(termtype_custom_methods) ENABLED START*/

  /*PROTECTED REGION END*/
}
