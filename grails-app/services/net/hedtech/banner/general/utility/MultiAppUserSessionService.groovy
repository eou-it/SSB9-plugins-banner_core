package net.hedtech.banner.general.utility

/**
 * Cross-app Shared Info Service.
 */
class MultiAppUserSessionService {

    static transactional = true

    def dataSource                         // injected by Spring
    def sessionFactory                     // injected by Spring
    def grailsApplication                  // injected by Spring

    /**
     *
     * @param seamlessToken
     * @param infoToPersist
     * @return
     */
    def publish(seamlessToken, Map<String, Object> infoToPersist) {
        log.debug (" Seamless Data to transfer for seamless token [" + seamlessToken + "]: " + infoToPersist)
        infoToPersist?.each { infoType, info ->
            if (isNull(info)) {
                log.warn(infoType + "has NULL VALUE. This cannot be shared.")
            } else {
                def multiAppUserSession = new MultiAppUserSession(
                        seamlessToken: seamlessToken,
                        infoType: infoType,
                        info: info
                )
                multiAppUserSession.save( failOnError: true)
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
        MultiAppUserSession.findAllBySeamlessToken (seamlessToken)
    }

    /**
     *
     * @param seamlessToken
     * @return seamlessSession
     */
    def consume (seamlessToken) {
        log.debug ("Consuming the seamless token : " + seamlessToken)
        def seamlessSession = this.lookupBySeamlessToken(seamlessToken)
        seamlessSession.each {
            it.delete( failOnError: true, flush: true )
        }
        log.debug ("Seamless session retrieved : " + seamlessSession)

        return seamlessSession
    }

}
