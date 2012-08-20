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
     * @param seamlessToken
     * @param infoToPersist
     * @return
     */
    def publish(seamlessToken, Map<String, Object> infoToPersist) {
        log.debug (" Banner User session Data to transfer for the token [" + seamlessToken + "]: " + infoToPersist)
        infoToPersist?.each { infoType, info ->
            if (isNull(info)) {
                log.warn(infoType + "has NULL VALUE. This cannot be shared.")
            } else {
                def bannerUserSession = new BannerUserSession(
                        seamlessToken: seamlessToken,
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
    def lookupBySeamlessToken(seamlessToken) {
        BannerUserSession.findAllBySeamlessToken (seamlessToken)
    }

    /**
     *
     * @param seamlessToken
     * @return seamlessSession
     */
    def consume (seamlessToken) {
        log.debug ("Consuming the banner user session token : " + seamlessToken)
        def seamlessSession = this.lookupBySeamlessToken(seamlessToken)
        seamlessSession.each {
            it.delete( failOnError: true, flush: true )
        }
        log.debug ("Banner User session retrieved : " + seamlessSession)

        return seamlessSession
    }

}
