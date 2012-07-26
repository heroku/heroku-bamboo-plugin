package com.heroku;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.security.StringEncrypter;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.Collections;
import java.util.List;

import static com.heroku.AbstractDeploymentTaskConfigurator.API_KEY;

/**
 * @author Ryan Brainard
 */
public class ArtifactDeploymentTasksTest extends MockObjectTestCase {

    private static final String PLAIN_TEXT_KEY = "PLAIN_TEXT";
    private static final String ENCRYPTED_KEY = new StringEncrypter().encrypt(PLAIN_TEXT_KEY);

    public void testApiKeyEncryption_Blank() throws Exception {
        final Mock mockParams = new Mock(ActionParametersMap.class);
        mockParams.expects(atLeastOnce()).method("containsKey").will(returnValue(false));
        mockParams.expects(never()).method("getString");
        mockParams.expects(never()).method("put");

        new TestingDeploymentTaskConfigurator().encryptApiKey((ActionParametersMap) mockParams.proxy());
        mockParams.verify();
    }

    public void testApiKeyEncryption_New() throws Exception {
        final Mock mockParams = new Mock(ActionParametersMap.class);
        mockParams.expects(atLeastOnce()).method("containsKey").will(returnValue(true));
        mockParams.expects(atLeastOnce()).method("getString").will(returnValue(PLAIN_TEXT_KEY));
        mockParams.expects(once()).method("put").with(eq(API_KEY), eq(ENCRYPTED_KEY));

        new TestingDeploymentTaskConfigurator().encryptApiKey((ActionParametersMap) mockParams.proxy());
        mockParams.verify();
    }

    public void testApiKeyEncryption_Existing() throws Exception {
        final Mock mockParams = new Mock(ActionParametersMap.class);
        mockParams.expects(atLeastOnce()).method("containsKey").will(returnValue(true));
        mockParams.expects(atLeastOnce()).method("getString").will(returnValue(ENCRYPTED_KEY));
        mockParams.expects(never()).method("put");

        new TestingDeploymentTaskConfigurator().encryptApiKey((ActionParametersMap) mockParams.proxy());
        mockParams.verify();
    }

    private static class TestingDeploymentTaskConfigurator extends AbstractDeploymentTaskConfigurator {

        @Override
        public String getPipelineName() {
            return "TEST";
        }

        @Override
        public List<String> getRequiredFiles() {
            return Collections.emptyList();
        }
    }

}
