/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.general.utility
import com.sungardhe.banner.testing.BaseIntegrationTestCase




class PersonalPreferenceServiceIntegrationTests extends BaseIntegrationTestCase {

  def personalPreferenceService

  protected void setUp() {
    formContext = ['GJAJPRF']
    super.setUp()
  }

  protected void tearDown() {
    super.tearDown()
  }


  void testCreatePersonalPreference() {
    def personalPreference = newPersonalPreference()
	personalPreference = personalPreferenceService.create(personalPreference)
	assertNotNull "PersonalPreference ID is null in PersonalPreference Service Tests Create", personalPreference.id
    assertNotNull personalPreference.dataOrigin 
	assertNotNull personalPreference.lastModifiedBy 
    assertNotNull personalPreference.lastModified 
  }

  void testUpdate() {
    def personalPreference = newPersonalPreference()
    personalPreference = personalPreferenceService.create(personalPreference)
	// create new values for the fields
	def igroup = "XXXXX"
	def ikey = "XXXXX"
	def istring = "XXXXX"
	def ivalue = "XXXXX"
	def isystemRequiredIndicator = false
    // change the values 
	personalPreference.group = igroup
	personalPreference.key = ikey
	personalPreference.string = istring
	personalPreference.value = ivalue
	personalPreference.systemRequiredIndicator = isystemRequiredIndicator
    personalPreference = personalPreferenceService.update(personalPreference)
    // test the values
	assertEquals igroup, personalPreference.group 
	assertEquals ikey, personalPreference.key 
	assertEquals istring, personalPreference.string 
	assertEquals ivalue, personalPreference.value 
	assertEquals isystemRequiredIndicator, personalPreference.systemRequiredIndicator 
  }
	 
  void testPersonalPreferenceDelete() {	 
	 def personalPreference = newPersonalPreference()
	 personalPreference = personalPreferenceService.create(personalPreference)
	 
	 def id = personalPreference.id
	 personalPreferenceService.delete(id)
	 
	 assertNull "PersonalPreference should have been deleted", personalPreference.get(id)
  }

  private def newPersonalPreference() {
    def personalPreference = new PersonalPreference(
    		group: "TTTTT", 
    		key: "TTTTT", 
    		string: "TTTTT", 
    		value: "TTTTT", 
    		systemRequiredIndicator: true,

    )
    return personalPreference
  }
  
  /**
   * Please put all the custom service tests in this protected section to protect the code
   * from being overwritten on re-generation
  */
  /*PROTECTED REGION ID(personalpreference_custom_service_integration_test_methods) ENABLED START*/
   void testPersonalPreferenceFetch() {
      def prefs = personalPreferenceService.fetchPersonalPreference("MENU","WIN32COMMON","STARTUP_MENU")
      assertNotNull prefs
    }
  /*PROTECTED REGION END*/
}  
