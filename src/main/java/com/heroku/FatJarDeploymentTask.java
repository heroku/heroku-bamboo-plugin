package com.heroku;

public class FatJarDeploymentTask extends AbstractDeploymentTask<FatJarDeploymentTaskConfigurator> {

    public FatJarDeploymentTask() {
        super(new FatJarDeploymentTaskConfigurator());
    }

    public FatJarDeploymentTask(StaticSandbox staticSandbox) {
        super(new FatJarDeploymentTaskConfigurator(), staticSandbox);
    }
}