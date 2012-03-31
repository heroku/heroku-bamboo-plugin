package com.heroku;

import com.atlassian.bamboo.task.TaskContext;

import java.io.File;
import java.util.Map;

public class WarDeploymentTask extends AbstractDeploymentTask {

    public WarDeploymentTask() {
        super();
    }
    
    public WarDeploymentTask(StaticSandbox staticSandbox) {
        super(staticSandbox);
    }

    @Override
    protected String getPipelineName() {
        return "war";
    }

    @Override
    protected void addFiles(TaskContext taskContext, Map<String, File> files) {
        files.put("war", absolutePath(taskContext, taskContext.getConfigurationMap().get("war")));
    }
}