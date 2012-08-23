package net.hedtech.banner.session

import org.apache.log4j.Logger

/**
 * Cross-app Shared Info Service.
 */
class BannerUserSessionService {

    static transactional = true

    def dataSource                         // injected by Spring
    def sessionFactory                     // injected by Spring
    def grailsApplication                  // injected by Spring

    private final log = Logger.getLogger(getClass())


    /**
     *
     * @param sessionToken
     * @param infoToPersist
     * @return
     */
    def publish(sessionToken, Map<String, Object> infoToPersist) {
        log.debug (" Banner User session Data to transfer for the token [" + sessionToken + "]: " + infoToPersist)
        infoToPersist?.each { infoType, info ->
            if (isNull(info)) {
                log.warn(infoType + "has NULL VALUE. This cannot be shared.")
            } else {
                def bannerUserSession = new BannerUserSession(
                        sessionToken: sessionToken,
                        infoType: infoType,
                        info: info
                )
                bannerUserSession.save( failOnError: true)
            }
        }
    }

    private boolean isNull (info){
        (info == null || (info instanceof String && info == ""))
    }

    /**
     *
     * @param seamlessToken
     * @return
     */
    def lookupBySessionToken(seamlessToken) {
        BannerUserSession.findAllBySessionToken (seamlessToken)
    }

    /**
     *
     * @param sessionToken
     * @return seamlessSession
     */
    def consume (sessionToken) {
        log.debug ("Consuming the banner user session token : " + sessionToken)
        def seamlessSession = this.lookupBySessionToken(sessionToken)
        seamlessSession.each {
            it.delete( failOnError: true, flush: true )
        }
        log.debug ("Banner User session retrieved : " + seamlessSession)

        return seamlessSession
    }

}
