/* ********************************************************************************
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


grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"

grails.plugin.location.'spring-security-cas'="../spring_security_cas.git"

grails.project.dependency.resolution = {

    // inherit Grails' default dependencies
    inherits( "global" ) {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }


    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    plugins {
        compile 'com.sungardhe:spring-security-core:1.0.1'
        provided 'com.sungardhe:banner-codenarc:0.1.3'
        provided 'com.sungardhe:code-coverage:1.2'
    }

    distribution = {
         localRepository = ""
         remoteRepository( id:"snapshots", url:"http://m038083.sungardhe.com:8081/nexus/content/repositories/snapshots" ) {
              authentication  username:'admin', password:'admin123'
         }
         remoteRepository( id:"releases", url:"http://m038083.sungardhe.com:8081/nexus/content/repositories/releases" ) {
             authentication  username:'admin', password:'admin123'
         }
    }


    repositories {
        mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/releases/"
        mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/snapshots/"
        mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/thirdparty/"

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenCentral()
        mavenRepo "http://repository.jboss.org/maven2/"
        mavenRepo "http://repository.codehaus.org"
    }

    plugins {
      provided 'com.sungardhe:banner-codenarc:0.1.3'// Note: Also update version within 'application.properties'
      provided 'com.sungardhe:code-coverage:1.2'    // Note: Also update version within 'application.properties'
    }

    dependencies {
    }

}
