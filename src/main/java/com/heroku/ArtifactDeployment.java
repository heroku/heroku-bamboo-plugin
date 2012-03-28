package com.heroku;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.herokuapp.directto.client.WarDeployer;
import org.jetbrains.annotations.NotNull;
import scala.collection.immutable.Map;

import java.io.File;

public class ArtifactDeployment implements TaskType {
    @NotNull
    @java.lang.Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final String apiKey = taskContext.getConfigurationMap().get("apiKey");
        final String appName = taskContext.getConfigurationMap().get("appName");
        final String artifactPath = taskContext.getConfigurationMap().get("artifactPath");

        final WarDeployer warDeploy = new WarDeployer(apiKey);
        final Map<String, String> deployResult = warDeploy.deploy(appName, new File(artifactPath));
        buildLogger.addBuildLogEntry(deployResult.toString());

        return TaskResultBuilder.create(taskContext).success().build();
    }
}