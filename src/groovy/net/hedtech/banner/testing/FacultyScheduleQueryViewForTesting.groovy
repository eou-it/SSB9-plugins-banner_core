/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package  net.hedtech.banner.testing

import org.hibernate.annotations.Formula
import org.hibernate.annotations.Type
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import javax.persistence.Column
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

@Entity
@Table(name = "SVQ_SIVASGQ")
class FacultyScheduleQueryViewForTesting {
    /**
     * Surrogate ID for SIBINST
     */
    @Id
    @Column(name = "SIVASGQ_ROWID")
    String id

    /**
     * Optimistic lock token for SIBINST
     */
    @Version
    @Column(name = "SIVASGQ_VERSION")
    Long version

    /**
     * Foreign Key : FK1_SIVASGQ_INV_STVTERM_CODE
     */

    @Column(name = "SIVASGQ_TERM_CODE")
    String term

    /**
     * This field is   the Course Reference Number for the course section for which you are creating meeting times
     */
    @Column(name = "SIVASGQ_CRN")
    String courseReferenceNumber

    /**
     * The Pidm of the faculty member
     */
    @Column(name = "SIVASGQ_PIDM")
    Integer pidm

    /**
     * Faculty name and ID from spriden
     */
    @Column(name = "SIVASGQ_ID")
    String bannerId

    @Column(name = "SIVASGQ_LAST_NAME")
    String lastName

    @Column(name = "SIVASGQ_FIRST_NAME")
    String firstName

    @Column(name = "SIVASGQ_MI")
    String middleInitial

/**
 * Section Meeting Start Date.
 */
    @Column(name = "SIVASGQ_START_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date startDate

/**
 * Section End Date.
 */
    @Column(name = "SIVASGQ_END_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date endDate

/**
 * This field defines the Begin Time of the course section being scheduled.  It is a required field and is in the format HHMM using military times.  The SSRSECT (Schedule of Classes) converts this time to standard times.
 */
    @Column(name = "SIVASGQ_BEGIN_TIME")
    String beginTime

/**
 * This field defines the End Time of the course section being scheduled.  It is a required field and is in the format HHMM using military times.  The SSRSECT (Schedule of Classes) converts this time to standard times.
 */
    @Column(name = "SIVASGQ_END_TIME")
    String endTime

    /**
     * The session indicator associated with the assignment
     */
    @Column(name = "SIVASGQ_CATEGORY")
    String category

/**
 * This field defines the Room where the course section will be scheduled.  It is not required when scheduling course section meeting times.  It is required when scheduling a course section meeting building.
 */
    @Column(name = "SIVASGQ_ROOM_CODE")
    String room

    /**
     * Foreign Key : FK1_SIVASGQ_INV_STVBLDG_CODE
     */

    @Column(name = "SIVASGQ_BLDG_CODE")
    String building

    /**
     * Section Meeting Time Sunday Indicator.
     */
    @Formula(value = "decode(nvl(SIVASGQ_SUN_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean sunday

/**
 * Section Meeting Time Monday Indicator.
 */
    @Formula(value = "decode(nvl(SIVASGQ_MON_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean monday

/**
 * Section Meeting Time Tuesday Indicator.
 */

    @Formula(value = "decode(nvl(SIVASGQ_TUE_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean tuesday
/**
 * Section Meeting Time Wednesday Indicator.
 */

    @Formula(value = "decode(nvl(SIVASGQ_WED_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean wednesday

/**
 * Section Meeting Time Thrusday Indicator.
 */

    @Formula(value = "decode(nvl(SIVASGQ_THU_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean thursday

/**
 * Section Meeting Time Friday Indicator.
 */

    @Formula(value = "decode(nvl(SIVASGQ_FRI_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean friday

/**
 * Section Meeting Time Saturday Indicator.
 */

    @Formula(value = "decode(nvl(SIVASGQ_SAT_DAY,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean saturday

/**
 * Section Time Conflict Override Indicator.
 */

    @Formula(value = "decode(nvl(SIVASGQ_OVER_RIDE,'N'),'N','N','Y')")
    @Type(type = "yes_no")
    Boolean  override

    @Column(name = "SIVASGQ_SUBJ_CODE")
    String subject

    @Column(name = "SIVASGQ_CRSE_NUMB")
    String courseNumber

    @Column(name = "SIVASGQ_seq_numb")
    String sequenceNumber


    @Column(name = "SIVASGQ_PTRM_CODE")
    String partOfTerm


    @Column(name = "SIVASGQ_XLST_CODE")
    String xlstGroup

    /**
     * The session credit hours
     */
    @Column(name = "SIVASGQ_CREDIT_HR_SESS", precision = 7, scale = 3)
    Double creditHourSession

    @Column(name = "SIVASGQ_SCHD_CODE", nullable = true, length = 3)
    String scheduleType

    @Column(name = "SIVASGQ_max_enrl")
    Integer maximumEnrollment = 0

    /**
     * This field is system maintained.  It displays a running total of enrollments in the section which have a
     * course status with a 'Count in Enroll' flag of 'Y' on the Registration Status Code Validation Form - STVRSTS.
     */
    @Column(name = "SIVASGQ_enrl")
    Integer enrollment = 0

    @Column(name = "SIVASGQ_MTYP_CODE")
    String meetingType

    /**
     * rowid of the meeting time record
     */

    @Column(name="SIVASGQ_MEETING_ROWID")
    String meetingRowid

    /**
     * To string
     * @return
     */

    public String toString() {
        """FacultyScheduleQueryViewForTesting[
				id=$id,
				version=$version,
				pidm=$pidm,
				term=$term,
				crn=$courseReferenceNumber,
				BannerID=$bannerId
                lastName=$lastName
                firstName=$firstName
                middleInitial=$middleInitial   ,
                beginTime=$beginTime,
                endTime=$endTime,
                room=$room,
                startDate=$startDate,
                endDate=$endDate,
                category=$category,
                sunday=$sunday,
                monday=$monday,
                tuesday=$tuesday,
                wednesday=$wednesday,
                thursday=$thursday,
                friday=$friday,
                saturday=$saturday,
                override=$override,
                creditHourSession=$creditHourSession,
                subject:$subject,
		        courseNumber=$courseNumber,
		        sequenceNumber=$sequenceNumber,
		        scheduleType=$scheduleType,
		        partofTerm=$partOfTerm,
		        xlstGroup=$xlstGroup,
                maximumEnrollment=$maximumEnrollment,
		        enrollment=$enrollment ,
		        meetingRowid=$meetingRowid
                ]"""
    }

    /**
     * Query finder for advanced search SIAASGQ
     */

    def static countAll(filterData) {
        finderByAll().count(filterData)
    }


    def static fetchSearch(filterData, pagingAndSortParams) {
        finderByAll().find(filterData, pagingAndSortParams)
    }

}
