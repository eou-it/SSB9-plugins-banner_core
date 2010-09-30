/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
 
 
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"

grails.project.dependency.resolution = {
    
    // inherit Grails' default dependencies
    inherits( "global" ) {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    
    
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

     plugins {
        compile 'com.sungardhe:spring-security-cas:1.0.2' // Note: Also update version within 'application.properties'=
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
    }
    
    
    dependencies {
    }

}
