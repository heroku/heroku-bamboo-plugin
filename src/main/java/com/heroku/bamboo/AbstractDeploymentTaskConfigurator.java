package com.heroku.bamboo;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionService;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class AbstractDeploymentTaskConfigurator extends AbstractTaskConfigurator implements DeploymentPipeline {

    protected static final String API_KEY = "apiKey";
    protected static final String APP_NAME = "appName";

    protected EncryptionService encryptionService;

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, getFieldsToCopy());
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForView(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, getFieldsToCopy());
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(config, params, getFieldsToCopy());
        return config;
    }

    protected List<String> getFieldsToCopy() {
        return ImmutableList.<String>builder().add(API_KEY, APP_NAME).addAll(getRequiredFiles()).build();
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
        enforceFieldRequiredness(params, errorCollection);
        encryptApiKey(params);
    }

    protected void enforceFieldRequiredness(ActionParametersMap params, ErrorCollection errorCollection) {
        for (String field : getFieldsToCopy()) {
            if (StringUtils.isEmpty(params.getString(field))) {
                errorCollection.addError(field, getI18nBean().getText("com.heroku.errors.emptyField"));
            }
        }
    }

    protected void encryptApiKey(ActionParametersMap params) {
        if (params.containsKey(API_KEY)) {
            try {
                // test if the key is already encrypted
                encryptionService.decrypt(params.getString(API_KEY));
            } catch (EncryptionException e) {
                // otherwise, encrypt it
                params.put(API_KEY, encryptionService.encrypt(params.getString(API_KEY)));
            }
        }
    }

    /** Spring setter */
    public void setEncryptionService(@ComponentImport EncryptionService encryptionService)
    {
        this.encryptionService = encryptionService;
    }
}