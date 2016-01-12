/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.mep

import org.apache.log4j.Logger
import groovy.sql.Sql
import oracle.jdbc.OracleTypes
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.web.context.request.RequestContextHolder

/**
 * Created by IntelliJ IDEA.
 * User: mhitrik
 * Date: 7/6/11
 * Time: 10:17 AM
 * To change this template use File | Settings | File Templates.
 */
class MultiEntityProcessingService {

    static transactional = true
    private final Logger log = Logger.getLogger(getClass())
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring

    def mif = false

    def init() {
        log.info "Mep initialization complete."
    }


    def isMEP(con = null) {
        def mepEnabled = RequestContextHolder.currentRequestAttributes().request.session.servletContext.getAttribute('mepEnabled')
        if (mepEnabled == null) {
            if (!con)
                con = new Sql(sessionFactory.getCurrentSession().connection())
            Sql sql = new Sql(con)
            try {
                sql.call("{$Sql.VARCHAR = call g\$_vpdi_security.g\$_is_mif_enabled_str()}") { mifEnabled -> mif = mifEnabled.toLowerCase().toBoolean() }
                RequestContextHolder.currentRequestAttributes().request.session.servletContext.setAttribute('mepEnabled', mif)
                mepEnabled = mif
            } catch (e) {
                log.error("ERROR: Could not establish mif context. $e")
                throw e
            } finally {
                //sql?.close()
            }
        }
        return mepEnabled
    }


    def setHomeContext(home) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("{call g\$_vpdi_security.g\$_vpdi_set_home_context(${home})}")

        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            sql?.close()
        }
    }

    def setHomeContext(home, con) {
        Sql sql = new Sql(con)
        try {
            sql.call("{call g\$_vpdi_security.g\$_vpdi_set_home_context(${home})}")
        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            //sql?.close()
        }
    }


    def getHomeContext() {
        def homeContext
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("{$Sql.VARCHAR = call g\$_vpdi_security.g\$_vpdi_get_inst_code_fnc()}") {home -> homeContext = home}
            homeContext
        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            sql?.close()
        }
    }


    def getHomeContext(con) {
        def homeContext
        Sql sql = new Sql(con)
        try {
            sql.call("{$Sql.VARCHAR = call g\$_vpdi_security.g\$_vpdi_get_inst_code_fnc()}") {home -> homeContext = home}
            homeContext
        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            //sql?.close()
        }
    }

    def setProcessContext(process) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("{call g\$_vpdi_security.g\$_vpdi_set_process_context(${process},'NEXT')}")

        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            sql?.close()
        }
    }

    def setProcessContext(process, con) {
        Sql sql = new Sql(con)
        try {
            sql.call("{call g\$_vpdi_security.g\$_vpdi_set_process_context(${process},'NEXT')}")

        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            //sql?.close()
        }
    }

    def getProcessContext() {
        def processContext
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("{$Sql.VARCHAR = call g\$_vpdi_security.g\$_vpdi_get_proc_context_fnc()}") {process -> processContext = process}
            processContext
        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            sql?.close()
        }
    }

    def setMepOnAccess(userName) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def home
        try {
            sql.call("{$Sql.VARCHAR = call gspvpdi.get_mif_default_code_for_user(${userName})}") {homeDefault -> home = homeDefault}
        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            sql?.close()
        }

        setHomeContext(home)
        setProcessContext(home)
    }


    def setMepOnAccess(userName, con) {
        Sql sql = new Sql(con)
        def home
        try {
            sql.call("{$Sql.VARCHAR = call gspvpdi.get_mif_default_code_for_user(${userName})}") {homeDefault -> home = homeDefault}
        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            //sql?.close()
        }

        setHomeContext(home, con)
        setProcessContext(home, con)
    }


    def getUserHomeCodes(userName) {

        def mepHomes = []

        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call("""
          declare
            c_cursor SYS_REFCURSOR;
           begin
           gspvpdi.get_mif_home_codes_for_user(${userName.toString().toUpperCase()},c_cursor);

           ${Sql.out OracleTypes.CURSOR} := c_cursor;

           end;
              """
        ) {cursor ->
            cursor.eachRow() {

                def mepHome = [:]

                mepHome.code = it.GURUSRI_VPDI_CODE
                mepHome.desc = it.GTVVPDI_DESC
                mepHome.default = it.GURUSRI_USER_DEF_INST_IND.toString().toBoolean()

                mepHomes << mepHome
            }
        }

        return mepHomes

    }


    def getMepCodes() {

        def mepHomes = []

        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.call("""
         declare
           c_cursor SYS_REFCURSOR;
          begin
          g\$_vpdi_security.g\$_vpdi_get_mif_codes (c_cursor);

          ${Sql.out OracleTypes.CURSOR} := c_cursor;

          end;
             """
        ) {cursor ->
            cursor.eachRow() {

                def mepHome = [:]

                mepHome.code = it.GTVVPDI_CODE
                mepHome.desc = it.GTVVPDI_DESC

                mepHomes << mepHome
            }
        }

        return mepHomes.sort{it.code}

    }


    def getUserHomesCount(userName) {
        getUserHomeCodes(userName)?.size()
    }


    def resetUserHomeContext(home) {
        SCH.context?.authentication?.principal?.mepHomeContext = home
        SCH.context?.authentication?.principal?.mepProcessContext = home
        setUserDefault(home)
    }


    def resetUserProcessContext(home) {
        SCH.context?.authentication?.principal?.mepProcessContext = home
        setProcessContext(home)
    }


    def hasMep(id) {
        def mep = false
        def objName

        if (id == null)
            return false


        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow("select * from gubpage where gubpage_name = ?", [id]) {
            objName = it.gubpage_code
        }

        def session = sessionFactory.getCurrentSession()
        def resultSet = session.createSQLQuery("SELECT gobvpdi_object FROM gobvpdi WHERE gobvpdi_object= :objName").setString("objName", objName).list()

        resultSet.each() {
            mep = true
        }
        mep
    }


    def getMepDescription(mep) {
        def desc
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow("select * from gtvvpdi where gtvvpdi_code = ?", [mep]) {
            desc = it.gtvvpdi_desc
        }

        desc

    }

    def getMepDescription(mep, con) {
        def desc
        Sql sql = new Sql(con)
        sql.eachRow("select * from gtvvpdi where gtvvpdi_code = ?", [mep]) {
            desc = it.gtvvpdi_desc
        }

        desc

    }



    private void setUserDefault(home) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("{call g\$_vpdi_security.g\$_vpdi_set_user_default(${home})}")

        } catch (e) {
            log.error("ERROR: Could not establish mif context. $e")
            throw e
        } finally {
            sql?.close()
        }

        setHomeContext(home)
        setProcessContext(home)
    }

}
