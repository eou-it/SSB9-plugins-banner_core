/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Version
import javax.persistence.GenerationType
import javax.persistence.SequenceGenerator
import org.hibernate.annotations.Type

// Used to test that an underlying Oracle exception thrown from the driver 
// can be reported. This model is NOT persistable -- it is used for testing an exception. 
@Entity
@Table(name="SMRALIB")
class AreaLibraryForTesting implements Serializable {
	
	@Id
	@Column(name="SMRALIB_SURROGATE_ID")
    @SequenceGenerator(name = "SMRALIB_SEQ_GEN", allocationSize = 1, sequenceName = "SMRALIB_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SMRALIB_SEQ_GEN")
	Long id

	@Column(name="SMRALIB_AREA", nullable = false, length=10)
	String area

	@Column(name="SMRALIB_AREA_DESC", nullable = false, length=30)
	String areaDescription

	@Type(type = "yes_no")
	@Column(name="SMRALIB_DYNAMIC_IND", nullable = false)
	Boolean dynamicIndicator

	@Column(name="SMRALIB_PRINT_IND", nullable = false, length=1)
	String printIndicator

	@Column(name="SMRALIB_ACTIVITY_DATE", nullable = false)
	Date lastModified

	@Type(type = "yes_no")
	@Column(name="SMRALIB_COMPL_USAGE_IND", nullable = false)
	Boolean complUsageIndicator

	@Type(type = "yes_no")
	@Column(name="SMRALIB_PREREQ_USAGE_IND", nullable = false)
	Boolean prerequisiteUsageIndicator

	@Column(name="SMRALIB_PRESCR_USAGE_IND", length=1)
	String prescrUsageIndicator

	@Version
	@Column(name="SMRALIB_VERSION", nullable = false, length=19)
	Long version

	@Column(name="SMRALIB_USER_ID", length=30)
	String lastModifiedBy

	@Column(name="SMRALIB_DATA_ORIGIN", length=30)
	String dataOrigin

	
	public String toString() {
		"AreaLibraryForTesting[id=$id, area=$area, areaDescription=$areaDescription, dynamicIndicator=$dynamicIndicator, printIndicator=$printIndicator, lastModified=$lastModified, complUsageIndicator=$complUsageIndicator, prerequisiteUsageIndicator=$prerequisiteUsageIndicator, version=$version, lastModifiedBy=$lastModifiedBy, dataOrigin=$dataOrigin]"
	}
	

	static constraints = {
		area(                       nullable:false, maxSize:10 )
		areaDescription(            nullable:false, maxSize:30 )
		dynamicIndicator(           nullable:false )
		printIndicator(             nullable:false, maxSize:1 )
		lastModified(               nullable:false )
		complUsageIndicator(        nullable:false )
		prerequisiteUsageIndicator( nullable:false )
		lastModifiedBy(             nullable:true, maxSize:30 )
		dataOrigin(                 nullable:true, maxSize:30 ) 
    }
    
}
