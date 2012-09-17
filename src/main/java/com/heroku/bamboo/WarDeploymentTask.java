package com.heroku.bamboo;

public class WarDeploymentTask extends AbstractDeploymentTask<WarDeploymentTaskConfigurator> {

    public WarDeploymentTask() {
        super(new WarDeploymentTaskConfigurator());
    }

    public WarDeploymentTask(StaticSandbox staticSandbox) {
        super(new WarDeploymentTaskConfigurator(), staticSandbox);
    }
}