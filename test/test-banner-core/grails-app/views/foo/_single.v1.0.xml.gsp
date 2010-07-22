<?xml version="1.0" encoding="UTF-8"?> 
<FooInstance xmlns="urn:com:sungardhe:Student" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" apiVersion="1.0">
    <g:render template="/foo/foo.v1.0.xml" model="${[foo: foo]}" />
    <Ref>${refBase}/${foo?.id}</Ref>
</FooInstance>