/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

import javax.persistence.*


/**
 * A model used for testing the Banner framework.
 */
@Entity
@Table(name="GTVZIPC")
class ZipForTesting implements Serializable {

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
		"ZipForTesting[id=$id, code=$code, city=$city, lastModifiedBy=$lastModifiedBy, version=$version, dataOrigin=$dataOrigin]"
	}


	static constraints = {
		code (          nullable: false, maxSize: 30 )
		city(           nullable: false, maxSize: 50 )
	    lastModified(   nullable: true )
		lastModifiedBy( nullable: true, maxSize: 30 )
		dataOrigin(     nullable: true, maxSize: 30 )
	}
}
