
/*******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.

 This copyrighted software contains confidential and proprietary information of
 SunGard Higher Education and its subsidiaries. Any use of this software is limited
 solely to SunGard Higher Education licensees, and is further subject to the terms
 and conditions of one or more written license agreements between SunGard Higher
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher
 Education in the U.S.A. and/or other regions and/or countries.
 *******************************************************************************/
/**
 Banner Automator Version: 1.29
 Generated: Thu Aug 01 15:12:51 IST 2013
 */
package net.hedtech.banner.general.overall

import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * General table for storing list of PIN questions
 */

@Entity
@Table(name = "GV_GOBQSTN")
class PinQuestion implements Serializable {

	/**
	 * Surrogate ID for GOBQSTN
	 */
	@Id
	@Column(name="GOBQSTN_SURROGATE_ID")
	@SequenceGenerator(name ="GOBQSTN_SEQ_GEN", allocationSize =1, sequenceName  ="GOBQSTN_SURROGATE_ID_SEQUENCE")
	@GeneratedValue(strategy =GenerationType.SEQUENCE, generator ="GOBQSTN_SEQ_GEN")
	Long id

	/**
	 * Optimistic lock token for GOBQSTN
	 */
	@Version
	@Column(name = "GOBQSTN_VERSION")
	Long version

	/**
	 * ID: Unique identifier for the question.
	 */
	@Column(name = "GOBQSTN_ID")
	String pinQuestionId

	/**
	 * DESC: Descriptive questions
	 */
	@Column(name = "GOBQSTN_DESC")
	String description

	/**
	 * DISPLAY_IND: Descriptive questions
	 */
	@Type(type = "yes_no")
	@Column(name = "GOBQSTN_DISPLAY_IND")
	Boolean displayIndicator

	/**
	 * ACTIVITY_DATE: Date on which the record was created or last updated.
	 */
	@Column(name = "GOBQSTN_ACTIVITY_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	Date lastModified

	/**
	 * USER_ID: ID of the user who created or last updated the record.
	 */
	@Column(name = "GOBQSTN_USER_ID")
	String lastModifiedBy

	/**
	 * DATA_ORIGIN: Source system that created or updated the row
	 */
	@Column(name = "GOBQSTN_DATA_ORIGIN")
	String dataOrigin



	public String toString() {
		"""PinQuestion[
					id=$id,
					version=$version,
					pinQuestionId=$pinQuestionId,
					description=$description,
					displayIndicator=$displayIndicator,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin]"""
	}


	boolean equals(o) {
	    if (this.is(o)) return true
	    if (!(o instanceof PinQuestion)) return false
	    PinQuestion that = (PinQuestion) o
        if(id != that.id) return false
        if(version != that.version) return false
        if(pinQuestionId != that.pinQuestionId) return false
        if(description != that.description) return false
        if(displayIndicator != that.displayIndicator) return false
        if(lastModified != that.lastModified) return false
        if(lastModifiedBy != that.lastModifiedBy) return false
        if(dataOrigin != that.dataOrigin) return false
        return true
    }


	int hashCode() {
		int result
	    result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pinQuestionId != null ? pinQuestionId.hashCode() : 0)
        result = 31 * result + (description != null ? description.hashCode() : 0)
        result = 31 * result + (displayIndicator != null ? displayIndicator.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        return result
	}

	static constraints = {
		description(nullable:false, maxSize:255)
		displayIndicator(nullable:false)
		lastModified(nullable:true)
		lastModifiedBy(nullable:true, maxSize:30)
		dataOrigin(nullable:true, maxSize:30)

    }
    //Read Only fields that should be protected against update
    public static readonlyProperties = [ 'pinQuestionId' ]

}
