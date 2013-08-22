package org.jenkinsci.plugins.hue_light;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;


public class LightBuildWrapper extends BuildWrapper {

    private final String lightId;

    @DataBoundConstructor
    public LightBuildWrapper(String lightId) {
        this.lightId = lightId;
    }

    public String getLightId() {
        return this.lightId;
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        return super.setUp(build, launcher, listener);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }



    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        private String bridgeIp;
        private String bridgeUsername;

        public DescriptorImpl() {
            this.load();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> abstractProject) {
            ///TODO check sth. useful here
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Control Hue-Light";
        }

        public FormValidation doCheckBridgeIp(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the IP of the bridge");
            //TODO add better validation

            return FormValidation.ok();
        }

        public FormValidation doCheckBridgeUsername(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the username");
            //TODO add better validation

            return FormValidation.ok();
        }

        public FormValidation doCheckLightId(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the ID of the light");
            if (!this.isInteger(value))
                return FormValidation.error("Please enter a number");
            //TODO add better validation

            return FormValidation.ok();
        }

        private boolean isInteger(String s) {
            try {
                Integer.parseInt(s);
            }
            catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            this.bridgeIp = formData.getString("bridgeIp");
            this.bridgeUsername = formData.getString("bridgeUsername");
            this.save();
            return super.configure(req, formData);
        }

        public String getBridgeIp() {
            return this.bridgeIp;
        }

        public String getBridgeUsername() {
            return this.bridgeUsername;
        }
    }
}
