package net.hedtech.banner.session;

import java.util.List;
import java.util.Map;

/**
 * TODO to move to banner-core plugin.
 *
 * Created by IntelliJ IDEA.
 * User: Rajanand.PK
 * Date: 12/10/11
 * Time: 12:32 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IBannerUserSessionContributor {

    /**
     * Extract the specific shared info to be persisted
     */
    public Map<String, Object> publish();

    /**
     * Update the app with the DB-retrieved
     * other-app shared info.
     *
     * @param bannerUserSession
     */
    public void consume(List<BannerUserSession> bannerUserSession);

}
