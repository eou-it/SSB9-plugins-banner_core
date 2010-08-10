/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.supplemental

import org.hibernate.persister.entity.SingleTableEntityPersister

/**
 * A service used to support persistence of supplemental data.
 */
class SupplementalDataService { 

    static transactional = true

    def sessionFactory                     // injected by Spring
    def supplementalDataPersistenceManager // injected by Spring

    def supplementalDataConfiguration = [:] // Just temporary -- this won't really be a map but more likely an XML configuration document


    public def getSupplementalDataConfigurationFor( Class modelClass ) {
        supplementalDataConfiguration."${modelClass.name}"
    }


    def init() {
        // Groovy SQL invocation of view


        // TODO refactor this to be more groovy !!!  -- also, revisit whether to do all at once during init() or do on a class-by-class basis as needed and cache?
        // Until we define the structure, we'll just dump this out...
        Map x = sessionFactory.getAllClassMetadata()
        for (Iterator i = x.values().iterator(); i.hasNext(); ) {
            SingleTableEntityPersister y = (SingleTableEntityPersister)i.next();
//            println( y.getName() + " -> " + y.getTableName() )
            for (int j = 0; j < y.getPropertyNames().length; j++) {
//                println( " " + y.getPropertyNames()[j] + " -> " + (y.getPropertyColumnNames( j ).length > 0 ? y.getPropertyColumnNames( j )[ 0 ] : ""))
            } 
        }
        println "SupplementalDataService initialization complete."
    }

    
    /**
     * Appends additional supplemental data configuration for a model. This is used for testing purposes.
     * @param map the additional supplemental data configuration in the form: [ modelClass: [ propertyName: [ required: boolean, dataType: someType ], ], ] 
     */
    public void appendSupplementalDataConfiguration( Map map ) {
        supplementalDataConfiguration << map
    }


    public boolean supportsSupplementalProperties( Class modelClass ) {
        supplementalDataConfiguration.keySet().contains modelClass.name
    }
    

    public List supplementalPropertyNamesFor( Class modelClass ) {
       supplementalDataConfiguration."${modelClass.name}".keySet().asList()
    }


    public boolean hasSupplementalProperties( modelInstance ) {
        modelInstance.hasSupplementalProperties()    
    }


    public def loadSupplementalDataFor( model ) {
        supplementalDataPersistenceManager.loadSupplementalDataFor( model )
    }


    public def persistSupplementalDataFor( model ) {
        removeUnsupportedPropertiesFrom( model )
        supplementalDataPersistenceManager.persistSupplementalDataFor( model )
    }


    public def removeSupplementalDataFor( model ) {
        supplementalDataPersistenceManager.removeSupplementalDataFor( model )
    }


    private def removeUnsupportedPropertiesFrom( model ) {
        def supportedNames = supplementalPropertyNamesFor( model.class )
        def supportedProperties = model.supplementalProperties?.findAll { k, v -> k in supportedNames }
        model.supplementalProperties = supportedProperties
    }
}
