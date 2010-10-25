
/*******************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 *******************************************************************************/

package com.sungardhe.banner.general.utility

import org.springframework.security.core.context.SecurityContextHolder
// NOTE:
// This service is injected with create, update, and delete methods that may throw runtime exceptions (listed below).  
// These exceptions must be caught and handled by the controller using this service.
// 
// update and delete may throw com.sungardhe.banner.exceptions.NotFoundException if the entity cannot be found in the database
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
