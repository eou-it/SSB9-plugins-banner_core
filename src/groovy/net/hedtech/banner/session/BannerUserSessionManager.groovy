package net.hedtech.banner.session

import net.hedtech.banner.SpringContextUtils
import net.hedtech.banner.security.FormContext
import org.apache.log4j.Logger
import org.springframework.web.context.request.RequestContextHolder

/**
 * TODO to move to banner-core. its derived classes can continue to reside in the UI plugins.
 *
 * A utility class that provides methods to retrieve
 * information about the web- application.
 */
abstract class BannerUserSessionManager {

    private static final Logger log = Logger.getLogger( "net.hedtech.banner.session.BannerUserSessionManager" )


    public static final String SESSION_BANNER_USER_SESSION_CONTRIBUTORS = "session.banner.user.session.contributors"
    public static final String REQ_SEAMLESS_TOKEN = "seamlessToken"

    /**
     * Retrieves the context from context URI.
     * eg:- param = /StudentCourseCatalog/zkau, return = /StudentCourseCatalog/
     *
     * @param uri
     * @return
     */
    public abstract String getContextFromContextURI(String uri);

    /**
     * Switches to the app specified by 'urlToNavigate' and
     * navigates to the page 'pageName' there.
     *
     * @param urlToNavigate
     * @param pageName
     */
    public abstract void switchApp(urlToNavigate, parameters);

    /**
     * Keep the registered bannerUserSessionContributor implementations in the session.
     *
     * @param bannerUserSessionContributors
     */
    public abstract void setBannerUserSessionContributorsToSession(List<IBannerUserSessionContributor> bannerUserSessionContributors);

    /**
     * Retrieve the registered bannerUserSessionContributor implementations from the session.
     * @return
     */
    public abstract List<IBannerUserSessionContributor> getBannerUserSessionContributorsFromSession();

    /**
     * Registers the multi-app user session contributor provided.
     *
     * @param bannerUserSessionContributor
     */
    public void registerBannerUserSessionContributor(IBannerUserSessionContributor bannerUserSessionContributor) {
        List<IBannerUserSessionContributor> bannerUserSessionContributors = getBannerUserSessionContributorsFromSession()
        if (! bannerUserSessionContributors) {
            bannerUserSessionContributors = []
            setBannerUserSessionContributorsToSession(bannerUserSessionContributors)
        }
        bannerUserSessionContributors <<  bannerUserSessionContributor
    }

    /**
     * Loads and Registers the multi-app user session contributor classes specified in the
     * configuration file.
     *
     */
    public void registerBannerUserSessionContributors() {
        new ConfigSlurper().parse(getClass().getClassLoader().loadClass("BannerUserSessionContributorsConfiguration"))?.each { sharedInfoKey, sharedInfoConfig ->
            registerBannerUserSessionContributor (SpringContextUtils.getGrailsApplicationClassLoader().loadClass(sharedInfoConfig.handler).newInstance())
        }
    }
    /**
     * Retrieve the logged-in user name.
     *
     * @return
     */
    public  def getUserName () {
        (org.springframework.security.core.context.SecurityContextHolder.context.authentication?.user.username)?.toUpperCase()
    }

    /**
     * Retrieve the Application Context Name
     * @return
     */
    public def getAppName () {
        getContextFromContextURI(getContextURI(getRequestUrlInfo()))
    }

    /**
     * Return the context URI from the  request URL.
     *
     * @param uri
     * @return
     */
    public  String getContextURI(uri) {
        uri?.path
    }

    public  def getBannerUserSessionService() {
        SpringContextUtils.applicationContext.getBean("bannerUserSessionService")
    }

    /**
     * Mock Form Context to work with the table
     * represents the BannerUserSession object.
     *
     * @return
     */
    private def setSharedAppInfoFormContext() {
        def contextList = FormContext.get();
        contextList.add("GUAGMNU")
        return contextList
    }

    /**
     * Before invoking switchApp, it performs the shared-app-info
     * backing-up task.
     *
     * @param urlToNavigate
     * @param pageName
     */
    public void redirectToOtherApp (urlToNavigate, pageName) {
        def sessionSeamlessToken = RequestContextHolder.currentRequestAttributes().session.id
        persistBannerUserSession(sessionSeamlessToken)
        log.debug ("Switching application to load the page " + pageName + " and the seamless token is " + sessionSeamlessToken)
        switchApp(urlToNavigate, ['page':pageName, (REQ_SEAMLESS_TOKEN):sessionSeamlessToken])
    }

