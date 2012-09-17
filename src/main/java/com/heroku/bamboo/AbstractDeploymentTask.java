package com.heroku.bamboo;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import com.heroku.api.App;
import com.heroku.api.HerokuAPI;
import com.herokuapp.directto.client.DeployRequest;
import com.herokuapp.directto.client.DirectToHerokuClient;
import com.herokuapp.directto.client.EventSubscription;
import com.herokuapp.directto.client.VerificationException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.herokuapp.directto.client.EventSubscription.Event;
import static com.herokuapp.directto.client.EventSubscription.Event.POLL_START;
import static com.herokuapp.directto.client.EventSubscription.Event.UPLOAD_START;
import static com.herokuapp.directto.client.EventSubscription.Subscriber;

/**
 * @author Ryan Brainard
 */
public abstract class AbstractDeploymentTask<P extends DeploymentPipeline> extends AbstractHerokuTask {

    private final P pipeline;

    public AbstractDeploymentTask(P pipeline) {
        super();
        this.pipeline = pipeline;
    }

    public AbstractDeploymentTask(P pipeline, StaticSandbox staticSandbox) {
        super(staticSandbox);
        this.pipeline = pipeline;
    }

    @Override
    protected TaskResult execute(TaskContext taskContext, String apiKey, HerokuAPI api, App app) {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final String pipelineName = pipeline.getPipelineName();
        final DirectToHerokuClient client = new DirectToHerokuClient.Builder()
                .setApiKey(apiKey)
                .setConsumersUserAgent(HerokuPluginProperties.getUserAgent())
                .build();

        buildLogger.addBuildLogEntry("Preparing to deploy " + pipelineName + " to Heroku app " + app.getName());

        final Map<String, File> files = new HashMap<String, File>(pipeline.getRequiredFiles().size());
        final String workingDir = taskContext.getWorkingDirectory().getAbsolutePath() + "/";
        for (String file : pipeline.getRequiredFiles()) {
            final String filepath = workingDir + taskContext.getConfigurationMap().get(file);
            buildLogger.addBuildLogEntry("Adding [" + file  + "]: " + filepath);
            files.put(file, new File(filepath));
        }

        final DeployRequest deployRequest = new DeployRequest(pipelineName, app.getName(), files)
                .setEventSubscription(new EventSubscription()
                        .subscribe(UPLOAD_START, new Subscriber() {
                            public void handle(Event event) {
                                buildLogger.addBuildLogEntry("Uploading...");
                            }
                        })
                        .subscribe(POLL_START, new Subscriber() {
                            public void handle(Event event) {
                                buildLogger.addBuildLogEntry("Deploying...");
                            }
                        })
                );

        try {
            client.verify(deployRequest);
        } catch (VerificationException e) {
            for (String msg : e.getMessages()) {
                buildLogger.addErrorLogEntry(msg);
            }
            return staticSandbox.failed(taskContext);
        }

        final Map<String, String> deployResults = client.deploy(deployRequest);

        buildLogger.addBuildLogEntry("Launching... done, " +  deployResults.get("release"));
        buildLogger.addBuildLogEntry(app.getWebUrl() + " deployed to Heroku");

        return "success".equals(deployResults.get("status"))
                ? staticSandbox.success(taskContext)
                : staticSandbox.failed(taskContext);
    }

}
