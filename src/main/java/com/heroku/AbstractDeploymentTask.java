package com.heroku;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.security.StringEncrypter;
import com.atlassian.bamboo.task.*;
import com.heroku.api.App;
import com.heroku.api.Heroku;
import com.heroku.api.HerokuAPI;
import com.heroku.api.exception.RequestFailedException;
import com.herokuapp.directto.client.DeployRequest;
import com.herokuapp.directto.client.DirectToHerokuClient;
import com.herokuapp.directto.client.EventSubscription;
import com.herokuapp.directto.client.VerificationException;
import org.apache.tools.ant.BuildListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static com.herokuapp.directto.client.EventSubscription.Event;
import static com.herokuapp.directto.client.EventSubscription.Event.POLL_START;
import static com.herokuapp.directto.client.EventSubscription.Event.UPLOAD_START;
import static com.herokuapp.directto.client.EventSubscription.Subscriber;

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
        try {
            return executeInternal(taskContext);
        } catch (HerokuBambooHandledException e) {
            taskContext.getBuildLogger().addErrorLogEntry(e.getMessage());
            return staticSandbox.failed(taskContext);
        }
    }

    private TaskResult executeInternal(TaskContext taskContext) {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final String apiKey = new StringEncrypter().decrypt(taskContext.getConfigurationMap().get(AbstractDeploymentTaskConfigurator.API_KEY));
        final String pipelineName = pipeline.getPipelineName();
        final HerokuAPI api = new HerokuAPI(apiKey);
        final App app = getOrCreateApp(buildLogger, api, taskContext.getConfigurationMap().get("appName"));
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

        buildLogger.addBuildLogEntry("Released " + deployResults.get("release") + " to " + app.getName());

        return "success".equals(deployResults.get("status"))
                ? staticSandbox.success(taskContext)
                : staticSandbox.failed(taskContext);
    }

    protected App getOrCreateApp(BuildLogger buildLogger, HerokuAPI api, String appName) {
        App app;

        try {
            app = api.getApp(appName);
        } catch (RequestFailedException appListingException) {
            if (appListingException.getStatusCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new HerokuBambooHandledException("No access to Heroku app '" + appName + "'. Check API key, app name, and ensure you have access.");
            }

            try {
                app = api.createApp(new App().named(appName).on(Heroku.Stack.Cedar));
                buildLogger.addBuildLogEntry("Created new app " + appName);
            } catch (RequestFailedException appCreationException) {
                if (appCreationException.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    throw new HerokuBambooHandledException("No access to create Heroku app '" + appName + "'. Check API key.");
                }

                buildLogger.addErrorLogEntry("Unknown error creating app '" + appName + "'\n" + appCreationException.getMessage());
                throw appCreationException;
            }
        }

        if (app == null || app.getId() == null) {
            throw new HerokuBambooHandledException("Heroku app '" + appName + "' could not be found. Check API key, app name, and ensure you have access.");
        }

        return app;
    }

}
