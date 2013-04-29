package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

class NumericBetweenOperator extends BetweenOperator {
    public String getQueryString(CriteriaData data) {
        if(data.params.size() > 1) {
            CriteriaParam param1 = data.params.get(0);
            CriteriaParam param2 = data.params.get(1);

            return "${data.tableAlias}.${data.tableBindingAttribute} >= :${param1.paramKey} and ${data.tableAlias}.${data.tableBindingAttribute} <= :${param2.paramKey}"
        }
    }
}
