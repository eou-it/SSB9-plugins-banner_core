package net.hedtech.banner.query.criteria

class CriteriaParam {
    private String paramKey
    private Object data;
    private Map additionalAttributes = new HashMap()

    public addAttribute(String attribute, Object value) {
        additionalAttributes.put(attribute, value)
    }

    public Object getAttribute(String attribute) {
        additionalAttributes.get(attribute);
    }
}
