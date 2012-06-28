package com.heroku;

import junit.framework.TestCase;

/**
 * @author Ryan Brainard
 */
public class HerokuPluginPropertiesTest extends TestCase {

    public void testVersionMergeFieldReplaced() throws Exception {
        assertFalse("Found: " + HerokuPluginProperties.getVersion(), "${project.version}".equals(HerokuPluginProperties.getVersion()));
    }

}
