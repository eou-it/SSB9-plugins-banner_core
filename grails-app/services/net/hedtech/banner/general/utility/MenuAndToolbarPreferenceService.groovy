
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

package net.hedtech.banner.general.utility

import org.springframework.security.core.context.SecurityContextHolder
// NOTE:
// This service is injected with create, update, and delete methods that may throw runtime exceptions (listed below).  
// These exceptions must be caught and handled by the controller using this service.
// 
// update and delete may throw net.hedtech.banner.exceptions.NotFoundException if the entity cannot be found in the database
// update and delete may throw org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException a runtime exception if an optimistic lock failure occurs
// create, update, and delete may throw grails.validation.ValidationException a runtime exception when there is a validation failure

//TODO:Can be deleted
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
