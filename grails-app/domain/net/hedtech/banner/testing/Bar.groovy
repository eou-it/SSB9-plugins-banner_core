/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

// *** NOTICE: DO NOT CHANGE THIS ENTITY TO USE JPA -- IT SERVES AS A TEST OF 'PURE GORM' PERSISTENCE. ***
/**
 * A test domain that uses pure GORM (versus hibernate annotations) to define the mapping to the database table.
 **/
class Bar  {  // Based on the Banner 'Interest' model

    // Grails works best with an 'id' field for the key. The current support for natural keys is a bit ugly...
    String id

    String description
    Date activity_date = new Date()
    String system_required_indicator = "N" // this evidently cannot be private -- when it is hibernate cannot sort by this field


    void setCode( String code ) {
        id = code
    }


    String getCode() {
        id
    }


    // Virtual attribute allowing exposure of a boolean versus a string Y/N.  Note: This is an area where JPA provides a more elegant solution. 
    // FYI: Evidently we need to use the get/set naming convention to allow property access to virtual attributes from gsp templates
    def setSystemRequired( boolean reqd ) {
	      if (reqd) system_required_indicator = "Y"
	      else      system_required_indicator = "N"
    }


    boolean getSystemRequired() {
	     "Y" == system_required_indicator
    }


    static transients = ['code', 'systemRequired'] 

		static constraints = {
        id          ( unique:true, blank:false, nullable:false, maxSize:2, minSize:2 )
        code        ( blank:false, nullable:false, maxSize:2, minSize:2 ) // we can still define some constraints, just not those that expect 'code' to be persisted (e.g., unique)
		description ( unique:true, blank:false, maxSize:30 )
	}
	
	
    static mapping = {
	    table "stvints"
	    version false
	    id generator: 'assigned', column: 'stvints_code', type: 'string'
	    columns {
	        description column: "stvints_desc"
	        activity_date column: "stvints_activity_date"
	        system_required_indicator column: "stvints_system_req_ind" 
	    }  
  	}
	
}
