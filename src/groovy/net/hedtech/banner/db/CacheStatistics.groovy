package net.hedtech.banner.db

/**
 * Created with IntelliJ IDEA.
 * User: rahulb
 * Date: 3/27/13
 * Time: 2:04 PM
 * To change this template use File | Settings | File Templates.
 */
class CacheStatistics {
    long hits
    long misses
    long abandoned
    long released
    long averageSessionPeriod
    Map sessionMap = [:]
    long sessionCounter
    long totalTime
    static CacheStatistics cacheStatsInstance

    private CacheStatistics() {

    }

    public void login(String sessionId) {
        sessionMap[sessionId] = System.currentTimeMillis()
        sessionCounter++
    }

    public void logout(String sessionId) {
        long loginTime = sessionMap[sessionId]
        long logoutTime = System.currentTimeMillis()
        totalTime += (logoutTime - loginTime)
        sessionMap.remove(sessionId)
    }

    public double getSessionAverageTime() {
        def closed = getClosedSessions()
        if(closed == 0) {
            return 0
        }
        return totalTime / (getClosedSessions() * 1000)
    }

    public long getActiveSessions() {
        return sessionMap.size()
    }

    public long getClosedSessions() {
        def closed = sessionCounter - sessionMap.size()
        return closed
    }

    public void hit() {
        hits++
    }

    public void release() {
        released++
    }

    public void miss() {
        misses++
    }

    public void abandon() {
        abandoned++
    }

    static CacheStatistics getInstance() {
        if (!cacheStatsInstance) {
            cacheStatsInstance = new CacheStatistics()
        }
        return cacheStatsInstance
    }

    @Override
    public String toString() {
        return "CacheStatistics{" +
                "abandoned=" + abandoned +
                ", hits=" + hits +
                ", misses=" + misses +
                '}';
    }
}
