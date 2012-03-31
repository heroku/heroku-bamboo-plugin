package com.heroku;

import com.atlassian.bamboo.task.TaskContext;

import java.io.File;
import java.util.Map;

public class FatJarDeploymentTask extends AbstractDeploymentTask {

    public FatJarDeploymentTask() {
        super();
    }

    public FatJarDeploymentTask(StaticSandbox staticSandbox) {
        super(staticSandbox);
    }

    @Override
    protected String getPipelineName() {
        return "fatjar";
    }

    @Override
    protected void addFiles(TaskContext taskContext, Map<String, File> files) {
        files.put("jar", absolutePath(taskContext, taskContext.getConfigurationMap().get("jar")));
        files.put("procfile", absolutePath(taskContext, taskContext.getConfigurationMap().get("procfile")));
    }
}