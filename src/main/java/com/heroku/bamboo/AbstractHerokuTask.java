package com.heroku.bamboo;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.security.EncryptionService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.heroku.api.App;
import com.heroku.api.Heroku;
import com.heroku.api.HerokuAPI;
import com.heroku.api.exception.RequestFailedException;
import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;

/**
 * @author Ryan Brainard
 */
public abstract class AbstractHerokuTask implements TaskType {

    protected final StaticSandbox staticSandbox;
    protected EncryptionService encryptionService;

    protected AbstractHerokuTask() {
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

    public AbstractHerokuTask(StaticSandbox staticSandbox) {
        this.staticSandbox = staticSandbox;
    }

    @NotNull
    @Override
    public TaskResult execute(@NotNull final TaskContext taskContext) throws TaskException {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
        final String apiKey = encryptionService.decrypt(taskContext.getConfigurationMap().get(AbstractDeploymentTaskConfigurator.API_KEY));
        final HerokuAPI api = new HerokuAPI(apiKey);

        try {
            final App app = getOrCreateApp(buildLogger, api, taskContext.getConfigurationMap().get("appName"));
            return execute(taskContext, apiKey, api, app);
        } catch (HerokuBambooHandledException e) {
            taskContext.getBuildLogger().addErrorLogEntry(e.getMessage());
            return staticSandbox.failed(taskContext);
        }
    }

    protected abstract TaskResult execute(TaskContext taskContext, String apiKey, HerokuAPI api, App app);

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

    /**
     * A sandbox for static methods that don't play well with jMock
     */
    protected static interface StaticSandbox {
        TaskResult success(TaskContext taskContext);
        TaskResult failed(TaskContext taskContext);
    }

    /** Spring setter */
    public void setEncryptionService(EncryptionService encryptionService)
    {
        this.encryptionService = encryptionService;
    }
}
