
/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.utility

import javax.persistence.*
import org.hibernate.annotations.Type;

/**
 * Personal Preference Table
 */
//TODO: NamedQueries that needs to be ported:
/**
 * Where clause on this entity present in forms:
 * Form Name: GJAJPRF
 *  where guruprf_user_id = user
 and guruprf_group = 'JOBSUB'
 and guruprf_key = :current_os

 * Order by clause on this entity present in forms:
 */
@Entity
@Table(name="GURUPRF")
@NamedQueries(value = [
@NamedQuery(name = "PersonalPreference.fetchByKey",
        query = """FROM   PersonalPreference a
		   WHERE  a.group = :group
		   AND    a.key = :key
		   AND    a.string = :string
	       AND    a.lastModifiedBy = :lastModifiedBy
	      """)
])
class PersonalPreference implements Serializable {

    /**
     * Surrogate ID for GURUPRF
     */
    @Id
    @Column(name="GURUPRF_SURROGATE_ID")
    @SequenceGenerator(name = "GURUPRF_SEQ_GEN", allocationSize = 1, sequenceName = "GURUPRF_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GURUPRF_SEQ_GEN")
    Long id

    /**
     * UserID
     */
    @Column(name="GURUPRF_USER_ID", length=30)
    String lastModifiedBy

    /**
     * Group
     */
    @Column(name="GURUPRF_GROUP", nullable = false, length=30)
    String group

    /**
     * Key
     */
    @Column(name="GURUPRF_KEY", nullable = false, length=30)
    String key

    /**
     * String
     */
    @Column(name="GURUPRF_STRING", nullable = false, length=30)
    String string

    /**
     * Value
     */
    @Column(name="GURUPRF_VALUE", length=200)
    String value

    /**
     * System Required Indicator
     */
    @Type(type = "yes_no")
    @Column(name="GURUPRF_SYSTEM_REQ_IND")
    Boolean systemRequiredIndicator

    /**
     * Activity Date of the last change
     */
    @Column(name="GURUPRF_ACTIVITY_DATE")
    Date lastModified

    /**
     * Version column which is used as a optimistic lock token for GURUPRF
     */
    @Version
    @Column(name="GURUPRF_VERSION", length=19)
    Long version

    /**
     * Data Origin column for GURUPRF
     */
    @Column(name="GURUPRF_DATA_ORIGIN", length=30)
    String dataOrigin



    public String toString() {
        """PersonalPreference[
					id=$id,
					lastModifiedBy=$lastModifiedBy,
					group=$group,
					key=$key,
					string=$string,
					value=$value,
					systemRequiredIndicator=$systemRequiredIndicator,
					lastModified=$lastModified,
					version=$version,
					dataOrigin=$dataOrigin, ]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true;
        if (!(o instanceof PersonalPreference)) return false;
        PersonalPreference that = (PersonalPreference) o;
        if(id != that.id) return false;
        if(lastModifiedBy != that.lastModifiedBy) return false;
        if(group != that.group) return false;
        if(key != that.key) return false;
        if(string != that.string) return false;
        if(value != that.value) return false;
        if(systemRequiredIndicator != that.systemRequiredIndicator) return false;
        if(lastModified != that.lastModified) return false;
        if(version != that.version) return false;
        if(dataOrigin != that.dataOrigin) return false;
        return true;
    }


    int hashCode() {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (string != null ? string.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (systemRequiredIndicator != null ? systemRequiredIndicator.hashCode() : 0);
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0);
        return result;
    }

    static constraints = {
        lastModifiedBy(nullable:true, maxSize:30)
        group(nullable:false, maxSize:30)
        key(nullable:false, maxSize:30)
        string(nullable:false, maxSize:30)
        value(nullable:true, maxSize:200)
        systemRequiredIndicator(nullable:true)
        lastModified(nullable:true)
        dataOrigin(nullable:true, maxSize:30)


        /**
         * Please put all the custom tests in this protected section to protect the code
         * from being overwritten on re-generation
         */
        /*PROTECTED REGION ID(personalpreference_custom_constraints) ENABLED START*/

        /*PROTECTED REGION END*/
    }

    /**
     * Please put all the custom methods/code in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(personalpreference_custom_methods) ENABLED START*/
    public static List fetchPersonalPreferencesByKey(String group, String key,String string,String user) {
        def personalPreferences
        personalPreferences = PersonalPreference.withSession {session ->
            personalPreferences = session.getNamedQuery('PersonalPreference.fetchByKey').setString('group',group).setString('key',key).setString('string',string).setString('lastModifiedBy',user).list()}
        return personalPreferences
    }

    /*PROTECTED REGION END*/
}