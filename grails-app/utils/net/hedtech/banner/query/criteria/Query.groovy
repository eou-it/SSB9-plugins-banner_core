package net.hedtech.banner.query.criteria

import net.hedtech.banner.query.operators.CriteriaOperator
import net.hedtech.banner.query.CriteriaOperatorFactory

class Query {
    String query;

    private Query (String query) {
        this.query = query;
    }

    public static Query createQuery(String query) {
        return new Query(query);
    }

    public static Query and(Query query1, Query query2) {
        return new Query(query1.toString() + " and " + query2.toString())
    }

    public void and(Query query) {
           this.query = this.and(query.toString())
    }

    public void and(String query) {
        this.query = this.query + " and " + query
    }

    public static Query or(Query query1, Query query2) {
        return new Query(query1.toString() + " or " + query2.toString())
    }

    public void or(Query query) {
          this.query = this.or(query.toString())
    }

    public void or(String query) {
       this.query = this.query + " and " + query
    }

    public static Query orderBy(Query query1, Query query2) {
        return new Query(query1.toString() + " order by " + query2.toString())
    }

    public void orderBy(Query query) {
        this.query = this.orderBy(query.toString())
    }

    public void orderBy(String query) {
        this.query = this.query + " order by " + query
    }

    public static Query equals(CriteriaData data) {
        CriteriaOperator equals = CriteriaOperatorFactory.getCriteriaOperator("equals")
        return equals.getQuery(data)
    }

    public String toString() {
        return query;
    }
}
