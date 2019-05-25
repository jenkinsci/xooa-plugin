package io.jenkins.plugins.xooa;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;


/**
 * Example of Jenkins global configuration.
 */
@Extension
public class GlobConfig extends GlobalConfiguration {

    /** @return the singleton instance */
    public static GlobConfig get() {
        return GlobalConfiguration.all().get(GlobConfig.class);
    }

    private String deploymentToken;

    public GlobConfig() {
        load();
    }

    /** @return the currently configured deploymentToken, if any */
    public String getDeploymentToken() {
        return deploymentToken;
    }

    /**
     * Together with {@link #getDeploymentToken}, binds to entry in {@code config.jelly}.
     * @param deploymentToken the new value of this field
     */
    @DataBoundSetter
    public void setDeploymentToken(String deploymentToken) {
        this.deploymentToken = deploymentToken;
        save();
    }

    public FormValidation doCheckDeploymentToken(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please provide deployment token");
        }
        return FormValidation.ok();
    }

}
