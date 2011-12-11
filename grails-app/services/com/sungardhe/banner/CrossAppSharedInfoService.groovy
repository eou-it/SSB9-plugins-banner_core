package com.sungardhe.banner

import com.sungardhe.banner.general.utility.CrossAppSharedInfo

class CrossAppSharedInfoService {

    static transactional = true

    def dataSource                         // injected by Spring
    def sessionFactory                     // injected by Spring
    def grailsApplication                  // injected by Spring

    def save(domains) {
        domains?.each { domain ->
            domain.save( failOnError: true, flush: true )
        }
    }

    def findByUserName (userName) {
        CrossAppSharedInfo.findAllByUserName (userName)
    }
}
