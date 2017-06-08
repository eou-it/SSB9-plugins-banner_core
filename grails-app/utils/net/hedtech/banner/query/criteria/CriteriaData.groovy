/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.criteria

class CriteriaData {
    private List<CriteriaParam> params = new ArrayList<CriteriaParam>();

    public void addParam(CriteriaParam criteriaParam) {
        params.add(criteriaParam)
    }

    public void addParams(List<CriteriaParam> params) {
        this.params = params
    }
}
