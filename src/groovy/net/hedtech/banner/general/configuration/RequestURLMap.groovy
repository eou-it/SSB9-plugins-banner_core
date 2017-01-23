/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

/**
 * RequestURLMap this pojo class is used to get all data assigned from the ConfigControllerEndpointPage.
 */
class RequestURLMap {
    static final long serialVersionUID = 1L

    String url
    String configAttribute
    String appName
    Long displaySequence
    long pageId
    long gubapplAppId
    Long version
    List<String> roleList

    RequestURLMap(String url,
                  String configAttribute,
                  String appName,
                  Long displaySequence,
                  Long pageId,
                  Long gubapplAppId,
                  Long version) {
        this.url = url
        this.configAttribute = configAttribute
        this.appName = appName
        this.displaySequence = displaySequence
        this.pageId = pageId
        this.gubapplAppId = gubapplAppId
        this.version = version
    }

    @Override
    public String toString() {
        return """\
            RequestURLMap{
                url='$url',
                configAttribute='$configAttribute',
                appName='$appName',
                displaySequence=$displaySequence,
                pageId=$pageId,
                gubapplAppId=$gubapplAppId,
                version=$version,
                roleList=$roleList
            }"""
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        RequestURLMap that = (RequestURLMap) o

        if (gubapplAppId != that.gubapplAppId) return false
        if (pageId != that.pageId) return false
        if (appName != that.appName) return false
        if (configAttribute != that.configAttribute) return false
        if (displaySequence != that.displaySequence) return false
        if (roleList != that.roleList) return false
        if (url != that.url) return false
        if (version != that.version) return false

        return true
    }

    int hashCode() {
        int result
        result = (url != null ? url.hashCode() : 0)
        result = 31 * result + (configAttribute != null ? configAttribute.hashCode() : 0)
        result = 31 * result + (appName != null ? appName.hashCode() : 0)
        result = 31 * result + (displaySequence != null ? displaySequence.hashCode() : 0)
        result = 31 * result + (int) (pageId ^ (pageId >>> 32))
        result = 31 * result + (int) (gubapplAppId ^ (gubapplAppId >>> 32))
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (roleList != null ? roleList.hashCode() : 0)
        return result
    }
}
