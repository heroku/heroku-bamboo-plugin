package com.heroku.bamboo;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.CommonTaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.heroku.api.App;
import com.heroku.api.HerokuAPI;
import com.heroku.api.exception.RequestFailedException;

/**
 * @author Ryan Brainard
 */
public class AbstractHerokuTaskIT  extends BaseHerokuTest {

    final AbstractHerokuTask step = new AbstractHerokuTask() {
        @Override
        protected TaskResult execute(CommonTaskContext taskContext, String apiKey, HerokuAPI api, App app) {
            throw new UnsupportedOperationException();
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockContext.reset();
    }

    public void testGetOrCreateApp_AlreadyExists() throws Exception {
        assertEquals("Precondition: App should already exist", appName, api.getApp(appName).getName());
        assertEquals(appName, step.getOrCreateApp((BuildLogger) mockLogger.proxy(), api, appName).getName());
    }

    public void testGetOrCreateApp_NewAppCreated() throws Exception {
        final String newAppName = "testapp" + System.currentTimeMillis();
        try {
            api.getApp(newAppName);
            fail();
        } catch (RequestFailedException e) {
            assertTrue("Precondition: App should not already exist", e.getMessage().contains("Unable to get app"));
        }

        try {
            assertEquals(newAppName, step.getOrCreateApp((BuildLogger) mockLogger.proxy(), api, newAppName).getName());
        } finally {
            api.destroyApp(newAppName);
        }
    }

    public void testGetOrCreateApp_BadApiKey() throws Exception {
        final String nonNullBadApiKey = "NON_NULL_BAD_API_KEY";
        final HerokuAPI badApi = new HerokuAPI(api.getConnection(), nonNullBadApiKey);

        final String newAppName = "test" + System.currentTimeMillis();
        assertFalse("Precondition: App should not already exist", api.appExists(newAppName));
        try {
            step.getOrCreateApp((BuildLogger) mockLogger.proxy(), badApi, appName).getName();
            fail();
        } catch (HerokuBambooHandledException e) {
            assertStringContains(e.getMessage(), "No access to create Heroku app");
        }
    }

    public void testGetOrCreateApp_AlreadyExists_WithoutAccess() throws Exception {
        final String existingAppWithoutAccess = "java";
        assertTrue("Precondition: App should already exist", api.appExists(existingAppWithoutAccess));

        try {
            api.getApp(existingAppWithoutAccess).getName();
            fail("Precondition: User should not have access");
        } catch (RequestFailedException e) {
            assertStringContains(e.getResponseBody(), "You do not have access to the app");
        }

        try {
            step.getOrCreateApp((BuildLogger) mockLogger.proxy(), api, existingAppWithoutAccess).getName();
            fail();
        } catch (HerokuBambooHandledException e){
            assertStringContains(e.getMessage(), "No access to Heroku app '" + existingAppWithoutAccess);
        }
    }
}
