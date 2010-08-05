/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.supplemental

/**
 * Essentially a DAO for supplemental data. This strategy works against the
 * GOVSDAV view for both reading and writing supplemental data.
 */
class SupplementalDataPersistenceManager { // since we don't expect other strategies at this time, we'll forgo a formal interface


    def dataSource // injected by Spring
    

    public def loadSupplementalDataFor( model ) {
        println "TO BE IMPLEMENTED:  SupplementalDataPersistenceManager.loadSupplementalDataFor will load supplementalProperties for entity ${model.class} with id ${model.id}"
        throw new RuntimeException( "Not yet implemented!" )
    }



    public def persistSupplementalDataFor( model ) {
        println "TO BE IMPLEMENTED: SupplementalDataPersistenceManager.persistSupplementalDataFor will persist supplementalProperties ${model.supplementalProperties()} for entity ${model.class} with id ${model.id}"
        throw new RuntimeException( "Not yet implemented!" )
    }


    public def removeSupplementalDataFor( model ) {
        println "TO BE IMPLEMENTED: SupplementalDataPersistenceManager.removeSupplementalDataFor will remove supplementalProperties ${model.supplementalProperties()}"
        throw new RuntimeException( "Not yet implemented!" )
    }
}
