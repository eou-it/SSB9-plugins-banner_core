package net.hedtech.banner.security

import java.util.regex.Pattern

/**
 */
public enum AccessPrivilegeType {
    READONLY(~/DEFAULT_Q/ as Pattern){

    },
    READWRITE(~/DEFAULT_M/  as Pattern){

    },
    UNDEFINED(null){

    };

    private Pattern pattern = null

    private AccessPrivilegeType (Pattern pattern) {
        this.pattern = pattern
    }

    public static boolean isReadOnlyPattern(roleName) {
        READONLY.pattern.matcher(roleName)
    }

    public static boolean isReadWritePattern(roleName) {
        READWRITE.pattern.matcher(roleName)
    }
}


