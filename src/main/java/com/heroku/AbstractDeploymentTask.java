package com.heroku;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.herokuapp.directto.client.DirectToHerokuClient;
import com.herokuapp.directto.client.VerificationException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Brainard
 */
public abstract class AbstractDeploymentTask<P extends DeploymentPipeline> implements TaskType {

    /**
     * A sandbox for static methods that don't play well with jMock
     */
    protected static interface StaticSandbox {
        TaskResult success(TaskContext taskContext);
        TaskResult failed(TaskContext taskContext);
    }
    
    private final P pipeline;
    private final StaticSandbox staticSandbox;

    public AbstractDeploymentTask(P pipeline) {
        this(pipeline, new StaticSandbox() {
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

    public AbstractDeploymentTask(P pipeline, StaticSandbox staticSandbox) {
        this.pipeline = pipeline;
        this.staticSandbox = staticSandbox;
    }

    @NotNull
    @Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final String apiKey = taskContext.getConfigurationMap().get("apiKey");
        final String appName = taskContext.getConfigurationMap().get("appName");
        final String pipelineName = pipeline.getPipelineName();
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

        final Map<String, String> deployResults = client.deploy(pipelineName, appName, files);
        buildLogger.addBuildLogEntry("Deploying...");

        buildLogger.addBuildLogEntry("Deploy results:");
        for (Map.Entry<String, String> result : deployResults.entrySet()) {
            buildLogger.addBuildLogEntry(" - " + result.getKey() + ":" + result.getValue());
        }

        return "success".equals(deployResults.get("status"))
                ? staticSandbox.success(taskContext) 
                : staticSandbox.failed(taskContext);
    }

    protected void addFiles(TaskContext taskContext, Map<String, File> files) {
        final String workingDir = taskContext.getWorkingDirectory().getAbsolutePath() + "/";
        for (String reqFile : pipeline.getRequiredFiles()) {
            files.put(reqFile, new File(workingDir + taskContext.getConfigurationMap().get(reqFile)));
        }
    }
}
