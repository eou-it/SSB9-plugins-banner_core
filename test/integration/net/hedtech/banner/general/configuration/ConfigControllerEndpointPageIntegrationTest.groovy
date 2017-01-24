/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.hibernate.Session
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigControllerEndpointPageIntegrationTest is used to test the ConfigControllerEndpointPage domain.
 */
class ConfigControllerEndpointPageIntegrationTest extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testFindAll() {
        ConfigControllerEndpointPage endpointPage = getDomain()
        //Save and findAll
        endpointPage.save(failOnError: true, flush: true)
        def list = endpointPage.findAll()
        assert (list.size > 0)
        assert (list.getAt(0).dataOrigin == 'Banner')
        assert (list.getAt(0).enableDisable == 'E')

        //Update and findAll
        endpointPage.setEnableDisable('D')
        endpointPage.save(failOnError: true, flush: true)
        list = endpointPage.findAll()
        assert (list.size > 0)
        assert (list.getAt(0).dataOrigin == 'Banner')
        assert (list.getAt(0).enableDisable == 'D')

        //Delete and findAll
        endpointPage.delete()
        list = endpointPage.findAll()
        assert (list.size >= 0)
    }

    @Test
    public void testGetAllConfigByAppNameWithSave() {
        ConfigApplication configApplication = getConfigApplication()
        configApplication.save(failOnError: true, flush: true)

        ConfigRolePageMapping configRolePageMapping = getConfigRolePageMapping()
        configRolePageMapping.save(failOnError: true, flush: true)

        ConfigControllerEndpointPage endpointPage = getDomain()
        endpointPage.save(failOnError: true, flush: true)

        def list = endpointPage.getAllConfigByAppName('PlatformSandboxApp')
        assert (list.size() > 0)
        assert (list.getAt(0) instanceof RequestURLMap)
    }

    @Test
    public void testGetAllConfigByAppNameWithHibernateSessionWithSave() {
        Session session = getHibernateSession()
        session.beginTransaction()

        saveRequiredDomains(session)

        def list = session.getNamedQuery(ConfigControllerEndpointPage.GET_ALL_CONFIG_BY_APP_NAME)
                .setString('appName', 'PlatformSandboxApp').list()
        assert (list.size() > 0)
        assert (list.getAt(0) instanceof RequestURLMap)
    }

    /**
     * The intention behind this method is to have this test method
     * is : In POC 2 implementation it was native SQL query and we have used
     * LISTAGG to get comma seperated string from the DB for multiple roles and other values
     * for same URL, so here we have managed it in a server side, this method implementation
     * will represent the same.
     *
     * And this will use getHibernateSession() to get the hibernate session in the way
     * where the POC 2 is doing, because when spring invoking RequestmapFilterInvocationDefinition
     * we have to load this hibernate manually.
     */
    @Test
    public void testGetAllConfigByAppNameWithHibernateSession() {
        Session session = getHibernateSession()
        session.beginTransaction()

        // Save all required domain.
        saveRequiredDomains(session)

        def list = session.getNamedQuery(ConfigControllerEndpointPage.GET_ALL_CONFIG_BY_APP_NAME)
                .setString('appName', 'PlatformSandboxApp').list()
        def urlSet = new LinkedHashSet<String>()
        list.each { RequestURLMap requestURLMap -> urlSet.add(requestURLMap.url) }

        def requestMap = new LinkedHashMap<String, ArrayList<RequestURLMap>>()
        urlSet.each { String url ->
            def patternList = new ArrayList<RequestURLMap>()
            list.each { RequestURLMap requestURLMap ->
                if (requestURLMap.url.equals(url)) {
                    patternList << requestURLMap
                }
            }
            requestMap.put(url, patternList)
        }
        assert (requestMap.size() >= 0)
    }

    /**
     * Saving the required domains for test.
     * @param session Hibernate session.
     */
    private void saveRequiredDomains(Session session) {
        ConfigApplication configApplication = getConfigApplication()
        session.save(configApplication)

        ConfigRolePageMapping configRolePageMapping = getConfigRolePageMapping()
        session.save(configRolePageMapping)

        ConfigControllerEndpointPage endpointPage = getDomain()
        session.save(endpointPage)
    }

    /**
     * Mocking the ConfigControllerEndpointPage domain.
     * @return ConfigControllerEndpointPage
     */
    private ConfigControllerEndpointPage getDomain() {
        ConfigControllerEndpointPage configControllerEndpointPage = new ConfigControllerEndpointPage(
                activityDate: new Date(),
                description: 'TEST',
                displaySequence: 1,
                enableDisable: 'E',
                gubapplAppId: 1,
                pageId: 1,
                pageName: 'TEST PAGE',
                userId: 'TEST_USER',
                id: 1, //TODO ID has to be removed once primary key added to table with sequence generator
                version: 0
        )
        return configControllerEndpointPage
    }

    /**
     * Mocking ConfigRolePageMapping domain.
     * @return ConfigRolePageMapping
     */
    private ConfigRolePageMapping getConfigRolePageMapping() {
        ConfigRolePageMapping configRolePageMapping = new ConfigRolePageMapping(
                userId: 'TEST_USER',
                pageId: 1,
                gubapplAppId: 1,
                activityDate: new Date(),
                id: 1, //TODO ID has to be removed once primary key added to table with sequence generator
                roleCode: 'TEST_ROLE',
                version: 0,
        )
        return configRolePageMapping
    }

    /**
     * Mocking ConfigApplication domain.
     * @return ConfigApplication
     */
    private ConfigApplication getConfigApplication() {
        ConfigApplication configApplication = new ConfigApplication(
                version: 0,
                id: 1,
                activityDate: new Date(),
                appId: 1, //TODO ID has to be removed once primary key added to table with sequence generator
                appName: 'PlatformSandboxApp',
                userId: 'TEST_USER',
        )
        return configApplication
    }

    /**
     * This private method will be used to test for RequestmapFilterInvocationDefinition interceptor
     * which the filter is invoked before grails config.
     * @return Session Hibernate session.
     */
    private Session getHibernateSession() {
        def dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
        def ctx = Holders.grailsApplication.mainContext
        def sessionFactory = ctx.sessionFactory
        Session session = sessionFactory.openSession(dataSource.getSsbConnection())
        return session
    }

}
