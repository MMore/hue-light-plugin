package org.jenkinsci.plugins.hue_light;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BallColor;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;
import nl.q42.jue.Light;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;


public class LightNotifier extends Notifier {
    private static final String FORM_KEY_BRIDGE_IP = "bridgeIp";
    private static final String FORM_KEY_BRIDGE_USERNAME = "bridgeUsername";
    private static final String FORM_KEY_BLUE = "colorBlue";
    private static final String FORM_KEY_GREEN = "colorGreen";
    private static final String FORM_KEY_YELLOW = "colorYellow";
    private static final String FORM_KEY_RED = "colorRed";
    private static final String FORM_KEY_SATURATION = "saturation";
    private static final String FORM_KEY_BRIGHTNESS = "brightness";
    private final HashSet<String> lightId;
    private final String preBuild;
    private final String goodBuild;
    private final String unstableBuild;
    private final String badBuild;
    private LightController lightController;

    @DataBoundConstructor
    public LightNotifier(String lightId, String preBuild, String goodBuild, String unstableBuild, String badBuild) {
    	this.lightId = new HashSet<String>();
    	if(lightId != null) {
    		String[] lightIds = lightId.split(",");
    		for(String id : lightIds) {
    			this.lightId.add(id.trim());
    		}
    	}
        
        this.preBuild = preBuild;
        this.goodBuild = goodBuild;
        this.unstableBuild = unstableBuild;
        this.badBuild = badBuild;
    }
	
	public String getLightId() {
		String lid = "";
		if(this.lightId != null && this.lightId.size() > 0) {
    		for(String id : this.lightId) {
    			lid += id + ",";
    		}
			lid = lid.substring(0, lid.length() - 1);
    	}
		return lid;
	}

    public String getPreBuild() {
        return this.preBuild;
    }

    public String getGoodBuild() {
        return this.goodBuild;
    }

    public String getUnstableBuild() {
        return this.unstableBuild;
    }

    public String getBadBuild() {
        return this.badBuild;
    }

    @Override
    /**
     * CJA: Note that old prebuild using Build is deprecated. Now using AbstractBuild parameter.
     */
    public boolean prebuild(AbstractBuild build, BuildListener listener) {
        // does not work in constructor...
        final DescriptorImpl descriptor = this.getDescriptor();

        this.lightController = new LightController(descriptor, listener.getLogger());
        
        for(String id : this.lightId) {
	        Light light = this.lightController.getLightForId(id);
	        this.lightController.setPulseBreathe(light, "Build Starting", ConfigColorToHue(this.preBuild));
        }
        return super.prebuild(build, listener);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        // Allowable values from build results:
        //
        // RED              - Bad Build
        // RED_ANIME
        // YELLOW           - Unstable Build
        // YELLOW_ANIME
        // BLUE             - Good Build
        // BLUE_ANIME
        // GREY
        // GREY_ANIME
        // DISABLED
        // DISABLED_ANIME
        // ABORTED
        // ABORTED_ANIME
        // NOTBUILT
        // NOTBUILT_ANIME

        BallColor ballcolor = build.getResult().color;
        
        for(String id : this.lightId) {
	        Light light = this.lightController.getLightForId(id);
	
	        switch (ballcolor) {
	            case RED:
	                this.lightController.setColor(light, "Bad Build", ConfigColorToHue(this.badBuild));
	                break;
	            case YELLOW:
	                this.lightController.setColor(light, "Unstable Build", ConfigColorToHue(this.unstableBuild));
	                break;
	            case BLUE:
	                this.lightController.setColor(light, "Good Build", ConfigColorToHue(this.goodBuild));
	                break;
	        }
        }
        return true;
    }

