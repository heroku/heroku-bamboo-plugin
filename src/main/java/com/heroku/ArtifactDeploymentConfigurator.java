package com.heroku;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.opensymphony.xwork.TextProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ArtifactDeploymentConfigurator extends AbstractTaskConfigurator {
    private TextProvider textProvider;

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put("apiKey", params.getString("apiKey"));
        config.put("appName", params.getString("appName"));
        config.put("artifactPath", params.getString("artifactPath"));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);

//        context.put("say", "Hello, World!");
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);

        populateForViewAndEdit(context, taskDefinition);
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForView(context, taskDefinition);

        populateForViewAndEdit(context, taskDefinition);
    }

    private void populateForViewAndEdit(Map<String, Object> context, TaskDefinition taskDefinition) {
        context.put("apiKey", taskDefinition.getConfiguration().get("apiKey"));
        context.put("appName", taskDefinition.getConfiguration().get("appName"));
        context.put("artifactPath", taskDefinition.getConfiguration().get("artifactPath"));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        final String apiKey = params.getString("apiKey");
        if (StringUtils.isEmpty(apiKey)) {
            errorCollection.addError("apiKey", textProvider.getText("com.heroku.say.error"));
        }
        //TODO: more requiredess
    }

    public void setTextProvider(final TextProvider textProvider) {
        this.textProvider = textProvider;
    }
}
