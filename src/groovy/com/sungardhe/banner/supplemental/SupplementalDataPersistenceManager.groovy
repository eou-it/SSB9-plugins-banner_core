/** *****************************************************************************
 � 2010 SunGard Higher Education.  All Rights Reserved.
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
		log.info "In load: ${model}"
		if (!supplementalDataService.supportsSupplementalProperties(model.getClass())) {
			log.info "Found No SDE properties for: ${model}"
			return		
		}else{			
			try {
				sql = new Sql(sessionFactory.getCurrentSession().connection())
				
				def session = sessionFactory.getCurrentSession() 
				
				def tableName = sessionFactory.getClassMetadata(model.getClass()).tableName
				def attributeName
				
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
				
				def resultSetAttributesList = session.createSQLQuery("""
					SELECT DISTINCT govsdav_attr_name as attrName
					FROM govsdav WHERE govsdav_table_name= :tableName
								""").setString("tableName", tableName).list()
				
				
				def myModel =[:]
				def sde = [:]
				
				resultSetAttributesList.each(){
					
					attributeName = it
					def modelProperties = [:]
					
					def resultSet = session.createSQLQuery(
					 """
					   	SELECT govsdav_attr_name, 
					   		   govsdav_attr_reqd_ind, 
					   		   govsdav_value_as_char, 
					   		   govsdav_disc, 
					   		   govsdav_pk_parenttab, 
							   govsdav_surrogate_id, 
							   govsdav_attr_data_type,
							   REPLACE(govsdav_attr_prompt_disp,'%DISC%',govsdav_disc)
					    FROM govsdav 
					   	WHERE govsdav_table_name = :tableName
					   	  AND govsdav_attr_name = :attributeName
					   	"""
					     ).setString("tableName", tableName).
					       setString("attributeName", attributeName).
						   list()
					
					resultSet.each(){
						
						def properties = [:]
						
						properties.required = it[1]
						properties.value = it[2]
						
						if(it[3] == null){
							properties.disc = 1
						}else{
							properties.disc = it[3]
						}
						
						properties.pkParentTab = it[4]
						properties.id = it[5]
						properties.dataType = it[6]
						properties.prompt = it[7]
						
						modelProperties."${properties.disc}" = properties
						
						log.info  "Properties: ${modelProperties}"						
					}
					
					myModel."${attributeName}" = modelProperties					
				}
				
				sde."${model.class.name}-${model.id}" = myModel
				
				persistentStore << sde
				
				def modelKey = "${model.class.name}-${model.id}"
				
				log.info "Model Key: ${modelKey}"
				
				if (persistentStore."$modelKey") {
					model.supplementalProperties = persistentStore."$modelKey".clone()
					log.info "Model Key: ${model.supplementalProperties}"
				}				
			}catch (e) {
				log.error "Failed to load SDE for the entity ${model.class.name}-${model.id}  Exception: $e "
				throw e
			}
		}
	}
	
	
	public def persistSupplementalDataFor( model ) {
		log.info "In persist: ${model}"
		def isNumeric = {    
			def formatter = java.text.NumberFormat.instance    
			def pos = [0] as java.text.ParsePosition   
			formatter.parse(it, pos)     // if parse position index has moved to end of string    
			                             // them the whole string was numeric    
			pos.index == it.size()
		}
		
		if (!supplementalDataService.supportsSupplementalProperties(model.getClass())) {
			log.info "Found No SDE properties for: ${model}"
			return		
		}else{			
			try {
				
				log.info "SDE Properties for model: ${model.supplementalProperties}"
				
				sql = new Sql(sessionFactory.getCurrentSession().connection())
				
				def session = sessionFactory.getCurrentSession()
				
				def tableName = sessionFactory.getClassMetadata(model.getClass()).tableName
				
				def sdeTableName = 'GORSDAV'
				
				def id
				def attributeName
				String disc
				def parentTab
				def dataType
				def value
				
				log.info "IN SAVE: "
				log.info model.supplementalProperties
				
				model.supplementalProperties.each{ 
					
					log.info "KEY: " + it.key
					log.info "VALUE: " + it.value
					
					def map = it.value
					attributeName = it.key
					
					map.each{
						
						def paramMap = it.value						
						log.info "VALUE: " + it.value
						
						id = paramMap.id
						
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
							disc = "1"
						}
						
						// Validation Logic						
						if (dataType.equals("NUMBER") && !isNumeric(value)){
							throw new RuntimeException( "Invalid Number" )
						}else if (dataType.equals("DATE") && !isDateValid(value)){
							throw new RuntimeException( "Invalid Date" )						
						}
						
						
						log.info "*****************************"
						log.info "id: " + id
						log.info "tableName:" + tableName
						log.info "attributeName:" + attributeName
						log.info "disc: " + disc
						log.info "parentTab: " + parentTab
						log.info "dataType: " + dataType
						log.info "value: " + value
						log.info "*****************************"
						
						
						/*******************************************************
						 * This code needs to be disabled. To debug Oracle ROWID
						 * 
						 * *****************************************************
						 sql = new Sql(sessionFactory.getCurrentSession().connection())
						 sql.call ("""   
						 declare
						 l_pkey 	GORSDAV.GORSDAV_PK_PARENTTAB%TYPE;
						 l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${sdeTableName},${id});
						 begin
						 ${Sql.VARCHAR} := l_rowid;
						 end ;
						 """
						 ){key ->						  
                            log.info "ROWID:" +	key
						 }
						 ***************************************************************/
						
						sql.call ("""
						declare
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