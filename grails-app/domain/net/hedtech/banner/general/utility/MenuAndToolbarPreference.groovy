
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

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import javax.persistence.NamedQuery
import javax.persistence.NamedQueries

/**
 * PREFERENCE TABLE THAT STORES TOOLBAR AND MENU INFOG
 */
//TODO: NamedQueries that needs to be ported:
 /**
    * Where clause on this entity present in forms:
  * Order by clause on this entity present in forms:
*/
@Entity
@Table(name="GURTPRF")
@NamedQueries(value = [
@NamedQuery(name = "MenuAndToolbarPreference.fetchByUser",
query = """FROM   MenuAndToolbarPreference a
		   WHERE    a.lastModifiedBy = :lastModifiedBy
	      """)
])
class MenuAndToolbarPreference implements Serializable {
	
	/**
	 * Surrogate ID for GURTPRF
	 */
	@Id
	@Column(name="GURTPRF_SURROGATE_ID")
	@GeneratedValue(generator ="triggerAssigned")
	@GenericGenerator(name = "triggerAssigned", strategy = "net.hedtech.banner.framework.persistence.util.TriggerAssignedIdentityGenerator")
	Long id

	/**
	 * User ID:The last user to update the record
	 */
	@Column(name="GURTPRF_USER_ID", length=30)
	String lastModifiedBy

	/**
	 * This indicator column determines whether the name of a form to be called is displayed in the M=Options Menu, F=Navigation Frame, B=both, N=neither.
	 */
	@Column(name="GURTPRF_TLB_BTN", length=0)
	String tlbBtn

	/**
	 * Display Horizontal toolbar on MDI window
	 */
	@Column(name="GURTPRF_DISP_HT_CB", length=1)
	String displayHtCb

	/**
	 * Display Vertical tool bar on MDI window
	 */
	@Column(name="GURTPRF_DISP_VT_CB", length=1)
	String displayVtCb

	/**
	 * Display hint on buttons
	 */
	@Column(name="GURTPRF_DISP_HINT", length=1)
	String displayHint

	/**
	 * Display Form name on window ba
	 */
	@Column(name="GURTPRF_FORMNAME_CB", length=1)
	String formnameCb

	/**
	 * Display release number on window bar
	 */
	@Column(name="GURTPRF_RELEASE_CB", length=1)
	String releaseCb

	/**
	 * Display database instance on window bar
	 */
	@Column(name="GURTPRF_DBASE_INST_CB", length=1)
	String dbaseInstitutionCb

	/**
	 * Display date and time on window bar
	 */
	@Column(name="GURTPRF_DATE_TIME_CB", length=1)
	String dateTimeCb

	/**
	 * Display required items with different color attribute
	 */
	@Column(name="GURTPRF_REQ_ITEM_CB", length=1)
	String requiredItemCb

	/**
	 * Keep position of resizeable canvas in main menu form GUAGMNU
	 */
	@Column(name="GURTPRF_LINESCRN_X_POS", length=22)
	Integer linescrnXPosition

	/**
	 * Keep position of resizeable buttons in main menu form GUAGMNU
	 */
	@Column(name="GURTPRF_LINEBTN_X_POS", length=22)
	Integer linebtnXPosition

	/**
	 * Tool bar button properties
	 */
	@Column(name="GURTPRF_FORMNAME_DISPLAY_IND")
	String formnameDisplayIndicator

	/**
	 * Version column which is used as a optimistic lock token for GURTPRF
	 */
	@Version
	@Column(name="GURTPRF_VERSION", length=19)
	Long version

	/**
	 * Data Origin column for GURTPRF
	 */
	@Column(name="GURTPRF_DATA_ORIGIN", length=30)
	String dataOrigin

	/**
	 * Activity Date of the last change
	 */
	@Column(name="GURTPRF_ACTIVITY_DATE")
	Date lastModified
	
