package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

class BetweenOperator extends CriteriaOperator {
    public BetweenOperator () {
       this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.between";
       this.operator = "between";
       this.key = Operators.BETWEEN
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            CriteriaParam param1 = data.params.get(0);
            CriteriaParam param2 = data.params.get(1);

            if(param1.data instanceof Integer && param2.data instanceof Integer) {
                return new NumericBetweenOperator().getQueryString(data)
            }
            if(param1.data instanceof Date && param2.data instanceof Date) {
                return new DateBetweenOperator().getQueryString(data)
            }
        }
        return "${data.tableAlias}.${data.tableBindingAttribute} between :${data.paramKey} and :${data.paramKey}_and"
    }
}
