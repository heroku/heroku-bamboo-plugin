package com.heroku;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.herokuapp.directto.client.DirectToHerokuClient;
import com.herokuapp.directto.client.VerificationException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Ryan Brainard
 */
public abstract class AbstractDeploymentTask implements TaskType {

    /**
     * A sandbox for static methods that don't play well with jMock
     */
    protected static interface StaticSandbox {
        TaskResult success(TaskContext taskContext);
        TaskResult failed(TaskContext taskContext);
    }
    
    private final StaticSandbox staticSandbox;

    public AbstractDeploymentTask() {
        this(new StaticSandbox() {
            @Override
            public TaskResult success(TaskContext taskContext) {
                return TaskResultBuilder.create(taskContext).success().build();
            }

            @Override
            public TaskResult failed(TaskContext taskContext) {
                return TaskResultBuilder.create(taskContext).failed().build();
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
        final String pipelineName = getPipelineName();
        final DirectToHerokuClient client = new DirectToHerokuClient(apiKey);

        buildLogger.addBuildLogEntry("Preparing to deploy to Heroku app [" + appName + "] via [" + pipelineName + "] pipeline");
        
        final Map<String, File> files = new HashMap<String, File>(1);
        addFiles(taskContext, files);
        for (Map.Entry<String,File> file : files.entrySet()) {
            buildLogger.addBuildLogEntry("Adding [" + file.getKey()  + "]: " + file.getValue());
        }

        try {
            client.verify(pipelineName, appName, files);
        } catch (VerificationException e) {
            for (String msg : e.getMessages()) {
                buildLogger.addErrorLogEntry(msg);
            }
            return staticSandbox.failed(taskContext);
        }

        final Future<Map<String, String>> deployFuture = client.deployAsync(pipelineName, appName, files);
        buildLogger.addBuildLogEntry("Deploying...");

        final Map<String, String> deployResults;
        try {
            deployResults = deployFuture.get(5, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            buildLogger.addErrorLogEntry("Deploy timeout", e);
            return staticSandbox.failed(taskContext);
        } catch (InterruptedException e) {
            buildLogger.addErrorLogEntry("Deploy interrupted", e);
            return staticSandbox.failed(taskContext);
        } catch (ExecutionException e) {
            buildLogger.addErrorLogEntry("Unknown deploy error", e);
            return staticSandbox.failed(taskContext);
        }

        buildLogger.addBuildLogEntry("Deploy results:");
        for (Map.Entry<String, String> result : deployResults.entrySet()) {
            buildLogger.addBuildLogEntry(" - " + result.getKey() + ":" + result.getValue());
        }

        return "success".equals(deployResults.get("status"))
                ? staticSandbox.success(taskContext) 
                : staticSandbox.failed(taskContext);
    }
    
    protected abstract String getPipelineName();
    
    protected abstract void addFiles(TaskContext taskContext, Map<String, File> files);
}
