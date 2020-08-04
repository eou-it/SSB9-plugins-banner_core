/*******************************************************************************
 Copyright 2019-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.audit
import grails.util.Holders
import groovy.util.logging.Slf4j

@Slf4j
class AuditUtility {

    public static String getClientIpAddress(request, Integer maxSize) {
        String ipAddressList = request.getHeader("X-FORWARDED-FOR")
        String clientIpAddress
        if (ipAddressList?.length() > 0) {
            String ipAddress = ipAddressList.split(",")[0]
            if (ipAddress.length() <= maxSize) {
                clientIpAddress = ipAddress
            } else {
                clientIpAddress = request.getRemoteAddr()
                log.error("Exception occured while getting clientIpAddress:Ip Address too long.")
            }
        } else {
            clientIpAddress = request.getRemoteAddr()
        }
        clientIpAddress
    }

    public static String getClientIpAddress(request) {
        String ipAddressList = request.getHeader("X-FORWARDED-FOR")
        String clientIpAddress = (ipAddressList?.length() > 0) ? ipAddressList.split(",")[0] : request.getRemoteAddr()
        clientIpAddress
    }


    public static String getAuditIpAddressConfiguration() {
        String auditIpAddressConfiguration = (Holders.config.AuditIPAddress instanceof String && Holders.config.AuditIPAddress.size() > 0) ? (Holders.config.AuditIPAddress).toLowerCase() : 'n'
        auditIpAddressConfiguration
    }


    public static String getMaskedIpAddress(String ipAddress) {
        String maskedIp
        String ipv6orIpv4Separator = ipAddress.contains(':') ? ":" : "."
        if (ipv6orIpv4Separator == ".") {
            maskedIp = maskIpv4(ipAddress)
        }
        else {
            maskedIp = maskIpv6(ipAddress)
        }
        maskedIp
    }

    private static String maskIpv4(String ipAddress) {
        String masked = ""
        ipAddress.eachWithIndex { it, index ->
            if (index > ipAddress.lastIndexOf('.') && it != '.') {
                it = it.replaceAll(it, 'X')
            }
            masked = masked + it
        }
        masked
    }

    private static String maskIpv6(String ipAddress) {
        int separatorCount = 0
        String masked = ""
        ipAddress.eachWithIndex { it, index ->
            if (it == ':') {
                separatorCount = separatorCount + 1
            }
            if (separatorCount > 2 && it != ':') {
                it = it.replaceAll(it, 'X')
            }
            masked = masked + it
        }
        masked
    }
}
