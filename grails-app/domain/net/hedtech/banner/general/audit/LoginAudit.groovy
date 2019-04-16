/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import javax.persistence.*

@Entity
@Table(name = 'GURASSL')
@NamedQueries(value = [
        @NamedQuery(name = "LoginAudit.fetchByLoginId",
                query = """ FROM LoginAudit loginAudit WHERE loginAudit.loginId = :loginId """),

        @NamedQuery(name = 'LoginAudit.fetchByAppId',
                query = """ FROM LoginAudit loginAudit WHERE loginAudit.appId = :appId """)
])

class LoginAudit implements Serializable{

    @Id
    @Column(name = 'GURASSL_SURROGATE_ID')
    @SequenceGenerator(name = "GURASSL_SEQ_GEN", sequenceName = "GURASSL_SURROGATE_ID_SEQUENCE",allocationSize=1)
    @GeneratedValue(generator = "GURASSL_SEQ_GEN", strategy = GenerationType.SEQUENCE)
    Long id


    @Temporal(TemporalType.TIMESTAMP )
    @Column(name = 'GURASSL_AUDIT_TIME')
    Date auditTime


    @Column(name = 'GURASSL_SSB_LOGIN_ID')
    String loginId

    @Column(name = 'GURASSL_APP_ID')
    String appId


    @Column(name = 'GURASSL_IPADDR')
    String ipAddress


    @Column(name = 'GURASSL_USER_AGENT')
    String userAgent


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = 'GURASSL_ACTIVITY_DATE')
    Date lastModified


    @Column(name = 'GURASSL_USER_ID')
    String lastModifiedBy


    @Column(name = 'GURASSL_VERSION')
    Long version


    @Column(name = 'GURASSL_PIDM')
    Integer pidm


    @Column(name = 'GURASSL_LOGON_COMMENT')
    String logonComment


    @Column(name = 'GURASSL_DATA_ORIGIN')
    String dataOrigin


    @Column(name = 'GURASSL_VPDI_CODE')
    String vpdiCode


    static constraints = {
        auditTime(nullable:false)
        loginId(nullable:false, maxSize: 256)
        appId(nullable:false, maxSize: 10)
        ipAddress(nullable:false, maxSize: 50)
        userAgent(nullable:false, maxSize: 512)
        logonComment(nullable:true, maxSize: 256)
        pidm(nullable:true)
        lastModified(nullable:true)
        lastModifiedBy(nullable:true, maxSize: 30)
        dataOrigin(nullable:true, maxSize: 30)
        version(nullable:true)
        vpdiCode(nullable:true, maxSize: 6)
    }


    int hashCode() {
        int result
        result = (auditTime != null ? auditTime.hashCode() : 0)
        result = 31 * result + (loginId != null ? loginId.hashCode() : 0)
        result = 31 * result + (appId != null ? appId.hashCode() : 0)
        result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0)
        result = 31 * result + (userAgent != null ? userAgent.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (logonComment != null ? logonComment.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (vpdiCode != null ? vpdiCode.hashCode() : 0)
        return result
    }


    boolean equals(o) {

        if (this.is(o)) return true
        if (getClass() != o.class) return false

        LoginAudit loginAudit = (LoginAudit) o

        if (auditTime != loginAudit.auditTime) return false
        if (loginId != loginAudit.loginId) return false
        if (appId != loginAudit.appId) return false
        if (ipAddress != loginAudit.ipAddress) return false
        if (userAgent != loginAudit.userAgent) return false
        if (lastModified != loginAudit.lastModified) return false
        if (lastModifiedBy != loginAudit.lastModifiedBy) return false
        if (id != loginAudit.id) return false
        if (version != loginAudit.version) return false
        if (pidm != loginAudit.pidm) return false
        if (logonComment != loginAudit.logonComment) return false
        if (dataOrigin != loginAudit.dataOrigin) return false
        if (vpdiCode != loginAudit.vpdiCode) return false

        return true

    }

    @Override
    public String toString() {
        return """\
            LoginAudit{
                auditTime = $auditTime,
                loginId=$loginId,
                appId=$appId,  
                ipAddress=$ipAddress,
                userAgent=$userAgent,
                activityDate=$lastModified,               
                userId='$lastModifiedBy',          
                id=$id,
                version=$version,
                pidm=$pidm,
                logonComment=$logonComment,
                dataOrigin='$dataOrigin',
                vpdiCode=$vpdiCode 
            }"""
    }

    public static LoginAudit fetchByLoginId(String loginId) {
        LoginAudit loginAudit
        if (loginId) {
            loginAudit = LoginAudit.withSession { session ->
                loginAudit = session.getNamedQuery('LoginAudit.fetchByLoginId').setString('loginId',loginId).uniqueResult()
            }
        }
        return loginAudit
    }

    public static LoginAudit fetchByAppId(String appId) {
        LoginAudit loginAudit
        if (appId) {
            loginAudit = LoginAudit.withSession { session ->
                loginAudit = session.getNamedQuery('LoginAudit.fetchByAppId').setString('appId',appId).uniqueResult()
            }
        }
        return loginAudit
    }

}
