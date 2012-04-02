package com.heroku;

public class TarGzDeploymentTask extends AbstractDeploymentTask<TargGzDeploymentTaskConfigurator> {

    public TarGzDeploymentTask() {
        super(new TargGzDeploymentTaskConfigurator());
    }

    public TarGzDeploymentTask(StaticSandbox staticSandbox) {
        super(new TargGzDeploymentTaskConfigurator(), staticSandbox);
    }
}