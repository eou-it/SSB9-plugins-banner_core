package com.sungardhe.banner

import com.sungardhe.banner.general.utility.CrossAppSharedInfo

/**
 * Cross-app Shared Info Service.
 */
class CrossAppSharedInfoService {

    static transactional = true

    def dataSource                         // injected by Spring
    def sessionFactory                     // injected by Spring
    def grailsApplication                  // injected by Spring

    /**
     * Before saving, it needs to delete
     * the existing data for this app belong
     * this user.
     *
     * @param domains
     * @return
     */
    def save(domains) {
        if (! domains?.isEmpty ()) {
            delete (domains[0].userName, domains[0].appName)
        }

        //TODO is there GORM batch save ?
        domains?.each { domain ->
            domain.save( failOnError: true)
        }
    }

    def delete (userName, appName) {
        CrossAppSharedInfo.executeUpdate("delete CrossAppSharedInfo c where c.appName = :appName and c.userName = :userName", [appName:appName, userName:userName])
    }

    def delete (userName) {
        CrossAppSharedInfo.executeUpdate("delete CrossAppSharedInfo c where c.userName = :userName", [userName:userName])
    }

    def findByUserName (userName) {
        CrossAppSharedInfo.findAllByUserName (userName)
    }
}
