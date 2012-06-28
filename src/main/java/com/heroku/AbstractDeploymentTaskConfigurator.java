package com.heroku;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.security.StringEncrypter;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.user.BambooAuthenticationContext;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.google.common.collect.ImmutableList;
import com.opensymphony.xwork.TextProvider;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractDeploymentTaskConfigurator extends AbstractTaskConfigurator implements DeploymentPipeline {

    private static final String API_KEY = "apiKey";
    private static final String APP_NAME = "appName";
    private static final String DUMMY_API_KEY = "0000000000000000000000000000000";

    private TextProvider textProvider;

    protected List<String> getFieldsToCopy() {
        return ImmutableList.<String>builder().add(API_KEY, APP_NAME).addAll(getRequiredFiles()).build();
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        taskConfiguratorHelper.populateTaskConfigMapWithActionParameters(config, params, getFieldsToCopy());
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, getFieldsToCopy());
        replaceApiKeyWithDummy(context);
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForView(context, taskDefinition);
        taskConfiguratorHelper.populateContextWithConfiguration(context, taskDefinition, getFieldsToCopy());
        replaceApiKeyWithDummy(context);
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
        
        for (String field : getFieldsToCopy()) {
            if (StringUtils.isEmpty(params.getString(field))) {
                errorCollection.addError(field, "Required");
            }
        }

        if (params.containsKey(API_KEY)) {
            if (!(params.get(API_KEY) instanceof String[])) { throw new RuntimeException("Unexpected API_KEY format"); }
            final String[] unencryptedApiKeyArray = (String[]) params.get(API_KEY);
            if (unencryptedApiKeyArray.length != 1) { throw new RuntimeException("Unexpected API_KEY array length"); }

            if (!DUMMY_API_KEY.equals(unencryptedApiKeyArray[0])) {
                params.put(API_KEY, new String[]{new StringEncrypter().encrypt(unencryptedApiKeyArray[0])});
            }
        }
    }

    private void replaceApiKeyWithDummy(Map<String, Object> context) {
        if (context.containsKey(API_KEY)) {
            context.put(API_KEY, DUMMY_API_KEY);
        }
    }

    public void setTextProvider(final TextProvider textProvider) {
        this.textProvider = textProvider;
    }
}
