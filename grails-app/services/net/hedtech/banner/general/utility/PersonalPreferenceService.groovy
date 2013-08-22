
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

class PersonalPreferenceService {

    boolean transactional = true

	static defaultCrudMethods = true
    
    /**
     * Please put all the custom methods in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(personalpreference_custom_service_methods) ENABLED START*/
   def List fetchPersonalPreference(String group, String key, String string)  {
     def user = SecurityContextHolder.context?.authentication?.principal?.username?.toUpperCase()

     def prefs =[]

       try {
           prefs = PersonalPreference.fetchPersonalPreferencesByKey(group,key,string,user)
       }catch(Exception ex){ }

     if (prefs?.size() <= 0)
       prefs = PersonalPreference.fetchPersonalPreferencesByKey(group,key,string,"BASELINE")
     return prefs
  }
    /*PROTECTED REGION END*/
}
