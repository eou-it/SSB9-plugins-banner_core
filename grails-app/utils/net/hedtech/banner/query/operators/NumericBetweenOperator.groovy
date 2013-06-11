package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class NumericBetweenOperator extends BetweenOperator {
    public String getQueryString(CriteriaData data) {
        if(data.params.size() > 1) {
            CriteriaParam param1 = data.params.get(0);
            CriteriaParam param2 = data.params.get(1);

            return "${data.tableAlias}.${data.tableBindingAttribute} >= :${param1.paramKey} and ${data.tableAlias}.${data.tableBindingAttribute} <= :${param2.paramKey}"
        }
    }
}
