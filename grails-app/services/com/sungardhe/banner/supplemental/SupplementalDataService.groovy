/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.
 CONFIDENTIAL BUSINESS INFORMATION
 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.supplemental

import org.hibernate.persister.entity.SingleTableEntityPersister
import org.apache.log4j.Logger
import groovy.sql.Sql
import java.sql.Connection;
import com.sungardhe.banner.db.BannerDS as BannerDataSource
import com.sungardhe.banner.db.BannerConnection

/**
 * A service used to support persistence of supplemental data.
 */
class SupplementalDataService {

	static transactional = true
	private final Logger log = Logger.getLogger( getClass() )

	def dataSource                         // injected by Spring
	def sessionFactory                     // injected by Spring
	def supplementalDataPersistenceManager // injected by Spring

	def supplementalDataConfiguration = [:]


	public def getSupplementalDataConfigurationFor( Class modelClass ) {
		supplementalDataConfiguration."${modelClass.name}"
	}


	def init() {
		// Groovy SQL invocation of view

		// TODO refactor this to be more groovy !!!  -- also, revisit whether to do all at once during init() or do on a class-by-class basis as needed and cache?
		// Until we define the structure, we'll just dump this out...
		Map x = sessionFactory.getAllClassMetadata()
		for (Iterator i = x.values().iterator(); i.hasNext(); ) {
			SingleTableEntityPersister y = (SingleTableEntityPersister)i.next();
			setSDE(y.getName(), y.getTableName())
			// println( y.getName() + " -> " + y.getTableName() )
			// for (int j = 0; j < y.getPropertyNames().length; j++) {
		    //                println( " " + y.getPropertyNames()[j] + " -> " + (y.getPropertyColumnNames( j ).length > 0 ? y.getPropertyColumnNames( j )[ 0 ] : ""))
			// }
		}
        log.info "SupplementalDataService initialization complete."
	}


	/**
	 * Appends additional supplemental data configuration for a model. This is used for testing purposes.
	 * @param map the additional supplemental data configuration in the form: [ modelClass: [ propertyName: [ required: boolean, dataType: someType ], ], ]
	 */
	public void appendSupplementalDataConfiguration( Map map ) {
		supplementalDataConfiguration << map
	}


	public boolean supportsSupplementalProperties( Class modelClass ) {
		supplementalDataConfiguration.keySet().contains modelClass.name
	}


	public List supplementalPropertyNamesFor( Class modelClass ) {
		supplementalDataConfiguration."${modelClass.name}".keySet().asList()
	}


	public boolean hasSupplementalProperties( modelInstance ) {
		modelInstance.hasSupplementalProperties()
	}


	public def loadSupplementalDataFor( model ) {
		supplementalDataPersistenceManager.loadSupplementalDataFor( model )
	}


	public def persistSupplementalDataFor( model ) {
		removeUnsupportedPropertiesFrom( model )
		supplementalDataPersistenceManager.persistSupplementalDataFor( model )
	}


	public def removeSupplementalDataFor( model ) {
		supplementalDataPersistenceManager.removeSupplementalDataFor( model )
	}


	private def removeUnsupportedPropertiesFrom( model ) {
		def supportedNames = supplementalPropertyNamesFor( model.class )
		def supportedProperties = model.supplementalProperties?.findAll { k, v -> k in supportedNames }
		model.supplementalProperties = supportedProperties
	}


	private setSDE(entityName, tableName){

		boolean found = false

		Connection	conn = (dataSource as BannerDataSource).getUnproxiedConnection() as BannerConnection
		Sql db = new Sql (conn)

		String rolePswd = ""
		try {
			db.call("{$Sql.VARCHAR = call g\$_security.g\$_get_role_password_fnc('BAN_DEFAULT_M' ,'SEED-DATA')}") {role -> rolePswd = role }
			String roleM = """SET ROLE "BAN_DEFAULT_M" IDENTIFIED BY "${rolePswd}" """
			db.execute(roleM)
			Connection sessionConnection = db.getConnection()

			def session = sessionFactory.openSession(sessionConnection);

			def resultSet = session.createSQLQuery("SELECT gorsdam_attr_name, gorsdam_attr_reqd_ind, gorsdam_attr_data_type FROM gorsdam WHERE gorsdam_table_name= :tableName").setString("tableName", tableName).list()

			def model =[:]
			def properties = [:]
			def sde = [:]

			resultSet.each(){
				found = true
				properties.required = it[1]
				def attrName = "${it[0]}"
				model."${attrName}" = properties
			}

			if (found){
				sde."${entityName}" = model
				appendSupplementalDataConfiguration(sde)
				log.info "Table: ${tableName}"
			}
		} catch (e) {
			println "ERROR: Could not establish role set up to the database. ${e.message}"
		}finally {
			db?.close()
		}
	}
}
