/** *****************************************************************************
 � 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import javax.persistence.SequenceGenerator
import javax.persistence.GenerationType

/**
 * Outside Interest Code Validation Table
 */
//TODO: NamedQueries that needs to be ported:
/**
 * Where clause on this entity present in forms:
 * Order by clause on this entity present in forms:
 * Form Name: STVINTS
 *  stvints_code

 */
@Entity
@Table(name = "STVINTS")
class Interest implements Serializable {

    /**
     * Surrogate ID for STVINTS
     */
    @Id
    @Column(name = "STVINTS_SURROGATE_ID")
    @SequenceGenerator(name = "STVINTS_SEQ_GEN", allocationSize = 1, sequenceName = "STVINTS_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STVINTS_SEQ_GEN")
    Long id

    /**
     * This field identifies the outside interest code referenced on the Prospect      Information Form (SRARECR).
     */
    @Column(name = "STVINTS_CODE", nullable = false, length = 2)
    String code

    /**
     * This field specifies the outside interest associated with the outside interest  code.
     */
    @Column(name = "STVINTS_DESC", length = 30)
    String description

    /**
     * This field identifies the most recent date a record was created or updated.
     */
    @Column(name = "STVINTS_activity_date")
    Date lastModified

    /**
     * System Required Indicator
     */
    @Column(name = "STVINTS_SYSTEM_REQ_IND", length = 1)
    String systemRequiredIndicator

    /**
     * Version column which is used as a optimistic lock token for STVINTS
     */
    @Version
    @Column(name = "STVINTS_VERSION", nullable = false, length = 19)
    Long version

    /**
     * Last Modified By column for STVINTS
     */
    @Column(name = "STVINTS_USER_ID", length = 30)
    String lastModifiedBy

    /**
     * Data Origin column for STVINTS
     */
    @Column(name = "STVINTS_DATA_ORIGIN", length = 30)
    String dataOrigin



    public String toString() {
        " Interests[  " +
				"	id=$id,   " +
				"	code=$code,   " +
				"	description=$description,    " +
				"	lastModified=$lastModified,    " +
				"	systemRequiredIndicator=$systemRequiredIndicator,   " +
				"	version=$version,     " +
				"	lastModifiedBy=$lastModifiedBy,   " +
				"	dataOrigin=$dataOrigin  ]" 
    }


    static constraints = {
        code(nullable: false, maxSize: 2)
        description(nullable: true, maxSize: 30)
        lastModified(nullable:true)
        systemRequiredIndicator(nullable: true, maxSize: 1, inList: ["Y"])
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)

        /**
         * Please put all the custom tests in this protected section to protect the code
         * from being overwritten on re-generation
         */
        /*PROTECTED REGION ID(interests_custom_constraints) ENABLED START*/

        /*PROTECTED REGION END*/
    }

    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(interests_custom_methods) ENABLED START*/

    /*PROTECTED REGION END*/
}
