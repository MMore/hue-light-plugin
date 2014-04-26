package org.jenkinsci.plugins.hue_light;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.*;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import nl.q42.jue.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;


public class LightNotifier extends Notifier {
    private final String lightId;
    private LightController lightController;


    @DataBoundConstructor
    public LightNotifier(String lightId) {
        this.lightId = lightId;
    }

    public String getLightId() {
        return this.lightId;
    }

    @Override
    /**
     * CJA: Note that old prebuild using Build is deprecated. Now using AbstractBuild parameter.
     */
    public boolean prebuild(AbstractBuild build, BuildListener listener) {
        // does not work in constructor...
        final DescriptorImpl descriptor = this.getDescriptor();
        this.lightController = new LightController(descriptor.getBridgeIp(), descriptor.getBridgeUsername(), listener.getLogger());

        Light light = this.lightController.getLightForId(this.lightId);
        this.lightController.setColor(light, LightColor.BLUE);

        return super.prebuild(build, listener);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        BallColor ballcolor = build.getResult().color;
        Light light = this.lightController.getLightForId(this.lightId);

        // success
        if (BallColor.BLUE == ballcolor)
            this.lightController.setColor(light, LightColor.GREEN);
        // unstable
        else if (BallColor.YELLOW == ballcolor)
            this.lightController.setColor(light, LightColor.YELLOW);
        // error
        else if (BallColor.RED == ballcolor)
            this.lightController.setColor(light, LightColor.RED);

        return true;
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }


    private static final String FORM_KEY_BRIDGE_IP = "bridgeIp";
    private static final String FORM_KEY_BRIDGE_USERNAME = "bridgeUsername";

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String bridgeIp;
        private String bridgeUsername;

        public DescriptorImpl() {
            this.load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Colorize Hue-Light";
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
            if (!formData.containsKey(FORM_KEY_BRIDGE_IP) || !formData.containsKey(FORM_KEY_BRIDGE_USERNAME))
                return false; // keep client on config page

            this.bridgeIp = formData.getString(FORM_KEY_BRIDGE_IP);
            this.bridgeUsername = formData.getString(FORM_KEY_BRIDGE_USERNAME);
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
