/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.
 CONFIDENTIAL BUSINESS INFORMATION
 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.supplemental

import groovy.sql.Sql
import java.sql.Connection
import org.apache.log4j.Logger
import com.sungardhe.banner.exceptions.ApplicationException

import java.text.SimpleDateFormat
import java.text.ParseException
/**
 * DAO for supplemental data. This strategy works against the
 * GOVSDAV view for both reading and writing supplemental data.
 */
class SupplementalDataPersistenceManager {
	
	def dataSource               // injected by Spring    
	def sessionFactory           // injected by Spring 
	def supplementalDataService  // injected by Spring 
	
	Sql sql
	
	Map persistentStore = [:]
	
	private final Logger log = Logger.getLogger( getClass() )
	
	public def loadSupplementalDataFor( model ) {
		println "In load: ${model}"
		if (!supplementalDataService.supportsSupplementalProperties(model.getClass())) {
		println "Found No SDE properties for: ${model}"
			return		
		}else{			
			try {
				sql = new Sql(sessionFactory.getCurrentSession().connection())
				
				def session = sessionFactory.getCurrentSession() 
				
				def tableName = sessionFactory.getClassMetadata(model.getClass()).tableName
				
				def id = model.id
				
				sql.call ("""
					  declare
					   l_pkey 	GORSDAV.GORSDAV_PK_PARENTTAB%TYPE;
					   l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${tableName},${id});

					   begin

					   l_pkey := gp_goksdif.f_get_pk(${tableName},l_rowid);
					   gp_goksdif.p_set_current_pk(l_pkey);

					   end;
	                 """
						)
				
				def resultSet = session.createSQLQuery("""
					SELECT govsdav_attr_name, 
						   govsdav_attr_reqd_ind, 
						   govsdav_value_as_char, 
						   govsdav_disc, 
						   govsdav_pk_parenttab, govsdav_surrogate_id, govsdav_attr_data_type FROM govsdav WHERE govsdav_table_name= :tableName
								""").setString("tableName", tableName).list()
				
				def myModel =[:]
				def sde = [:]
				resultSet.each(){	
					def properties = [:]
					
					properties.required = it[1]
					properties.value = it[2]
					properties.disc = it[3]
					properties.pkParentTab = it[4]
					properties.id = it[5]
					properties.dataType = it[6]
					
					def attrName = "${it[0]}"	
					myModel."${attrName}" = properties
					
					println "Properties: ${properties}"
				}
				
				sde."${model.class.name}-${model.id}" = myModel
				
				persistentStore << sde
				
				def modelKey = "${model.class.name}-${model.id}"
				
				println "Model Key: ${modelKey}"
				
				if (persistentStore."$modelKey") {
					model.supplementalProperties = persistentStore."$modelKey".clone()
					println "Model Key: ${model.supplementalProperties}"
				}				
			}catch (e) {
				log.error "Failed to load SDE for the entity ${model.class.name}-${model.id}  Exception: $e "
				throw e
			}
		}
	}
	
		
	public def persistSupplementalDataFor( model ) {
		println "In persist: ${model}"
		def isNumeric = {    
			def formatter = java.text.NumberFormat.instance    
			def pos = [0] as java.text.ParsePosition   
			formatter.parse(it, pos)     // if parse position index has moved to end of string    
			                             // them the whole string was numeric    
			pos.index == it.size()
		}
		
		if (!supplementalDataService.supportsSupplementalProperties(model.getClass())) {
			println "Found No SDE properties for: ${model}"
			return		
		}else{			
			try {
				
				println "SDE Properties for model: ${model.supplementalProperties}"
				
				sql = new Sql(sessionFactory.getCurrentSession().connection())
				
				def session = sessionFactory.getCurrentSession()
				
				def tableName = sessionFactory.getClassMetadata(model.getClass()).tableName
				
				def sdeTableName = 'GORSDAV'
				
				def id
				def attributeName
				def disc
				def parentTab
				def dataType
				def value
				
				model.supplementalProperties.each{ 
					
					def paramMap = it.value
					
					id = paramMap.id
					attributeName = it.key
					value = paramMap.value
					disc = paramMap.disc
					parentTab = paramMap.pkParentTab
					
					if (parentTab == null){
						parentTab = getPk(tableName,model.id)
					}
					
					dataType = paramMap.dataType
					
					if (value == null){
						value = ""
					}
					
					if (disc == null){
						disc = '1'
					}
					
					// Validation Logic						
					if (dataType.equals("NUMBER") && !isNumeric(value)){
						throw new RuntimeException( "Invalid Number" )
					}else if (dataType.equals("DATE") && !isDateValid(value)){
						throw new RuntimeException( "Invalid Date" )						
					}
					
					sql.call ("""
						declare
						   l_pkey 	GORSDAV.GORSDAV_PK_PARENTTAB%TYPE;
						   l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${sdeTableName},${id});
						begin

						gp_goksdif.p_set_attribute(
								   ${tableName}
								  ,${attributeName}
								  ,${disc}
								  ,${parentTab}
								  ,l_rowid
								  ,${dataType}
								  ,${value}
								  );

						end;
	                          """
							)
				}				
			}catch (e) {
				log.error "Failed to save SDE for the entity ${model.class.name}-${model.id}  Exception: $e "
				throw e
			}
		}
	}
	
	
	public def removeSupplementalDataFor( model ) {
		println "TO BE IMPLEMENTED: SupplementalDataPersistenceManager.removeSupplementalDataFor will remove supplementalProperties ${model.supplementalProperties()}"
		throw new RuntimeException( "Not yet implemented!" )
	}
	
	
	private def getPk( def table, def id ) {			
		def pk		
		try {			
			sql = new Sql(sessionFactory.getCurrentSession().connection())
			
			sql.call ("""   
					declare
					 l_pkey varchar2(1000);
					 l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${table},${id});
					begin
					 l_pkey := gp_goksdif.f_get_pk(${table},l_rowid);

			         ${Sql.VARCHAR} := l_pkey;

			        end ;
		    """
					){key ->						  
						pk = key	
						}
			
			return pk			
		}catch (e) {
			log.error "Failed to get PK for the entity. Exception: $e "
			throw e
		}
	}
	
	
// ---------------------------- Helper Methods -----------------------------------	
	
	// Number Validation
	private boolean isValidDateFormats(String dateStr, String...formats){      
		for (String format : formats) {               
			SimpleDateFormat sdf = new SimpleDateFormat(format)               
			sdf.setLenient(false)              
			try {                       
				sdf.parse(dateStr)                      
				return true;              
			} catch (ParseException e) {                       
				//Ignore because its not the right format.     
			}        
		}       
		return false;
	}
	
	
	// Date Validation
	private boolean isDateValid(def dateStr){
		def validDate = false
		if (dateStr.length() == 4) {       
			validDate = isValidDateFormats(dateStr, "yyyy")
		} else if (dateStr.indexOf('/') > 0) {       
			validDate = isValidDateFormats(dateStr.toLowerCase() , "MM/dd/yyyy", "MM/yyyy","yyyy/MM/dd")
		} else if (dateStr.indexOf('-') > 0) {  
			validDate = isValidDateFormats(dateStr.toLowerCase() , "MM-dd-yyyy", "MM-yyyy","yyyy-MM-dd","dd-MMM-yyyy")
		}else {        
			validDate = isValidDateFormats(dateStr.toLowerCase() , "ddMMMyyyy", "MMMyyyy")
		}		
		return validDate		
	}
}