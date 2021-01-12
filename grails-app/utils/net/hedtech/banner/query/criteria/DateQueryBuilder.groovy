/*******************************************************************************
 Copyright 2013-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.criteria

class DateQueryBuilder {
    StringBuilder stringBuilder
    public DateQueryBuilder() {
        stringBuilder = new StringBuilder()
        stringBuilder.append(" ( ")
    }
    public void append(String str) {
        stringBuilder.append(str);
    }

    public void appendDateSupport(String str) {
        stringBuilder.append("TO_DATE(TO_CHAR(")
        stringBuilder.append(str)
        stringBuilder.append(",'MM-DD-YYYY'), 'MM-DD-YYYY')")
    }


    public void appendDateTimeSupport(String str) {
        stringBuilder.append("to_timestamp(TO_CHAR(")
        stringBuilder.append(str)
        stringBuilder.append(",'MM-DD-YYYY HH24:MI:SS'), 'MM-DD-YYYY HH24:MI:SS')")
    }

    public String toString() {
        return stringBuilder.append(" ) ").toString();
    }
}
