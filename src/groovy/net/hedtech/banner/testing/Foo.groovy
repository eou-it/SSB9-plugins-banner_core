/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

import net.hedtech.banner.service.DatabaseModifiesState
import javax.persistence.*

/**
 * A model used for testing the Banner framework.
 */
@Entity
@Table( name="STVCOLL" )
// The 'DatabaseModifiesState' annotation is 'normally' used to indicate that the database modifies the object after it is saved.
// This annotation is used by ServiceBase to indicate models that must be refreshed after saving. In this case, Foo (aka College) 
// is not really  modified in the database, but we use the annotation here to force testing of this ServiceBase functionality.
@DatabaseModifiesState 
class Foo implements Serializable { // based on 'College'

	@Id
	@Column( name="STVCOLL_SURROGATE_ID" )
    @SequenceGenerator(name = "STVCOLL_SEQ_GEN", allocationSize = 1, sequenceName = "STVCOLL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STVCOLL_SEQ_GEN")
	Long id

	@Column( name="STVCOLL_CODE", nullable = false ) // , length=2
	String code

	@Column( name="STVCOLL_DESC", nullable = false, length=30 )
	String description

	@Column( name="STVCOLL_ADDR_STREET_LINE1", length=75 )
	String addressStreetLine1

	@Column( name="STVCOLL_ADDR_STREET_LINE2", length=75 )
	String addressStreetLine2

	@Column( name="STVCOLL_ADDR_STREET_LINE3", length=75 )
	String addressStreetLine3

	@Column( name="STVCOLL_ADDR_CITY", length=50 )
	String addressCity

	@Column( name="STVCOLL_ADDR_STATE", length=2 )
	String addressState

	@Column( name="STVCOLL_ADDR_COUNTRY", length=28 )
	String addressCountry

	@Column( name="STVCOLL_ADDR_ZIP_CODE", length=10 )
	String addressZipCode

	@Column ( name="STVCOLL_SYSTEM_REQ_IND", nullable = true, length = 1 )
	String systemRequiredIndicator

	@Column( name="STVCOLL_VR_MSG_NO", length=22 )
	BigDecimal voiceResponseMessageNumber

	@Column( name="STVCOLL_STATSCAN_CDE3", length=6 )
	String statisticsCanadianInstitution

	@Column( name="STVCOLL_DICD_CODE", length=3 )
	String districtDivision

	@Column( name="STVCOLL_HOUSE_NUMBER", length=10 )
	String houseNumber

	@Column( name="STVCOLL_ADDR_STREET_LINE4", length=75 )
	String addressStreetLine4

	@Version
	@Column( name="STVCOLL_VERSION", nullable = false, length=19 )
	Long version

	@Column( name="STVCOLL_ACTIVITY_DATE", nullable = true )
	Date lastModified

	@Column( name="STVCOLL_USER_ID", length=30, nullable = true )
	String lastModifiedBy

	@Column( name="STVCOLL_DATA_ORIGIN", length=30, nullable = true )
	String dataOrigin

    @Transient
    Foo childByDefault

    @Transient
    Foo childByCode

    @Transient
    Foo childById

	public String toString() {
		"Foo[id=$id, code=$code, description=$description, addressStreetLine1=$addressStreetLine1, addressStreetLine2=$addressStreetLine2, addressStreetLine3=$addressStreetLine3, addressCity=$addressCity, addressState=$addressState, addressCountry=$addressCountry, addressZipCode=$addressZipCode, lastModified=$lastModified, systemRequiredIndicator=$systemRequiredIndicator, voiceResponseMessageNumber=$voiceResponseMessageNumber, statisticsCanadianInstitution=$statisticsCanadianInstitution, districtDivision=$districtDivision, houseNumber=$houseNumber, addressStreetLine4=$addressStreetLine4, lastModifiedBy=$lastModifiedBy, version=$version, dataOrigin=$dataOrigin]"
	}

    public static readonlyProperties = [ 'addressCountry', 'addressZipCode', 'districtDivision' ]

	static constraints = {
		code                          ( nullable: false, maxSize: 2  )
		description                   ( nullable: false, maxSize: 30 )
		addressStreetLine1            ( nullable: true,  maxSize: 75 )
		addressStreetLine2            ( nullable: true,  maxSize: 75 )
		addressStreetLine3            ( nullable: true,  maxSize: 75 )
		addressCity                   ( nullable: true,  maxSize: 50 )
		addressState                  ( nullable: true,  maxSize: 2  )
		addressCountry                ( nullable: true,  maxSize: 28 )
		addressZipCode                ( nullable: true,  maxSize: 10 )
		systemRequiredIndicator       ( nullable: true,  maxSize: 1, inList:['Y','N'] )
		voiceResponseMessageNumber    ( nullable: true,  maxSize: 22 )
		statisticsCanadianInstitution ( nullable: true,  maxSize: 6  )
		districtDivision              ( nullable: true,  maxSize: 3  )
		houseNumber                   ( nullable: true,  maxSize:10  )
		addressStreetLine4            ( nullable: true,  maxSize: 75 )
	    lastModified                  ( nullable: true )
		lastModifiedBy                ( nullable: true,  maxSize: 30 )
		dataOrigin                    ( nullable: true,  maxSize: 30 )
        childByDefault                ( nullable: true,  validProperty: true   )
        childByCode                   ( nullable: true,  validProperty: "code" )
        childById                     ( nullable: true,  validProperty: "id"   )
	}
}
