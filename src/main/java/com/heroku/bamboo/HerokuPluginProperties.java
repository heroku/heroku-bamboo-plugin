package com.heroku.bamboo;

import org.apache.commons.collections.map.UnmodifiableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Ryan Brainard
 */
class HerokuPluginProperties {

    private HerokuPluginProperties() {}

    private static final Map<String, String> projectProperties = loadProjectProperties();

    /**
     * @return Unmodifiable map of project properties
     */
    private static Map<String, String> loadProjectProperties() {
        Properties projectProperties = new Properties();
        InputStream propStream = null;
        try {
            propStream = HerokuPluginProperties.class.getClassLoader().getResourceAsStream("heroku-bamboo-plugin.properties");
            projectProperties.load(propStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (propStream != null) {
                try {
                    propStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        //noinspection unchecked
        return UnmodifiableMap.decorate(projectProperties);
    }

    static String getUserAgent() {
        return "heroku-bamboo-plugin/" + getVersion();
    }

    /**
     * @return version of this heroku-jenkins-plugin project from pom.xml
     */
    static String getVersion() {
        return projectProperties.get("heroku-bamboo-plugin.version");
    }
}
