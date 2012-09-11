package com.heroku;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMapImpl;
import com.atlassian.bamboo.security.StringEncrypter;
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

public class ArtifactDeploymentTasksIT extends BaseHerokuTest {

    protected TaskResult runTask(Class<? extends AbstractDeploymentTask> taskClass) throws Exception {
        TaskType task = taskClass.getConstructor(AbstractDeploymentTask.StaticSandbox.class).newInstance((AbstractDeploymentTask.StaticSandbox) mockStatics.proxy());
        return task.execute((TaskContext) mockContext.proxy());
    }
    
    public void testWarDeployment() throws Exception {
        configMap.put("war", File.createTempFile("some", ".war", workingDir).getName());
        final TaskResult taskResult = runTask(WarDeploymentTask.class);
        assertEquals(TaskState.SUCCESS, taskResult.getTaskState());
    }

}
