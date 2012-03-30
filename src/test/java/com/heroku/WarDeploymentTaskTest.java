package com.heroku;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMapImpl;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskState;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class WarDeploymentTaskTest extends MockObjectTestCase {

    private final Mock mockStatics = new Mock(WarDeploymentTask.StaticSandbox.class);
    private final Mock mockContext = new Mock(TaskContext.class);
    private final Mock mockLogger = new Mock(BuildLogger.class);
    private final Mock mockSuccessfulTaskResult = new Mock(TaskResult.class);
    private final ConfigurationMapImpl configMap = new ConfigurationMapImpl();
    private WarDeploymentTask deploymentTaskTask;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockStatics.expects(atLeastOnce()).method("success").will(returnValue(mockSuccessfulTaskResult.proxy()));
        mockContext.expects(once()).method("getBuildLogger").will(returnValue(mockLogger.proxy()));
        mockContext.expects(atLeastOnce()).method("getConfigurationMap").will(returnValue(configMap));
        mockLogger.expects(atLeastOnce()).method("addBuildLogEntry");
        mockSuccessfulTaskResult.expects(atLeastOnce()).method("getTaskState").will(returnValue(TaskState.SUCCESS));
        deploymentTaskTask = new WarDeploymentTask((WarDeploymentTask.StaticSandbox) mockStatics.proxy());
    }

    public void testDeployment() throws Exception {
        configMap.put("apiKey", System.getProperty("heroku.apiKey"));
        configMap.put("appName", System.getProperty("heroku.appName"));
        configMap.put("artifactPath", System.getProperty("heroku.warFile"));

        assertEquals(TaskState.SUCCESS, deploymentTaskTask.execute((TaskContext) mockContext.proxy()).getTaskState());
    }

}
