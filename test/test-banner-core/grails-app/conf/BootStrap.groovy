/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

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