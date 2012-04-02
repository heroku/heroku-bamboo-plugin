package com.heroku;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class WarDeploymentTaskConfigurator extends AbstractDeploymentTaskConfigurator {

    @Override
    public String getPipelineName() {
        return "war";
    }

    @Override
    public List<String> getRequiredFiles() {
        return ImmutableList.of("war");
    }
}
