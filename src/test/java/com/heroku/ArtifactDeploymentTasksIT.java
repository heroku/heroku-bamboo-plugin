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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class ArtifactDeploymentTasksIT extends MockObjectTestCase {

    private final Mock mockStatics = new Mock(WarDeploymentTask.StaticSandbox.class);
    private final Mock mockContext = new Mock(TaskContext.class);
    private final Mock mockLogger = new Mock(BuildLogger.class);
    private final Mock mockSuccessfulTaskResult = new Mock(TaskResult.class);
    private final Mock mockFailedTaskResult = new Mock(TaskResult.class);
    private final ConfigurationMapImpl configMap = new ConfigurationMapImpl();
    private final File workingDir = createTempDir();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockStatics.expects(anything()).method("success").will(returnValue(mockSuccessfulTaskResult.proxy()));
        mockStatics.expects(anything()).method("failed").will(returnValue(mockFailedTaskResult.proxy()));
        mockContext.expects(once()).method("getBuildLogger").will(returnValue(mockLogger.proxy()));
        mockContext.expects(atLeastOnce()).method("getConfigurationMap").will(returnValue(configMap));
        mockContext.expects(atLeastOnce()).method("getWorkingDirectory").will(returnValue(workingDir));
        mockLogger.expects(anything()).method("addBuildLogEntry");
        mockLogger.expects(anything()).method("addErrorLogEntry");
        mockSuccessfulTaskResult.expects(anything()).method("getTaskState").will(returnValue(TaskState.SUCCESS));
        mockFailedTaskResult.expects(anything()).method("getTaskState").will(returnValue(TaskState.FAILED));
    }
    
    protected TaskResult runTask(Class<? extends AbstractDeploymentTask> taskClass) throws Exception {
        configMap.put("apiKey", System.getProperty("heroku.apiKey"));
        configMap.put("appName", System.getProperty("heroku.appName"));
        TaskType task = taskClass.getConstructor(AbstractDeploymentTask.StaticSandbox.class).newInstance((AbstractDeploymentTask.StaticSandbox) mockStatics.proxy());
        return task.execute((TaskContext) mockContext.proxy());
    }
    
    public void testWarDeployment() throws Exception {
        configMap.put("war", File.createTempFile("some", ".war", workingDir).getName());
        final TaskResult taskResult = runTask(WarDeploymentTask.class);
        assertEquals(TaskState.SUCCESS, taskResult.getTaskState());
    }

    public void testFatJarDeployment() throws Exception {
        configMap.put("jar", File.createTempFile("some", ".jar", workingDir).getName());
        configMap.put("procfile", createProcfile(workingDir).getName());
        assertEquals(TaskState.SUCCESS, runTask(FatJarDeploymentTask.class).getTaskState());
    }

    public void testTarGzDeployment() throws Exception {
        configMap.put("targz", File.createTempFile("some", ".tar.gz", workingDir).getName());
        configMap.put("procfile", createProcfile(workingDir).getName());
        assertEquals(TaskState.SUCCESS, runTask(TarGzDeploymentTask.class).getTaskState());
    }

    InvocationMatcher anything() {
        return new AnyArgumentsMatcher();
    }
    
    private static File createTempDir() {
        try {
            final File tmp = File.createTempFile("temp", "dir");
            if (!tmp.delete()) throw new IOException("Could not delete");
            if (!tmp.mkdir()) throw new IOException("Could not mkdir");
            return tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private File createProcfile(File dir) {
        try {
            final File procfile = File.createTempFile("Procfile", "", dir);
            PrintWriter writer = new PrintWriter(procfile);
            writer.append("web: exit");
            writer.close();
            return procfile;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
