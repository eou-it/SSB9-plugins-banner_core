
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

import javax.persistence.*

/**
 * General table for storing responses and  PIN questions
 */

@Entity
@Table(name = "GV_GOBANSR")
@NamedQueries(value = [
@NamedQuery(name = "GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm",
        query = """select count(a.answerDescription)
                   FROM GeneralForStoringResponsesAndPinQuestion a
                   WHERE  pidm = :pidm
                """),
@NamedQuery(name = "GeneralForStoringResponsesAndPinQuestion.fetchCountOfSameQuestionForPidm",
        query = """select count(a.questionDescription)
                   FROM GeneralForStoringResponsesAndPinQuestion a
                   WHERE  pidm = :pidm
                   AND questionDescription = :questionDescription
                """)
])
class GeneralForStoringResponsesAndPinQuestion implements Serializable {

	/**
	 * Surrogate ID for GOBANSR
	 */
	@Id
	@Column(name="GOBANSR_SURROGATE_ID")
	@SequenceGenerator(name ="GOBANSR_SEQ_GEN", allocationSize =1, sequenceName  ="GOBANSR_SURROGATE_ID_SEQUENCE")
	@GeneratedValue(strategy =GenerationType.SEQUENCE, generator ="GOBANSR_SEQ_GEN")
	Long id

	/**
	 * Optimistic lock token for GOBANSR
	 */
	@Version
	@Column(name = "GOBANSR_VERSION")
	Long version

	/**
	 * PIDM: PIDM of the self service user who created PIN responses.
	 */
	@Column(name = "GOBANSR_PIDM")
	Integer pidm

	/**
	 * NUM: Number of the question/answer stored
	 */
	@Column(name = "GOBANSR_NUM")
	Integer number

	/**
	 * QSTN_DESC: Descriptive questions
	 */
	@Column(name = "GOBANSR_QSTN_DESC")
	String questionDescription

	/**
	 * ANSR_DESC: Descriptive answer for the question
	 */
	@Column(name = "GOBANSR_ANSR_DESC")
	String answerDescription

	/**
	 * ANSR_SALT: Random value used in hashing answer
	 */
	@Column(name = "GOBANSR_ANSR_SALT")
	String answerSalt

	/**
	 * ACTIVITY_DATE: Date on which the record was created or last updated.
	 */
	@Column(name = "GOBANSR_ACTIVITY_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	Date lastModified

	/**
	 * USER_ID: ID of the user who created or last updated the record.
	 */
	@Column(name = "GOBANSR_USER_ID")
	String lastModifiedBy

	/**
	 * DATA_ORIGIN: Source system that created or updated the row
	 */
	@Column(name = "GOBANSR_DATA_ORIGIN")
	String dataOrigin


	/**
	 * Foreign Key : FK1_GOBANSR_INV_GOBQSTN_KEY
	 */
	@ManyToOne
	@JoinColumns([
		@JoinColumn(name="GOBANSR_GOBQSTN_ID", referencedColumnName="GOBQSTN_ID")
		])
	PinQuestion pinQuestion


	public String toString() {
		"""GeneralForStoringResponsesAndPinQuestion[
					id=$id,
					version=$version,
					pidm=$pidm,
					number=$number,
					questionDescription=$questionDescription,
					answerDescription=$answerDescription,
					answerSalt=$answerSalt,
					lastModified=$lastModified,
					lastModifiedBy=$lastModifiedBy,
					dataOrigin=$dataOrigin,
					pinQuestion=$pinQuestion]"""
	}


	boolean equals(o) {
	    if (this.is(o)) return true
	    if (!(o instanceof GeneralForStoringResponsesAndPinQuestion)) return false
	    GeneralForStoringResponsesAndPinQuestion that = (GeneralForStoringResponsesAndPinQuestion) o
        if(id != that.id) return false
        if(version != that.version) return false
        if(pidm != that.pidm) return false
        if(number != that.number) return false
        if(questionDescription != that.questionDescription) return false
        if(answerDescription != that.answerDescription) return false
        if(answerSalt != that.answerSalt) return false
        if(lastModified != that.lastModified) return false
        if(lastModifiedBy != that.lastModifiedBy) return false
        if(dataOrigin != that.dataOrigin) return false
        if(pinQuestion != that.pinQuestion) return false
        return true
    }


	int hashCode() {
		int result
	    result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (number != null ? number.hashCode() : 0)
        result = 31 * result + (questionDescription != null ? questionDescription.hashCode() : 0)
        result = 31 * result + (answerDescription != null ? answerDescription.hashCode() : 0)
        result = 31 * result + (answerSalt != null ? answerSalt.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (pinQuestion != null ? pinQuestion.hashCode() : 0)
        return result
	}

	static constraints = {
		pidm(nullable:false, min: -99999999, max: 99999999 )
		number(nullable:false, min: -99999999, max: 99999999 )
		questionDescription(nullable:true, maxSize:255)
		answerDescription(nullable:false, maxSize:255)
		answerSalt(nullable:false, maxSize:128)
		lastModified(nullable:true)
		lastModifiedBy(nullable:true, maxSize:30)
		dataOrigin(nullable:true, maxSize:30)
		pinQuestion(nullable:true)

    }
    //Read Only fields that should be protected against update
    public static readonlyProperties = [ 'pidm', 'number' ]

    static def fetchCountOfAnswersForPidm(Map map) {
        GeneralForStoringResponsesAndPinQuestion.withSession { session ->
            def generalForStoringResponsesAndPinQuestion = session.getNamedQuery('GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm')
                    .setInteger('pidm', map.pidm)
                    .list()[0]
            return generalForStoringResponsesAndPinQuestion
        }
    }

    static def fetchCountOfSameQuestionForPidm(Map map) {
        GeneralForStoringResponsesAndPinQuestion.withSession { session ->
            def generalForStoringResponsesAndPinQuestion = session.getNamedQuery('GeneralForStoringResponsesAndPinQuestion.fetchCountOfSameQuestionForPidm')
                    .setInteger('pidm', map.pidm)
                    .setString('questionDescription', map.questionDescription)
                    .list()[0]
            return generalForStoringResponsesAndPinQuestion
        }
    }
}
