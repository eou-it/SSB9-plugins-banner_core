/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import javax.persistence.*

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type


/**
 * A model used for testing the Banner framework.
 */
@Entity
@Table(name="GTVZIPC")  
class Zip implements Serializable {

	@Id
	@Column(name="GTVZIPC_SURROGATE_ID")
	@GeneratedValue(generator ="triggerAssigned")
	@GenericGenerator(name = "triggerAssigned", strategy = "com.sungardhe.banner.framework.persistence.util.TriggerAssignedIdentityGenerator")
	Long id

	@Column(name="GTVZIPC_CODE", nullable = false)
	String code

	@Column(name="GTVZIPC_CITY", nullable = false, length=30)
	String city

	@Version
	@Column(name="GTVZIPC_VERSION", nullable = false, length=19)
	Long version

	@Column(name="GTVZIPC_ACTIVITY_DATE", nullable = true)
	Date lastModified

	@Column(name="GTVZIPC_USER_ID", length=30, nullable = true)
	String lastModifiedBy 

	@Column(name="GTVZIPC_DATA_ORIGIN", length=30, nullable = true)
	String dataOrigin


	public String toString() {
		"Zip[id=$id, code=$code, city=$description, lastModifiedBy=$lastModifiedBy, version=$version, dataOrigin=$dataOrigin]"
	}
	

	static constraints = {
		code(nullable: false, maxSize: 30) 
		city(nullable: false, maxSize: 50)
	    lastModified(nullable: true)
		lastModifiedBy(nullable: true, maxSize: 30)
		dataOrigin(nullable: true, maxSize: 30)
	}
}
