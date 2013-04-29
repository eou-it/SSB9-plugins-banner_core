package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

class InOperator extends CriteriaOperator{

    public InOperator () {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.in";
        this.operator = "in";
        this.key = Operators.IN
    }

    public String getQueryString(CriteriaData data) {
        if(ifParamsExists(data)) {
            CriteriaParam param = data.params.get(0);

            List params = getParamsFromCriteriaParams(param.data)
            if(params.get(0) instanceof Date) {
               return new DateInOperator().getQueryString(data)
            }

            return "${data.tableAlias}.${data.tableBindingAttribute} IN (:${param.paramKey})"
        }
        return ""
    }

    private getParamsFromCriteriaParams(List criteriaParams) {
        List params = new ArrayList();
        int numberOfCriteriaParams = criteriaParams.size()
        for(int counter = 0; counter < numberOfCriteriaParams; counter++) {
            params.add(criteriaParams.get(counter).data)
        }
        return params
    }
}
