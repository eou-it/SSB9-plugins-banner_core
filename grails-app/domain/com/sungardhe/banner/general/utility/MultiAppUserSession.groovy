package com.sungardhe.banner.general.utility

import javax.persistence.*

@Entity
@Table(name="GURSESS")
class MultiAppUserSession implements Serializable {

    @Id
    @Column(name = "GURSESS_SURROGATE_ID")
    @SequenceGenerator(name = "GURSESS_SEQ_GEN", allocationSize = 1, sequenceName = "GURSESS_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GURSESS_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GURSESS
     */
    @Version
    @Column(name = "GURSESS_VERSION", nullable = false, precision = 19)
    Long version

    /**
	 * UserID
	 */
	@Column(name="GURSESS_USER_ID", length=30)
	String lastModifiedBy

	/**
	 * Activity Date of the last change
	 */
	@Column(name="GURSESS_ACTIVITY_DATE")
	Date lastModified

	/**
	 * Data Origin column for GURSESS
	 */
	@Column(name="GURSESS_DATA_ORIGIN", length=30)
	String dataOrigin

    @Column(name="GURSESS_USER")
    String userName

    @Column(name="GURSESS_NAME")
    String infoType

    @Transient
    Object info

    @Column(name="GURSESS_VALUE")
    String stringInfo

    @Column(name="GURSESS_DATE_VALUE")
    Date dateInfo

    static constraints = {
        lastModifiedBy(nullable:true, maxSize:30)
		lastModified(nullable:true)
		dataOrigin(nullable:true, maxSize:30)
        userName(nullable:false, maxSize:150)
        infoType(nullable:false, maxSize:1000)
        info(nullable:false)
        dateInfo(nullable:true)
        stringInfo(nullable:true)
    }

    Object getInfo () {
        if (getDateInfo()) {
            return dateInfo
        } else {
            return stringInfo
        }
    }

    void setInfo (Object info) {
         if (info instanceof Date){
             dateInfo = info
         } else {
             stringInfo = info
         }
    }


    boolean equals(o) {
        if (this.is(o)) return true;
        if (getClass() != o.class) return false;

        MultiAppUserSession that = (MultiAppUserSession) o;

        if (dataOrigin != that.dataOrigin) return false;
        if (id != that.id) return false;
        if (info != that.info) return false;
        if (infoType != that.infoType) return false;
        if (lastModified != that.lastModified) return false;
        if (lastModifiedBy != that.lastModifiedBy) return false;
        if (userName != that.userName) return false;
        if (version != that.version) return false;

        return true;
    }

    int hashCode() {
        int result;
        result = id.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + lastModifiedBy.hashCode();
        result = 31 * result + lastModified.hashCode();
        result = 31 * result + dataOrigin.hashCode();
        result = 31 * result + userName.hashCode();
        result = 31 * result + infoType.hashCode();
        result = 31 * result + info.hashCode();
        return result;
    }


    public String toString () {
        """MultiAppUserSession[
                id=$id,
                userName=$userName,
                infoType=$infoType,
                info=$info,
                version=$version,
                lastModifiedBy=$lastModifiedBy,
                lastModified=$lastModified,
                dataOrigin=$dataOrigin]"""
    }
}
