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
package com.sungardhe.banner.testing

import javax.persistence.*


/**
 * A model used for testing the Banner framework.
 */
@Entity
@Table(name="GTVZIPC")
class Zip implements Serializable {

	@Id
	@Column(name="GTVZIPC_SURROGATE_ID")
    @SequenceGenerator(name = "GTVZIPC_SEQ_GEN", allocationSize = 1, sequenceName = "GTVZIPC_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GTVZIPC_SEQ_GEN")
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
		"Zip[id=$id, code=$code, city=$city, lastModifiedBy=$lastModifiedBy, version=$version, dataOrigin=$dataOrigin]"
	}


	static constraints = {
		code (          nullable: false, maxSize: 30 )
		city(           nullable: false, maxSize: 50 )
	    lastModified(   nullable: true )
		lastModifiedBy( nullable: true, maxSize: 30 )
		dataOrigin(     nullable: true, maxSize: 30 )
	}
}
