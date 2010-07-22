<?xml version="1.0" encoding="UTF-8"?> 
<FooList xmlns="urn:com:sungardhe:Student"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             totalCount="${totalCount}" page="${params?.offset ? params?.offset : 0}" pageSize="${params?.max ? params?.max : 0}" apiVersion="1.0">
    <g:render template="/foo/foo.v1.0.xml" var="foo" collection="${fooList}" />
    <Ref>${refBase}/list</Ref>
</FooList>