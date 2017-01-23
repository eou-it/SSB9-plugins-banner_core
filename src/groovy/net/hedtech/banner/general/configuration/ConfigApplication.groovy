/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import javax.persistence.*

/**
 * The persistent class for the GUBAPPL database table.
 *
 */
@Entity
@Table(name = 'GUBAPPL', schema = 'GENERAL')
@NamedQueries(value = [
        @NamedQuery(name = 'ConfigApplication.findAll', query = '''SELECT g FROM ConfigApplication g''')
])
public class ConfigApplication implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @Column(name = 'GUBAPPL_SURROGATE_ID', precision = 19)
    Long id

    @Temporal(TemporalType.DATE)
    @Column(name = 'GUBAPPL_ACTIVITY_DATE', nullable = false)
    Date activityDate

    @Column(name = 'GUBAPPL_APP_ID', nullable = false, precision = 19)
    Long appId

    @Column(name = 'GUBAPPL_APP_NAME', length = 255)
    String appName

    @Column(name = 'GUBAPPL_DATA_ORIGIN', length = 30)
    String dataOrigin

    @Column(name = 'GUBAPPL_USER_ID', length = 30)
    String userId

    @Version
    @Column(name = 'GUBAPPL_VERSION', precision = 19)
    Long version

    @Column(name = 'GUBAPPL_VPDI_CODE', length = 6)
    String vpdiCode

    public Gubappl() {
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ConfigApplication gubappl = (ConfigApplication) o

        if (activityDate != gubappl.activityDate) return false
        if (appId != gubappl.appId) return false
        if (appName != gubappl.appName) return false
        if (dataOrigin != gubappl.dataOrigin) return false
        if (id != gubappl.id) return false
        if (userId != gubappl.userId) return false
        if (version != gubappl.version) return false
        if (vpdiCode != gubappl.vpdiCode) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (activityDate != null ? activityDate.hashCode() : 0)
        result = 31 * result + (appId != null ? appId.hashCode() : 0)
        result = 31 * result + (appName != null ? appName.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (userId != null ? userId.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (vpdiCode != null ? vpdiCode.hashCode() : 0)
        return result
    }


    @Override
    public String toString() {
        return """\
            Gubappl{
                id=$id,
                activityDate=$activityDate,
                appId=$appId,
                appName='$appName',
                dataOrigin='$dataOrigin',
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
        def configApplication
        ConfigApplication.withSession { session ->
            configApplication = session.getNamedQuery('ConfigApplication.findAll').list()
        }
        return configApplication
    }
}
