package com.heroku.bamboo;

import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskState;
import com.atlassian.bamboo.task.TaskType;

import java.io.File;

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
