package com.heroku.bamboo;

import java.util.List;

/**
 * @author Ryan Brainard
 */
public interface DeploymentPipeline {
    String getPipelineName();
    List<String> getRequiredFiles();
}
