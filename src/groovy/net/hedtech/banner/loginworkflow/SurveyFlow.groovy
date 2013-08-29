package net.hedtech.banner.loginworkflow

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.utility.DateUtility
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

import java.sql.SQLException

/** *****************************************************************************
 © 2013 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
class SurveyFlow implements PostLoginWorkflow {

    def sessionFactory
    private final log = Logger.getLogger(getClass())
    public boolean showPage(request) {
        def pidm = getPidm()
        def session = request.getSession()
        String isDone = session.getAttribute("surveydone")
        def pushSurvey = false
        if (isSurveyAvailableForUserAuthority() && isDone != "true") {
            // Survey is not yet taken.
            def surveyStartDateRow = getSurveyStartDate()
            def surveyEndDateRow = getSurveyEndDate()
            if(!surveyStartDateRow.isEmpty() &&!surveyEndDateRow.isEmpty()) {
                def today = DateUtility.getTodayDate()
                def surveyStartDate = surveyStartDateRow[0]?.gtvsdax_reporting_date
                def surveyEndDate = surveyEndDateRow[0]?.gtvsdax_reporting_date ?: today
                // Survey start date is not null & Today is between Survey start and end dates
                if (surveyStartDate && (surveyStartDate <= today && today <= surveyEndDate)) {
                    if (getSurveyConfirmedIndicator(pidm) != 'Y') {
                        pushSurvey = true
                    }
                }
            }
            return pushSurvey
        } else {
            // Do not show Survey page as Survey has already been taken.
            return pushSurvey
        }
    }


    public String getControllerUri() {
        return "/ssb/survey/survey"
    }


    static def getPidm() {
        def user = SecurityContextHolder?.context?.authentication?.principal
        if (user instanceof BannerUser) {
            return user.pidm
        }
        return null
    }


    private static def isSurveyAvailableForUserAuthority() {
        def authorities = SecurityContextHolder?.context?.authentication?.principal?.authorities
        def userAuthorities = authorities?.collect { it.objectName }
        return (userAuthorities?.contains('SELFSERVICE-STUDENT') || userAuthorities?.contains('SELFSERVICE-EMPLOYEE'))
    }

    private def getSurveyStartDate() {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            def surveyStartDateRow = sql.rows("""SELECT TRUNC(GTVSDAX_REPORTING_DATE) as GTVSDAX_REPORTING_DATE
                               FROM GTVSDAX
                              WHERE GTVSDAX_INTERNAL_CODE       = 'RESTARTDAT'
                                AND GTVSDAX_INTERNAL_CODE_SEQNO = 1
                                AND GTVSDAX_INTERNAL_CODE_GROUP = 'SSMREDATE'
                                AND GTVSDAX_SYSREQ_IND          = 'Y'""")
            return surveyStartDateRow
        } catch (SQLException ae) {
            sql.close()
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }
    }


    private def getSurveyEndDate() {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            def surveyEndDateRow = sql.rows("""SELECT TRUNC(GTVSDAX_REPORTING_DATE) as GTVSDAX_REPORTING_DATE
                               FROM GTVSDAX
                              WHERE GTVSDAX_INTERNAL_CODE       = 'REENDDATE'
                                AND GTVSDAX_INTERNAL_CODE_SEQNO = 1
                                AND GTVSDAX_INTERNAL_CODE_GROUP = 'SSMREDATE'
                                AND GTVSDAX_SYSREQ_IND          = 'Y'""")
            return surveyEndDateRow
        } catch (SQLException ae) {
            sql.close()
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }
    }


    private def getSurveyConfirmedIndicator(pidm) {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select SPBPERS_CONFIRMED_RE_CDE from spbpers where spbpers_pidm = ${pidm}""")
            return row?.SPBPERS_CONFIRMED_RE_CDE
        } catch (SQLException ae) {
            sql.close()
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        }
    }

}