	public String toString() {
		"""PreferenceThatStoresToolbarAndMenuInfog[
					id=$id, 
					lastModifiedBy=$lastModifiedBy, 
					tlbBtn=$tlbBtn, 
					displayHtCb=$displayHtCb, 
					displayVtCb=$displayVtCb, 
					displayHint=$displayHint, 
					formnameCb=$formnameCb, 
					releaseCb=$releaseCb, 
					dbaseInstitutionCb=$dbaseInstitutionCb, 
					dateTimeCb=$dateTimeCb, 
					requiredItemCb=$requiredItemCb, 
					linescrnXPosition=$linescrnXPosition, 
					linebtnXPosition=$linebtnXPosition, 
					formnameDisplayIndicator=$formnameDisplayIndicator, 
					version=$version, 
					dataOrigin=$dataOrigin,
					lastModified=$lastModified]"""
	}

	
	boolean equals(o) {
	    if (this.is(o)) return true;
	    if (!(o instanceof MenuAndToolbarPreference)) return false;
	    MenuAndToolbarPreference that = (MenuAndToolbarPreference) o;
        if(id != that.id) return false;
        if(lastModifiedBy != that.lastModifiedBy) return false;
        if(tlbBtn != that.tlbBtn) return false;
        if(displayHtCb != that.displayHtCb) return false;
        if(displayVtCb != that.displayVtCb) return false;
        if(displayHint != that.displayHint) return false;
        if(formnameCb != that.formnameCb) return false;
        if(releaseCb != that.releaseCb) return false;
        if(dbaseInstitutionCb != that.dbaseInstitutionCb) return false;
        if(dateTimeCb != that.dateTimeCb) return false;
        if(requiredItemCb != that.requiredItemCb) return false;
        if(linescrnXPosition != that.linescrnXPosition) return false;
        if(linebtnXPosition != that.linebtnXPosition) return false;
        if(formnameDisplayIndicator != that.formnameDisplayIndicator) return false;
        if(version != that.version) return false;
        if(dataOrigin != that.dataOrigin) return false;
        if(lastModified != that.lastModified) return false;
        return true;
    }

	
	int hashCode() {
		int result;
	    result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        result = 31 * result + (tlbBtn != null ? tlbBtn.hashCode() : 0);
        result = 31 * result + (displayHtCb != null ? displayHtCb.hashCode() : 0);
        result = 31 * result + (displayVtCb != null ? displayVtCb.hashCode() : 0);
        result = 31 * result + (displayHint != null ? displayHint.hashCode() : 0);
        result = 31 * result + (formnameCb != null ? formnameCb.hashCode() : 0);
        result = 31 * result + (releaseCb != null ? releaseCb.hashCode() : 0);
        result = 31 * result + (dbaseInstitutionCb != null ? dbaseInstitutionCb.hashCode() : 0);
        result = 31 * result + (dateTimeCb != null ? dateTimeCb.hashCode() : 0);
        result = 31 * result + (requiredItemCb != null ? requiredItemCb.hashCode() : 0);
        result = 31 * result + (linescrnXPosition != null ? linescrnXPosition.hashCode() : 0);
        result = 31 * result + (linebtnXPosition != null ? linebtnXPosition.hashCode() : 0);
        result = 31 * result + (formnameDisplayIndicator != null ? formnameDisplayIndicator.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        return result;
	}

	static constraints = {
		lastModifiedBy(nullable:true, maxSize:30)
		tlbBtn(nullable:true, maxSize:0)
		displayHtCb(nullable:true, maxSize:1)
		displayVtCb(nullable:true, maxSize:1)
		displayHint(nullable:true, maxSize:1)
		formnameCb(nullable:true, maxSize:1)
		releaseCb(nullable:true, maxSize:1)
		dbaseInstitutionCb(nullable:true, maxSize:1)
		dateTimeCb(nullable:true, maxSize:1)
		requiredItemCb(nullable:true, maxSize:1)
		linescrnXPosition(nullable:true)
		linebtnXPosition(nullable:true)
		formnameDisplayIndicator(nullable:true)
        lastModified(nullable:true)
		dataOrigin(nullable:true, maxSize:30)


		/**
	     * Please put all the custom tests in this protected section to protect the code
	     * from being overwritten on re-generation
	     */
	    /*PROTECTED REGION ID(preferencethatstorestoolbarandmenuinfog_custom_constraints) ENABLED START*/
	    
	    /*PROTECTED REGION END*/
    }
    
    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(preferencethatstorestoolbarandmenuinfog_custom_methods) ENABLED START*/
      public static List fetchMenuAndToolbarPreferenceByUser( String user) {
        def menuAndToolbarPreferences
        menuAndToolbarPreferences = PersonalPreference.withSession {session ->
            menuAndToolbarPreferences = session.getNamedQuery('MenuAndToolbarPreference.fetchByUser').setString('lastModifiedBy',user).list()}
        return menuAndToolbarPreferences
    }
    /*PROTECTED REGION END*/
}
