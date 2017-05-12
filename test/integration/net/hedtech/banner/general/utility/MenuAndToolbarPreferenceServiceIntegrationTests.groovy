/*******************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.utility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class MenuAndToolbarPreferenceServiceIntegrationTests extends BaseIntegrationTestCase {

  def MenuAndToolbarPreferenceService

  @Before
  public void setUp() {
    formContext = ['GUAGMNU']
    super.setUp()
  }

  @After
  public void tearDown() {
    super.tearDown()
  }

  @Test
  void testCreateMenuAndToolbarPreference() {
    def MenuAndToolbarPreference = newMenuAndToolbarPreference()
	MenuAndToolbarPreference = MenuAndToolbarPreferenceService.create(MenuAndToolbarPreference)
	assertNotNull "MenuAndToolbarPreference ID is null in MenuAndToolbarPreference Service Tests Create", MenuAndToolbarPreference.id
    assertNotNull MenuAndToolbarPreference.dataOrigin
	assertNotNull MenuAndToolbarPreference.lastModifiedBy
    assertNotNull MenuAndToolbarPreference.lastModified
  }

  @Test
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

  @Test
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
   @Test
    void testMenuAndToolbarPreferenceFetch() {
      def prefs = MenuAndToolbarPreferenceService.fetchMenuAndToolbarPreference()
      assertNotNull prefs
    }
  /*PROTECTED REGION END*/
}