    /**
     * Checks whether there is a need for context-switch for
     * the specified 'menuUrlToNavigate'.
     *
     * @param menuUrlToNavigate
     * @return
     */
    public boolean needContextSwitch (menuUrlToNavigate) {
        if (! menuUrlToNavigate.url) {
            return false;
        }
        URL requestUrl = getRequestUrlInfo ()
        String currentContext = requestUrl.protocol + "://" + requestUrl.host + ":" + requestUrl.port + getAppName ()
        (menuUrlToNavigate.url != currentContext)
    }

    /**
     * Switch the context if needed.
     *
     * @param menuUrlToNavigate
     * @return
     */
    public boolean switchContextIfNeeded (menuUrlToNavigate) {
        if (needContextSwitch (menuUrlToNavigate)) {
            redirectToOtherApp (menuUrlToNavigate.url, menuUrlToNavigate.pageName)
            return true
        }
        return false
    }

    public URL getRequestUrlInfo () {
        new URL(getRequest()?.requestURL?.toString())
    }

    public def getRequest () {
        RequestContextHolder.currentRequestAttributes()?.request
    }

    public def getRequestParameterMap () {
        getRequest()?.parameterMap
    }

    public String getCompleteRequestURL () {
        appendRequestParameters (requestParameterMap, requestUrlInfo)
    }

    public String appendRequestParameters(parameters, String mainString) {
        def parameterString = mapToString(parameters, "&")
        if (parameterString) {
            mainString += "?" + parameterString + "\";"
        }
        return mainString
    }

    /**
     * A utility method to read-in a key-value pairs
     * and to join using the connector argument.
     *
     * eg:- map = [page:'main', name:'team', fruit:'apple'], connector = '&'
     *      returned value = 'page=main&name=team&fruit=apple'
     *
     *  TODO move to a utility class.
     *
     * @param map
     * @param connector
     * @return
     */
    static def mapToString(map, connector) {
        def str = ''
        map?.each { key, value ->
            str += '&' + key
            str += '=' + value
        }
        if (str)
        {  // the first argument should not prefixed by '&', so removing it.
            str <<= '' // converts to stringbuffer
            str[0..0]=''  // deletes the first character
        }
        str
    }


    public  List<IBannerUserSessionContributor> getBannerUserSessionContributors() {
        getBannerUserSessionContributorsFromSession()
    }

    /**
     *
     * Persist the info that the app wanted to share
     * with other apps. It uses the registered
     * AppSharedInfoHandlers to get the info to
     * share.
     *
     */
    private  void persistBannerUserSession(seamlessToken) {

        List<IBannerUserSessionContributor> bannerUserSessionContributors = getBannerUserSessionContributors ()
        if (bannerUserSessionContributors) {
            def infoToShare = [:]
            bannerUserSessionContributors?.each { bannerUserSessionContributor ->
                infoToShare.putAll(bannerUserSessionContributor.publish())
            }
            //TODO is this necessary ?
            setSharedAppInfoFormContext()

            bannerUserSessionService.publish(seamlessToken, infoToShare)
        }

    }

    /**
     *
     * Updates the web-app artifacts with the info retrieved
     * from other apps having same kind of artifacts.
     *
     * It uses the registered AppSharedInfoHandlers to perform
     * this task.
     *
     * @return
     */
    public  def consumeBannerUserSession(seamlessToken) {
        //TODO is this necessary ?
        setSharedAppInfoFormContext ()

        List<BannerUserSession> bannerUserSession = bannerUserSessionService.consume (seamlessToken)
        getBannerUserSessionContributors()?.each { bannerUserSessionContributor ->
            bannerUserSessionContributor.consume(bannerUserSession)
        }
    }

    public def handleSeamlessNavigationRequest (httpRequestObj) {
        String reqSeamlessToken = httpRequestObj?.getParameter(REQ_SEAMLESS_TOKEN)
        if (reqSeamlessToken){
            consumeBannerUserSession (reqSeamlessToken)
        }
    }

}
