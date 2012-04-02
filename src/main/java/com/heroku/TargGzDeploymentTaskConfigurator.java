package com.heroku;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class TargGzDeploymentTaskConfigurator extends AbstractDeploymentTaskConfigurator {

    @Override
    public String getPipelineName() {
        return "targz";
    }

    @Override
    public List<String> getRequiredFiles() {
        return ImmutableList.of("targz", "procfile");
    }
}
