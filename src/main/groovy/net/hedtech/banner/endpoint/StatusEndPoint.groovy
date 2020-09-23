/* ******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.endpoint

import grails.util.Holders
import net.hedtech.banner.db.SessionCounterListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.Endpoint
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
public class StatusEndPoint implements Endpoint<Map<String, Object>> {

    @Autowired
    private ApplicationContext applicationContext

    int mb = 1024 * 1024

    @Override
    public String getId() {
        return "status"
    }

    @Override
    public boolean isEnabled() {
        def statusEnabled = Holders.config.endpoints.status.enabled
        boolean enableFlag = false
        if(statusEnabled != null && statusEnabled instanceof String){
            enableFlag = statusEnabled.toBoolean()
        } else if(statusEnabled != null && statusEnabled instanceof Boolean) {
            enableFlag = statusEnabled
        }
        return enableFlag
    }

    @Override
    public boolean isSensitive() {
        return true
    }

    @Override
    public Map<String, Object> invoke() {
        Map<String, Object> result = new LinkedHashMap<String, Object>()
        result.put('totalActiveSession', SessionCounterListener.totalActiveSession)
        DataSource ssbDataSource = this.applicationContext.getBean('underlyingSsbDataSource')
        DataSource dataSource = this.applicationContext.getBean('underlyingDataSource')
        println("ssbDataSource="+ssbDataSource.url)
        println("dataSource="+dataSource.url)
        result.put('ssbDataSource.active', ssbDataSource.getNumActive())
        result.put('ssbDataSource.idle', ssbDataSource.getNumIdle())
        result.put('dataSource.active', dataSource.getNumActive())
        result.put('dataSource.idle', dataSource.getNumIdle())
        result.put('totalMemory', Runtime.getRuntime().totalMemory()/mb+" MB")
        result.put('freeMemory',  Runtime.getRuntime().freeMemory()/mb+" MB")
        result.put('maxMemory', Runtime.getRuntime().maxMemory()/mb+" MB")
        return result
    }

}
