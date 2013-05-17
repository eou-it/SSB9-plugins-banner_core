package net.hedtech.banner.security

/**
 */
public enum TabLevelSecurityAccessIndicator {
    FULL_ACCESS_TO_END_USER("F"){

    },
    READONLY_ACCESS_TO_END_USER("Q"){

    },
    NO_ACCESS_TO_END_USER("N"){

    };

    private String indicatorCode = null

    private TabLevelSecurityAccessIndicator(String indicatorCode) {
        this.indicatorCode = indicatorCode
    }

    public static getTabLevelSecurityAccessIndicator(String indicatorCode) {
        if ("F".equalsIgnoreCase(indicatorCode)) {
            return FULL_ACCESS_TO_END_USER
        } else if ("Q".equalsIgnoreCase(indicatorCode)) {
            return READONLY_ACCESS_TO_END_USER
        } else if ("N".equalsIgnoreCase(indicatorCode)) {
            return NO_ACCESS_TO_END_USER
        } else {
            throw new IllegalArgumentException("No Tab Level Security Access Indicator for the code $indicatorCode")
        }
    }

}


