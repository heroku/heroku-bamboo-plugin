package com.heroku;

import java.util.List;

/**
 * @author Ryan Brainard
 */
public interface DeploymentPipeline {
    String getPipelineName();
    List<String> getRequiredFiles();
}
