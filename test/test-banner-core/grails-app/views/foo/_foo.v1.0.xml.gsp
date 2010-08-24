<Foo id="${foo?.id}" apiVersion='1.0' systemRequiredIndicator="${(foo?.systemRequiredIndicator ? true : false)}" lastModified="${foo?.lastModified}" lastModifiedBy="${foo?.lastModifiedBy}" dataOrigin="${foo?.dataOrigin}" optimisticLockVersion="${foo?.version}">
    <Code>${foo?.code}</Code>
    <Description>${foo?.description}</Description>
    <g:if test="${foo?.addressStreetLine1}"><AddressStreetLine1>${foo?.addressStreetLine1}</AddressStreetLine1></g:if>
    <g:if test="${foo?.addressStreetLine2}"><AddressStreetLine2>${foo?.addressStreetLine2}</AddressStreetLine2></g:if>
    <g:if test="${foo?.addressStreetLine3}"><AddressStreetLine3>${foo?.addressStreetLine3}</AddressStreetLine3></g:if>
    <g:if test="${foo?.addressStreetLine4}"><AddressStreetLine4>${foo?.addressStreetLine4}</AddressStreetLine4></g:if>
    <g:if test="${foo?.houseNumber}"><HouseNumber>${foo?.houseNumber}</HouseNumber></g:if>
    <g:if test="${foo?.addressCity}"><AddressCity>${foo?.addressCity}</AddressCity></g:if>
    <g:if test="${foo?.addressState}"><AddressState>${foo?.addressState}</AddressState></g:if>
    <g:if test="${foo?.addressCountry}"><AddressCountry>${foo?.addressCountry}</AddressCountry></g:if>
    <g:if test="${foo?.addressZipCode}"><AddressZipCode>${foo?.addressZipCode}</AddressZipCode></g:if>
    <g:if test="${foo?.voiceResponseMessageNumber}"><VoiceResponseMessageNumber>${foo?.voiceResponseMessageNumber}</VoiceResponseMessageNumber></g:if>
    <g:if test="${foo?.statisticsCanadianInstitution}"><StatisticsCanadianInstitution>${foo?.statisticsCanadianInstitution}</StatisticsCanadianInstitution></g:if>
    <g:if test="${foo?.districtDivision}"><DistrictDivision>${foo?.districtDivision}</DistrictDivision></g:if>
    <Ref>${refBase}/${foo?.id}</Ref>
</Foo>