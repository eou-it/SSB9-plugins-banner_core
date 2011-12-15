package com.sungardhe.banner.general.utility

import javax.persistence.*

@Entity
@Table(name="ABCDEDF")
class CrossAppSharedInfo implements Serializable {

    @Id
    @Column(name = "HORCRSI_SURROGATE_ID")
    @SequenceGenerator(name = "HORCRSI_SEQ_GEN", allocationSize = 1, sequenceName = "HORCRSI_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "HORCRSI_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for HORCRSI
     */
    @Version
    @Column(name = "HORCRSI_VERSION", nullable = false, precision = 19)
    Long version


    @Column(name="HORCRSI_USER_NAME")
    String userName

    @Column(name="HORCRSI_APP_NAME")
    String appName

    @Column(name="HORCRSI_INFO_TYPE")
    String infoType

    @Transient
    Object info

    @Column(name="HORCRSI_INFO")
    String stringInfo

    @Column(name="HORCRSI_DATE_INFO")
    Date dateInfo

    static constraints = {
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

        CrossAppSharedInfo that = (CrossAppSharedInfo) o;

        if (appName != that.appName) return false;
        if (infoType != that.infoType) return false;
        if (userName != that.userName) return false;

        return true;
    }

    int hashCode() {
        int result;
        result = userName.hashCode();
        result = 31 * result + appName.hashCode();
        result = 31 * result + infoType.hashCode();
        return result;
    }


    public String toString ( ) {
        final StringBuilder sb = new StringBuilder ( ) ;
        sb . append ( "CrossAppSharedInfo" ) ;
        sb . append ( "{userName='" ) . append ( userName ) . append ( '\'' ) ;
        sb . append ( ", appName='" ) . append ( appName ) . append ( '\'' ) ;
        sb . append ( ", infoType='" ) . append ( infoType ) . append ( '\'' ) ;
        sb . append ( ", info=" ) . append ( info ) ;
        sb . append ( '}' ) ;
        return sb . toString ( ) ;
    }
}
