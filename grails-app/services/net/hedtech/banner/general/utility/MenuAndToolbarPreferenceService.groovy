
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

package net.hedtech.banner.general.utility

import org.springframework.security.core.context.SecurityContextHolder
// NOTE:
// This service is injected with create, update, and delete methods that may throw runtime exceptions (listed below).  
// These exceptions must be caught and handled by the controller using this service.
// 
// update and delete may throw net.hedtech.banner.exceptions.NotFoundException if the entity cannot be found in the database
// update and delete may throw org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException a runtime exception if an optimistic lock failure occurs
// create, update, and delete may throw grails.validation.ValidationException a runtime exception when there is a validation failure

class MenuAndToolbarPreferenceService {

    boolean transactional = true

	static defaultCrudMethods = true
    
    /**
     * Please put all the custom methods in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(preferencethatstorestoolbarandmenuinfog_custom_service_methods) ENABLED START*/
      def List fetchMenuAndToolbarPreference()  {
      def currentUser = SecurityContextHolder.context?.authentication?.principal?.username?.toUpperCase()
      def prefs = MenuAndToolbarPreference.fetchMenuAndToolbarPreferenceByUser(currentUser)
      if (prefs.size() <= 0)
        prefs = MenuAndToolbarPreference.fetchMenuAndToolbarPreferenceByUser("BASELINE")

     return prefs
  }
    /*PROTECTED REGION END*/
}
