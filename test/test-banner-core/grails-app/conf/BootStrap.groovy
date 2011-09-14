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

import org.codehaus.groovy.grails.commons.ApplicationAttributes
import com.sungardhe.banner.service.DomainManagementMethodsInjector
import grails.util.GrailsNameUtils

/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Configuring the dataSource to ensure connections are tested prior to use
 * -- Injects CRUD methods into services having a static defaultCrudMethods set to true
 * */
class BootStrap {

    def grailsApplication


    def init = { servletContext ->
        def ctx = servletContext.getAttribute( ApplicationAttributes.APPLICATION_CONTEXT )

        // Configuring the dataSource to ensure connections are tested prior to use
        def dataSource = ctx.dataSource.underlyingDataSource
        dataSource.setMinEvictableIdleTimeMillis( 1000 * 60 * 30 )
        dataSource.setTimeBetweenEvictionRunsMillis( 1000 * 60 * 30 )
        dataSource.setNumTestsPerEvictionRun( 3 )
        dataSource.setTestOnBorrow( true )
        dataSource.setTestWhileIdle( false )
        dataSource.setTestOnReturn( false )
        dataSource.setValidationQuery( "select 1 from dual" )
    }


    def destroy = {
        // no-op
    }

}