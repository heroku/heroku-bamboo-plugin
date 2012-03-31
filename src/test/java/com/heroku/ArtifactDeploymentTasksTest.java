package com.heroku;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMapImpl;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskState;
import com.atlassian.bamboo.task.TaskType;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.InvocationMatcher;
import org.jmock.core.matcher.AnyArgumentsMatcher;

import java.io.File;

public class ArtifactDeploymentTasksTest extends MockObjectTestCase {

    private final Mock mockStatics = new Mock(WarDeploymentTask.StaticSandbox.class);
    private final Mock mockContext = new Mock(TaskContext.class);
    private final Mock mockLogger = new Mock(BuildLogger.class);
    private final Mock mockSuccessfulTaskResult = new Mock(TaskResult.class);
    private final Mock mockFailedTaskResult = new Mock(TaskResult.class);
    private final ConfigurationMapImpl configMap = new ConfigurationMapImpl();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockStatics.expects(anything()).method("success").will(returnValue(mockSuccessfulTaskResult.proxy()));
        mockStatics.expects(anything()).method("failed").will(returnValue(mockFailedTaskResult.proxy()));
        mockContext.expects(once()).method("getBuildLogger").will(returnValue(mockLogger.proxy()));
        mockContext.expects(atLeastOnce()).method("getConfigurationMap").will(returnValue(configMap));
        mockContext.expects(atLeastOnce()).method("getWorkingDirectory").will(returnValue(new File(System.getProperty("heroku.workingDir"))));
        mockLogger.expects(anything()).method("addBuildLogEntry");
        mockLogger.expects(anything()).method("addErrorLogEntry");
        mockSuccessfulTaskResult.expects(anything()).method("getTaskState").will(returnValue(TaskState.SUCCESS));
        mockFailedTaskResult.expects(anything()).method("getTaskState").will(returnValue(TaskState.FAILED));
    }
    
    protected TaskResult runTask(Class<? extends AbstractDeploymentTask> taskClass, Class<? extends AbstractDeploymentTaskConfigurator> configuratorClass) throws Exception {
        configMap.put("apiKey", System.getProperty("heroku.apiKey"));
        configMap.put("appName", System.getProperty("heroku.appName"));
        TaskType task = taskClass.getConstructor(AbstractDeploymentTask.StaticSandbox.class).newInstance((AbstractDeploymentTask.StaticSandbox) mockStatics.proxy());
        return task.execute((TaskContext) mockContext.proxy());
    }
    
    public void testWarDeployment() throws Exception {
        configMap.put("war", System.getProperty("heroku.warFile"));
        assertEquals(TaskState.SUCCESS, runTask(WarDeploymentTask.class, WarDeploymentTaskConfigurator.class).getTaskState());
    }

    public void testFatJarDeployment() throws Exception {
        configMap.put("jar", System.getProperty("heroku.jarFile"));
        configMap.put("procfile", System.getProperty("heroku.procfileFile"));
        assertEquals(TaskState.SUCCESS, runTask(FatJarDeploymentTask.class, FatJarDeploymentTaskConfigurator.class).getTaskState());
    }
    
    InvocationMatcher anything() {
        return new AnyArgumentsMatcher();
    }
}
