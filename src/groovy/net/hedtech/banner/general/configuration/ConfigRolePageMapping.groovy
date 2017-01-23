/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GURAPPR database table.
 *
 */
@Entity
@Table(name = 'GURAPPR', schema = 'GENERAL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigRolePageMapping.findAll', query = '''SELECT g FROM ConfigRolePageMapping g''')
])
public class ConfigRolePageMapping implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @Column(name = 'GURAPPR_SURROGATE_ID', precision = 19)
    Long id

    @Temporal(TemporalType.DATE)
    @Column(name = 'GURAPPR_ACTIVITY_DATE', nullable = false)
    Date activityDate

    @Column(name = 'GURAPPR_DATA_ORIGIN', length = 30)
    String dataOrigin

    @Column(name = 'GURAPPR_GUBAPPL_APP_ID', nullable = false, precision = 19)
    Long gubapplAppId

    @Column(name = 'GURAPPR_USER_ID', length = 30)
    String userId

    @Version
    @Column(name = 'GURAPPR_VERSION', precision = 19)
    Long version

    @Column(name = 'GURAPPR_VPDI_CODE', length = 6)
    String vpdiCode

    @Column(name = 'PAGE_ID', nullable = false, precision = 19)
    Long pageId

    @Column(name = 'TWTVROLE_CODE', nullable = false, length = 30)
    String code

    public ConfigRolePageMapping() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigRolePageMapping gurappr = (ConfigRolePageMapping) o

        if (activityDate != gurappr.activityDate) return false
        if (gubapplAppId != gurappr.gubapplAppId) return false
        if (code != gurappr.code) return false
        if (dataOrigin != gurappr.dataOrigin) return false
        if (id != gurappr.id) return false
        if (pageId != gurappr.pageId) return false
        if (userId != gurappr.userId) return false
        if (version != gurappr.version) return false
        if (vpdiCode != gurappr.vpdiCode) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (activityDate != null ? activityDate.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (gubapplAppId != null ? gubapplAppId.hashCode() : 0)
        result = 31 * result + (userId != null ? userId.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (vpdiCode != null ? vpdiCode.hashCode() : 0)
        result = 31 * result + (pageId != null ? pageId.hashCode() : 0)
        result = 31 * result + (code != null ? code.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            ConfigRolePageMapping{
                id=$id,
                activityDate=$activityDate,
                dataOrigin='$dataOrigin',
                gubapplAppId=$gubapplAppId,
                userId='$userId',
                version=$version,
                vpdiCode='$vpdiCode',
                pageId=$pageId,
                code='$code'
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def findAll() {
        def configRolePageMapping
        ConfigRolePageMapping.withSession { session ->
            configRolePageMapping = session.getNamedQuery('ConfigRolePageMapping.findAll').list()
        }
        return configRolePageMapping
    }
}
