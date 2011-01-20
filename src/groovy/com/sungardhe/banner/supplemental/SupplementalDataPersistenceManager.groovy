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

import org.apache.log4j.Logger

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

	private final Logger log = Logger.getLogger( getClass() )




    /**
     * Returns the supplied model after loading it's supplemental data.
     * @param model the model that has supplemental data to persist
     * @return def, fully populated with supplemental data re-loaded from the database
     */
	public def loadSupplementalDataFor( model ) {
		log.trace "In load: ${model}"
		if (!supplementalDataService.supportsSupplementalProperties( model.getClass() )) {
			log.trace "Found No SDE properties for: ${model}"
			return
		}

		try {
			sql = new Sql( sessionFactory.getCurrentSession().connection() )
            def tableName = sessionFactory.getClassMetadata( model.getClass() ).tableName.toUpperCase()
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

			def resultSetAttributesList = sessionFactory.getCurrentSession().createSQLQuery(
                """SELECT DISTINCT govsdav_attr_name as attrName
				   FROM govsdav WHERE govsdav_table_name= :tableName
				""").setString( "tableName", tableName ).list()

			def supplementalProperties = [:]
			resultSetAttributesList.each() {
                loadSupplementalProperty( it, supplementalProperties, tableName )
			}
		    model.setSupplementalProperties( supplementalProperties.clone(), false ) // false -> don't mark as dirty
            log.debug "Set supplemental properties: ${model.supplementalProperties}"
            model
		} catch (e) {
			log.error "Failed to load SDE for the entity ${model.class.name}-${model.id}  Exception: $e "
			throw e
		}
	}


    /**
     * Returns the supplied model after persisting it's supplemental data.
     * @param model the model that has supplemental data to persist
     * @return def the model fully populated with supplemental data re-loaded from the database
     */
	public def persistSupplementalDataFor( model ) {
		log.trace "In persist: ${model}"
		if (!supplementalDataService.supportsSupplementalProperties(model.getClass())) {
			log.info "Found No SDE properties for: ${model}"
			return
		}

		try {
			log.debug "SDE Properties for model: ${model.supplementalProperties}"

			sql = new Sql( sessionFactory.getCurrentSession().connection() )
			def tableName = sessionFactory.getClassMetadata(model.getClass()).tableName.toUpperCase()
			def sdeTableName = 'GORSDAV'

			def id
			def attributeName
			String disc
			def parentTab
			def dataType
			def value

			log.debug "IN SAVE: ${model.supplementalProperties}"

			model.supplementalProperties.each {

				log.debug "KEY: ${it.key} - VALUE: ${it.value}"
				def map = it.value
				attributeName = it.key

				map.each {
					def paramMap = it.value
					log.debug "VALUE: " + it.value

					id = paramMap.id
					value = paramMap.value
					disc = paramMap.disc
					parentTab = paramMap.pkParentTab

                    parentTab = parentTab ?: getPk( tableName,model.id )
					dataType = paramMap.dataType

					value = value ?: ""
                    disc = disc ?: "1"

                    if (value){
					  validateDataType( dataType, value )
                    }

				    if (log.isDebugEnabled()) debug( id, tableName, attributeName, disc, parentTab, dataType, value )

					sql.call ( """declare
					                  l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${sdeTableName},${id});
					              begin
					                  gp_goksdif.p_set_attribute( ${tableName}, ${attributeName}, ${disc},
							                                      ${parentTab}, l_rowid, ${dataType}, ${value} );
					              end;
	                           """ )
				}
			}

            sql.executeUpdate("""
                                   update GORSDAV
                                      set GORSDAV_DISC = rownum
                                    where  GORSDAV_TABLE_NAME = '${tableName}'
                                      and GORSDAV_PK_PARENTTAB = '${parentTab}'
                                      and GORSDAV_ATTR_NAME = '${attributeName}'
                                   """
                              )

            loadSupplementalDataFor( model )
		} catch (e) {
			log.error "Failed to save SDE for the entity ${model.class.name}-${model.id}  Exception: $e "
			throw e
		}
	}


    private def debug( id, tableName, attributeName, String disc, parentTab, dataType, String value ) {
        log.debug "*****************************"
        log.debug "id: " + id
        log.debug "tableName:" + tableName
        log.debug "attributeName:" + attributeName
        log.debug "disc: " + disc
        log.debug "parentTab: " + parentTab
        log.debug "dataType: " + dataType
        log.debug "value: " + value
        log.debug "*****************************"

        /** *****************************************************
         * This code may be enabled in order to debug Oracle ROWID
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
         log.info "ROWID:" +	key}************************************************************** */
    }


    private def validateDataType( dataType, String value ) {
        if (dataType.equals( "NUMBER" ) && !isNumeric( value )) {
            throw new RuntimeException( "Invalid Number" )
        }
        else if (dataType.equals( "DATE" ) && value && !isDateValid( value )) {
            throw new RuntimeException( "Invalid Date" )
        }
    }


    public def removeSupplementalDataFor( model ) {
		log.warn "TO BE IMPLEMENTED: SupplementalDataPersistenceManager.removeSupplementalDataFor asked to remove supplementalProperties ${model.supplementalProperties()}"
		throw new RuntimeException( "Not yet implemented!" )
	}


    // Loads the identified attribute into the supplied supplementalProperties map
    private def loadSupplementalProperty( String attributeName, Map supplementalProperties, String tableName ) {
        def session = sessionFactory.getCurrentSession()

        def resultSet = session.createSQLQuery(
         """  SELECT govsdav_attr_name,
                      govsdav_attr_reqd_ind,
                      govsdav_value_as_char,
                      govsdav_disc,
                      govsdav_pk_parenttab,
                      govsdav_surrogate_id,
                      govsdav_attr_data_type,
                      REPLACE( govsdav_attr_prompt_disp, '%DISC%',govsdav_disc ),
                      govsdav_disc_type,
                      govsdav_disc_validation,
                      govsdav_attr_data_len,
                      govsdav_attr_data_scale,
                      govsdav_attr_info,
                      govsdav_attr_order
               FROM govsdav
                   WHERE govsdav_table_name = :tableName
                   AND govsdav_attr_name = :attributeName
               """
             ).setString( "tableName", tableName ).
               setString( "attributeName", attributeName ).list()

        if (!supplementalProperties."${attributeName}") supplementalProperties."${attributeName}" = [:]
        resultSet.each() {
            SupplementalPropertyDiscriminatorContent discProp =
                new SupplementalPropertyDiscriminatorContent( required: it[1],
                                                              value: it[2],
                                                              disc: (it[3] != null ? it[3] : 1),
                                                              pkParentTab: it[4],
                                                              id: it[5],
                                                              dataType: it[6],
                                                              prompt: it[7],
                                                              discType: it[8],
                                                              validation: it[9] != null ? it[9].toInteger() : 1,
                                                              dataLength: it[10],
                                                              dataScale: it[11],
                                                              attrInfo: it[12],
                                                              attrOrder: it[13])

            SupplementalPropertyValue propValue = new SupplementalPropertyValue( [ (discProp.disc): discProp ] )
            supplementalProperties."${attributeName}" << propValue
        }
    }


	private def getPk( def table, def id ) {
		try {
			sql = new Sql( sessionFactory.getCurrentSession().connection() )

            def pk
			sql.call ( """declare
					          l_pkey varchar2(1000);
					          l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${table},${id});
					      begin
					          l_pkey := gp_goksdif.f_get_pk(${table},l_rowid);
			                  ${Sql.VARCHAR} := l_pkey;
			              end;
		               """ ) { key -> pk = key }
			return pk
		} catch (e) {
			log.error "Failed to get PK for the entity. Exception: $e "
			throw e
		}
	}


	// ---------------------------- Helper Methods -----------------------------------


    def isNumeric = {
        def formatter = java.text.NumberFormat.instance
        def pos = [0] as java.text.ParsePosition
        formatter.parse( it, pos )     // if parse position index has moved to end of string
                                       // them the whole string was numeric
        pos.index == it.size()
    }


	private boolean isValidDateFormats( String dateStr, String...formats ) {
		for (String format : formats) {
			SimpleDateFormat sdf = new SimpleDateFormat( format )
			sdf.setLenient( false )
			try {
				sdf.parse( dateStr )
				return true
			} catch (ParseException e) {
				// Ignore because its not the right format.
			}
		}
		return false
	}


	private boolean isDateValid( dateStr ) {
		def validDate = false
		if (dateStr.length() == 4) {
			validDate = isValidDateFormats( dateStr, "yyyy" )
		} else if (dateStr.indexOf( '/' ) > 0) {
			validDate = isValidDateFormats( dateStr.toLowerCase() , "MM/dd/yyyy", "MM/yyyy", "yyyy/MM/dd" )
		} else if (dateStr.indexOf( '-' ) > 0) {
			validDate = isValidDateFormats( dateStr.toLowerCase() , "MM-dd-yyyy", "MM-yyyy", "yyyy-MM-dd", "dd-MMM-yyyy" )
		} else {
			validDate = isValidDateFormats( dateStr.toLowerCase() , "ddMMMyyyy", "MMMyyyy" )
		}
		return validDate
	}
}