    /**
     * Note that we support Blue, Green, Yellow and Red as named colors. Anything else, we presume it's
     * an integer. If we can't decode it, we return 0, which is actually red, but hey, we have to return
     * something.
     *
     * @param color The color we want to turn into a numeric hue
     * @return The numeric hue
     */
    private Integer ConfigColorToHue(String color) {

        if (color.equalsIgnoreCase("blue")) {

            return Integer.parseInt(this.getDescriptor().getBlue());

        } else if (color.equalsIgnoreCase("green")) {

            return Integer.parseInt(this.getDescriptor().getGreen());

        } else if (color.equalsIgnoreCase("yellow")) {

            return Integer.parseInt(this.getDescriptor().getYellow());

        } else if (color.equalsIgnoreCase("red")) {

            return Integer.parseInt(this.getDescriptor().getRed());

        } else {

            if (DescriptorImpl.isInteger(color))
                return Integer.parseInt(color);
            else
                return 0;
        }
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String bridgeIp;
        private String bridgeUsername;
        private String blue;
        private String green;
        private String yellow;
        private String red;
        private String saturation;
        private String brightness;

        public DescriptorImpl() {
            this.load();
        }

        public static boolean isInteger(String s) {
            try {
                Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Colorize Hue-Light";
        }

        /**
         * Validates that some IP address was entered for the bridge. A hostname is also valid (do not change variable
         * name because this would be a breaking change).
         *
         * @param value The bridge IP address
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckBridgeIp(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the IP or hostname of the bridge");

            return FormValidation.ok();
        }

        /**
         * Validates that some username was entered. This could really be anything.
         *
         * @param value The user name
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckBridgeUsername(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the username");

            return FormValidation.ok();
        }

        /**
         * Validates that some light ID was entered and that it's a non-negative integer
         *
         * @param value The ID of the light to be used
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckLightId(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the ID(s) of the light(s) separated by commas");
            
            String[] lightIds = value.split(",");
            for(String id : lightIds) {
                id = id.trim();
    	        if (!isInteger(id))
	                return FormValidation.error("Please enter positive integers only");
	            if (Integer.parseInt(id) < 0)
	                return FormValidation.error("Please enter non-negative numbers only");
            }
            return FormValidation.ok();
        }

        /**
         * Validates that some value was entered for blue and that it's a non-negative integer
         *
         * @param value The hue value for blue
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckBlue(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the hue value for blue");
            if (!isInteger(value))
                return FormValidation.error("Please enter a number");
            if (Integer.parseInt(value) < 0)
                return FormValidation.error("Please enter a non-negative number");

            return FormValidation.ok();
        }

        /**
         * Validates that some value was entered for green and that it's a non-negative integer
         *
         * @param value The hue value for green
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckGreen(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the hue value for green");
            if (!this.isInteger(value))
                return FormValidation.error("Please enter a number");
            if (Integer.parseInt(value) < 0)
                return FormValidation.error("Please enter a non-negative number");

            return FormValidation.ok();
        }

        /**
         * Validates that some value was entered for yellow and that it's a non-negative integer
         *
         * @param value The hue value for yellow
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckYellow(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the hue value for yellow");
            if (!isInteger(value))
                return FormValidation.error("Please enter a number");
            if (Integer.parseInt(value) < 0)
                return FormValidation.error("Please enter a non-negative number");

            return FormValidation.ok();
        }

        /**
         * Validates that some value was entered for red and that it's a non-negative integer
         *
         * @param value The hue value for red
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckRed(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the hue value for red");
            if (!isInteger(value))
                return FormValidation.error("Please enter a number");
            if (Integer.parseInt(value) < 0)
                return FormValidation.error("Please enter a non-negative number");

            return FormValidation.ok();
        }

        /**
         * Validates that some value was entered for saturation and that it's [0..255]
         *
         * @param value The hue value for saturation
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckSaturation(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the hue value for saturation");
            if (!isInteger(value))
                return FormValidation.error("Please enter a number");
            if (Integer.parseInt(value) < 0 || Integer.parseInt(value) > 255) 
                return FormValidation.error("Please enter number in range [0...255]");

            return FormValidation.ok();
        }

        /**
         * Validates that some value was entered for brightness and that it's [1..255]
         *
         * @param value The hue value for brightness
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheckBrightness(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set the hue value for saturation");
            if (!isInteger(value))
                return FormValidation.error("Please enter a number");
            if (Integer.parseInt(value) < 1 || Integer.parseInt(value) > 255)
                return FormValidation.error("Please enter number in range [1...255]");

            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            if (!formData.containsKey(FORM_KEY_BRIDGE_IP) || !formData.containsKey(FORM_KEY_BRIDGE_USERNAME))
                return false; // keep client on config page

            this.bridgeIp = formData.getString(FORM_KEY_BRIDGE_IP);
            this.bridgeUsername = formData.getString(FORM_KEY_BRIDGE_USERNAME);
            this.blue = formData.getString(FORM_KEY_BLUE);
            this.green = formData.getString(FORM_KEY_GREEN);
            this.yellow = formData.getString(FORM_KEY_YELLOW);
            this.red = formData.getString(FORM_KEY_RED);
            this.saturation = formData.getString(FORM_KEY_SATURATION);
            this.brightness = formData.getString(FORM_KEY_BRIGHTNESS);

            this.save();

            return super.configure(req, formData);
        }

        public String getBridgeIp() {
            return this.bridgeIp;
        }

        public String getBridgeUsername() {
            return this.bridgeUsername;
        }

        public String getBlue() {
            return this.blue;
        }

        public String getGreen() {
            return this.green;
        }

        public String getYellow() {
            return this.yellow;
        }

        public String getRed() {
            return this.red;
        }

        public String getSaturation() {
            return this.saturation;
        }

        public String getBrightness() {
            return this.brightness;
        }
    }
}
