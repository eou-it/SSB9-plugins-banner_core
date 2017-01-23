/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GUBAIR database table.
 *
 */
@Entity
@Table(name = 'GUBAIR', schema = 'GENERAL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigInstance.findAll', query = '''SELECT g FROM ConfigInstance g''')
])
public class ConfigInstance implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @Column(name = 'GUBAIR_SURROGATE_ID', precision = 19)
    Long id

    @Temporal(TemporalType.DATE)
    @Column(name = 'GUBAIR_ACTIVITY_DATE', nullable = false)
    Date activityDate

    @Column(name = 'GUBAIR_DATE_ORIGIN', length = 30)
    String dateOrigin

    @Column(name = 'GUBAIR_ENV', nullable = false, precision = 19)
    Long env

    @Column(name = 'GUBAIR_GUBAPPL_APP_ID', nullable = false, precision = 19)
    Long gubapplAppId

    @Column(name = 'GUBAIR_URL', length = 256)
    String url

    @Column(name = 'GUBAIR_USER_ID', length = 30)
    String userId

    @Version
    @Column(name = 'GUBAIR_VERSION', precision = 19)
    Long version

    @Column(name = 'GUBAIR_VPDI_CODE', length = 6)
    String vpdiCode

    public Gubair() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigInstance gubair = (ConfigInstance) o

        if (activityDate != gubair.activityDate) return false
        if (dateOrigin != gubair.dateOrigin) return false
        if (env != gubair.env) return false
        if (gubapplAppId != gubair.gubapplAppId) return false
        if (id != gubair.id) return false
        if (url != gubair.url) return false
        if (userId != gubair.userId) return false
        if (version != gubair.version) return false
        if (vpdiCode != gubair.vpdiCode) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (activityDate != null ? activityDate.hashCode() : 0)
        result = 31 * result + (dateOrigin != null ? dateOrigin.hashCode() : 0)
        result = 31 * result + (env != null ? env.hashCode() : 0)
        result = 31 * result + (gubapplAppId != null ? gubapplAppId.hashCode() : 0)
        result = 31 * result + (url != null ? url.hashCode() : 0)
        result = 31 * result + (userId != null ? userId.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (vpdiCode != null ? vpdiCode.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            Gubair{
                id=$id,
                activityDate=$activityDate,
                dateOrigin='$dateOrigin',
                env=$env,
                gubapplAppId=$gubapplAppId,
                url='$url',
                userId='$userId',
                version=$version,
                vpdiCode='$vpdiCode'
            }"""
    }

    /**
     * Named query to fetch all data from this domain without any criteria.
     * @return List
     */
    public static def findAll() {
        def configInstance
        ConfigInstance.withSession { session ->
            configInstance = session.getNamedQuery('ConfigInstance.findAll').list()
        }
        return configInstance
    }
}
