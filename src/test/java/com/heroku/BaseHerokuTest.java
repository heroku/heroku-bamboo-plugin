package com.heroku;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMapImpl;
import com.atlassian.bamboo.security.StringEncrypter;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskState;
import com.heroku.api.HerokuAPI;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.InvocationMatcher;
import org.jmock.core.matcher.AnyArgumentsMatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Ryan Brainard
 */
public class BaseHerokuTest extends MockObjectTestCase {

    protected final Mock mockStatics = new Mock(WarDeploymentTask.StaticSandbox.class);
    protected final Mock mockContext = new Mock(TaskContext.class);
    protected final Mock mockLogger = new Mock(BuildLogger.class);
    protected final Mock mockSuccessfulTaskResult = new Mock(TaskResult.class);
    protected final Mock mockFailedTaskResult = new Mock(TaskResult.class);
    protected final ConfigurationMapImpl configMap = new ConfigurationMapImpl();
    protected final File workingDir = createTempDir();

    protected final String apiKey = System.getProperty("heroku.apiKey");
    protected final HerokuAPI api = new HerokuAPI(apiKey);
    protected final String encryptedApiKey = new StringEncrypter().encrypt(apiKey);
    protected final String appName = System.getProperty("heroku.appName");

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mockStatics.expects(anything()).method("success").will(returnValue(mockSuccessfulTaskResult.proxy()));
        mockStatics.expects(anything()).method("failed").will(returnValue(mockFailedTaskResult.proxy()));
        mockContext.expects(atLeastOnce()).method("getBuildLogger").will(returnValue(mockLogger.proxy()));
        mockContext.expects(atLeastOnce()).method("getConfigurationMap").will(returnValue(configMap));
        mockContext.expects(atLeastOnce()).method("getWorkingDirectory").will(returnValue(workingDir));
        mockLogger.expects(anything()).method("addBuildLogEntry");
        mockLogger.expects(anything()).method("addErrorLogEntry");
        mockSuccessfulTaskResult.expects(anything()).method("getTaskState").will(returnValue(TaskState.SUCCESS));
        mockFailedTaskResult.expects(anything()).method("getTaskState").will(returnValue(TaskState.FAILED));

        configMap.put("apiKey", encryptedApiKey);
        configMap.put("appName", appName);
    }

    protected InvocationMatcher anything() {
        return new AnyArgumentsMatcher();
    }

    protected static File createTempDir() {
        try {
            final File tmp = File.createTempFile("temp", "dir");
            if (!tmp.delete()) throw new IOException("Could not delete");
            if (!tmp.mkdir()) throw new IOException("Could not mkdir");
            return tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected File createProcfile(File dir) {
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

    public void assertStringContains(String message, String haystack, String needle) {
        if (haystack.contains(needle)) {
            // good
            return;
        } else {
            fail(message + " (seeking '" + needle + "')");
        }
    }

    public void assertStringContains(String haystack, String needle) {
        if (haystack.contains(needle)) {
            // good
            return;
        } else {
            fail("Could not find '" + needle + "'.");
        }
    }
}
