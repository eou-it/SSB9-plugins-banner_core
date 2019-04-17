/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.audit

import net.hedtech.banner.general.audit.PageAccessAudit

class PageAccessAuditTagLib {
    def pageAccessAuditService

    def pageAccessAudit = {
        try {
            PageAccessAudit pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
            if (pageAccessAudit) {
                log.debug("PageAccess Audit created for = ${pageAccessAudit.pageUrl}")
            } else {
                log.debug("PageAccess Audit not created as EnablePageAudit is not enabled or matching")
            }
        } catch (ex) {
            log.error("Exception occured while executing pageAccessAudit " + ex.getMessage())
        }
    }
}
