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


    @Column(name="GURSESS_USER_NAME")
    String userName

    @Column(name="GURSESS_INFO_TYPE")
    String infoType

    @Transient
    Object info

    @Column(name="GURSESS_INFO")
    String stringInfo

    @Column(name="GURSESS_DATE_INFO")
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

        MultiAppUserSession that = (MultiAppUserSession) o;

        if (infoType != that.infoType) return false;
        if (userName != that.userName) return false;

        return true;
    }

    int hashCode() {
        int result;
        result = userName.hashCode();
        result = 31 * result + infoType.hashCode();
        return result;
    }


    public String toString ( ) {
        final StringBuilder sb = new StringBuilder ( ) ;
        sb . append ( "MultiAppUserSession" ) ;
        sb . append ( "{userName='" ) . append ( userName ) . append ( '\'' ) ;
        sb . append ( ", infoType='" ) . append ( infoType ) . append ( '\'' ) ;
        sb . append ( ", info=" ) . append ( getInfo() ) ;
        sb . append ( '}' ) ;
        return sb . toString ( ) ;
    }
}
