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

import org.springframework.security.core.context.SecurityContextHolder


class MenuAndToolbarPreferenceServiceIntegrationTests extends BaseIntegrationTestCase {

  def MenuAndToolbarPreferenceService

  protected void setUp() {
    formContext = ['GJAJPRF']
    super.setUp()
  }

  protected void tearDown() {
    super.tearDown()
  }


  void testCreateMenuAndToolbarPreference() {
    def MenuAndToolbarPreference = newMenuAndToolbarPreference()
	MenuAndToolbarPreference = MenuAndToolbarPreferenceService.create(MenuAndToolbarPreference)
	assertNotNull "MenuAndToolbarPreference ID is null in MenuAndToolbarPreference Service Tests Create", MenuAndToolbarPreference.id
    assertNotNull MenuAndToolbarPreference.dataOrigin
	assertNotNull MenuAndToolbarPreference.lastModifiedBy
    assertNotNull MenuAndToolbarPreference.lastModified
  }

  void testUpdate() {
    def MenuAndToolbarPreference = newMenuAndToolbarPreference()
    MenuAndToolbarPreference = MenuAndToolbarPreferenceService.create(MenuAndToolbarPreference)
	// create new values for the fields
	def itlbBtn = ""
	def idisplayHtCb = "X"
	def idisplayVtCb = "X"
	def idisplayHint = "X"
	def iformnameCb = "X"
	def ireleaseCb = "X"
	def idbaseInstitutionCb = "X"
	def idateTimeCb = "X"
	def irequiredItemCb = "X"
	def ilinescrnXPosition = 9 
	def ilinebtnXPosition = 9 
	def iformnameDisplayIndicator = "Y"

    // change the values 
	MenuAndToolbarPreference.tlbBtn = itlbBtn
	MenuAndToolbarPreference.displayHtCb = idisplayHtCb
	MenuAndToolbarPreference.displayVtCb = idisplayVtCb
	MenuAndToolbarPreference.displayHint = idisplayHint
	MenuAndToolbarPreference.formnameCb = iformnameCb
	MenuAndToolbarPreference.releaseCb = ireleaseCb
	MenuAndToolbarPreference.dbaseInstitutionCb = idbaseInstitutionCb
	MenuAndToolbarPreference.dateTimeCb = idateTimeCb
	MenuAndToolbarPreference.requiredItemCb = irequiredItemCb
	MenuAndToolbarPreference.linescrnXPosition = ilinescrnXPosition
	MenuAndToolbarPreference.linebtnXPosition = ilinebtnXPosition
	MenuAndToolbarPreference.formnameDisplayIndicator = iformnameDisplayIndicator
    
    MenuAndToolbarPreference = MenuAndToolbarPreferenceService.update(MenuAndToolbarPreference)
    // test the values
	//assertEquals itlbBtn, MenuAndToolbarPreference.tlbBtn
	assertEquals idisplayHtCb, MenuAndToolbarPreference.displayHtCb
	assertEquals idisplayVtCb, MenuAndToolbarPreference.displayVtCb
	assertEquals idisplayHint, MenuAndToolbarPreference.displayHint
	assertEquals iformnameCb, MenuAndToolbarPreference.formnameCb
	assertEquals ireleaseCb, MenuAndToolbarPreference.releaseCb
	assertEquals idbaseInstitutionCb, MenuAndToolbarPreference.dbaseInstitutionCb
	assertEquals idateTimeCb, MenuAndToolbarPreference.dateTimeCb
	assertEquals irequiredItemCb, MenuAndToolbarPreference.requiredItemCb
	assertEquals ilinescrnXPosition, MenuAndToolbarPreference.linescrnXPosition
	assertEquals ilinebtnXPosition, MenuAndToolbarPreference.linebtnXPosition
	assertEquals iformnameDisplayIndicator, MenuAndToolbarPreference.formnameDisplayIndicator
  }
	 
  void testMenuAndToolbarPreferenceDelete() {
	 def MenuAndToolbarPreference = newMenuAndToolbarPreference()
	 MenuAndToolbarPreference = MenuAndToolbarPreferenceService.create(MenuAndToolbarPreference)
	 
	 def id = MenuAndToolbarPreference.id
	 MenuAndToolbarPreferenceService.delete(id)
	 
	 assertNull "MenuAndToolbarPreference should have been deleted", MenuAndToolbarPreference.get(id)
  }

  private def newMenuAndToolbarPreference() {
    def MenuAndToolbarPreference = new MenuAndToolbarPreference(
    		tlbBtn: "", 
    		displayHtCb: "T", 
    		displayVtCb: "T", 
    		displayHint: "T", 
    		formnameCb: "T", 
    		releaseCb: "T", 
    		dbaseInstitutionCb: "T", 
    		dateTimeCb: "T", 
    		requiredItemCb: "T", 
    		linescrnXPosition: 1,
            linebtnXPosition: 1,
            formnameDisplayIndicator: "Y"

    )
    return MenuAndToolbarPreference
  }
  
  /**
   * Please put all the custom service tests in this protected section to protect the code
   * from being overwritten on re-generation
  */
  /*PROTECTED REGION ID(MenuAndToolbarPreference_custom_service_integration_test_methods) ENABLED START*/
    void testMenuAndToolbarPreferenceFetch() {
      def prefs = MenuAndToolbarPreferenceService.fetchMenuAndToolbarPreference()
      println prefs.get(0).formnameDisplayIndicator
      assertNotNull prefs
    }
  /*PROTECTED REGION END*/
}  
