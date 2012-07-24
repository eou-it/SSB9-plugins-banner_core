package com.sungardhe.banner.seamless

import com.sungardhe.banner.general.utility.MultiAppUserSessionService
import com.sungardhe.common.ui.zk.Utils
import javax.servlet.http.HttpSessionEvent
import org.zkoss.zk.ui.http.HttpSessionListener
import net.hedtech.common.ui.zk.Utils
import net.hedtech.banner.general.utility.MultiAppUserSessionService

/**
 * Created by IntelliJ IDEA.
 * User: rajanandppk
 * Date: 7/16/12
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
class SeamlessHttpSessionListener extends HttpSessionListener {
    MultiAppUserSessionService multiAppUserSessionService

    public void sessionDestroyed(HttpSessionEvent evt) {
        def seamlessToken = evt.session.getAttribute("session.seamlessToken")
        if (seamlessToken) {
            multiAppUserSessionService.delete(seamlessToken)
        }
	}

    private synchronized MultiAppUserSessionService getMultiAppUserSessionService() {
        if (multiAppUserSessionService == null) {
            multiAppUserSessionService =
            Utils.applicationContext.getBean("multiAppUserSessionService")
        }
        return multiAppUserSessionService
    }
}
