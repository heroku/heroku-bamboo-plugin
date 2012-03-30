package com.heroku;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.herokuapp.directto.client.DirectToHerokuClient;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Brainard
 */
public abstract class AbstractDeploymentTask implements TaskType {

    private final StaticSandbox staticSandbox;

    public AbstractDeploymentTask() {
        this(new StaticSandbox() {
            public TaskResult success(TaskContext taskContext) {
                return TaskResultBuilder.create(taskContext).success().build();
            }
        });
    }

    public AbstractDeploymentTask(StaticSandbox staticSandbox) {
        this.staticSandbox = staticSandbox;
    }

    @NotNull
    @Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final String apiKey = taskContext.getConfigurationMap().get("apiKey");
        final String appName = taskContext.getConfigurationMap().get("appName");

        final DirectToHerokuClient directClient = new DirectToHerokuClient(apiKey);

        final Map<String, File> files = new HashMap<String, File>(1);
        addFiles(taskContext, files);

        final Map<String, String> deployResults;
        try {
            deployResults = directClient.deploy(getPipelineName(), appName, files);
        } catch (InterruptedException e) {
            throw new TaskException("Deployment was interrupted", e);
        }

        for (Map.Entry<String, String> result : deployResults.entrySet()) {
            buildLogger.addBuildLogEntry(result.getKey() + ":" + result.getValue());
        }

        return staticSandbox.success(taskContext);
    }

    protected abstract String getPipelineName();
    
    protected abstract void addFiles(TaskContext taskContext, Map<String, File> files);

    protected static interface StaticSandbox {
        TaskResult success(TaskContext taskContext);
    }
}
