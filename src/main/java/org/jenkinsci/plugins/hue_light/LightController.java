package org.jenkinsci.plugins.hue_light;


import nl.q42.jue.HueBridge;
import nl.q42.jue.Light;
import nl.q42.jue.State;
import nl.q42.jue.StateUpdate;

import java.io.PrintStream;

public class LightController {
    private final PrintStream logger;
    private HueBridge hueBridge;
    private int saturation;
    private int brightness;

    /**
     * Connect with a hue bridge.
     *
     * @param descriptor The descriptor for this application
     * @param logger     logger stream
     */
    public LightController(LightNotifier.DescriptorImpl descriptor, PrintStream logger) {
        this.logger = logger;
        this.saturation = Integer.parseInt(descriptor.getSaturation());
        this.brightness = Integer.parseInt(descriptor.getBrightness());

        try {
            this.hueBridge = new HueBridge(descriptor.getBridgeIp(), descriptor.getBridgeUsername());
        } catch (Exception e) {
            this.logError(e.getMessage());
        }
    }

    /**
     * Returns a light object for a specific id.
     *
     * @param id id of a light
     * @return light object if light found, otherwise null
     */
    public Light getLightForId(String id) {
        if (null != this.hueBridge) {
            try {
                for (Light light : this.hueBridge.getLights()) {
                    if (light.getId().equals(id)) {
                        return light;
                    }
                }
            } catch (Exception e) {
                this.logError(e.getMessage());
            }
        }

        return null;
    }

    /**
     * Sets the color of a light.
     *
     * @param light light object that should be manipulated
     * @param logName The name to use for logging this color state change
     * @param hue The hue of the desired color
     * @return true if color update was successful, otherwise false
     */
    public boolean setColor(Light light, String logName, int hue) {

        if (null == this.hueBridge || null == light)
            return false;

        StateUpdate stateUpdate = new StateUpdate().turnOn().setBrightness(this.brightness).setSat(this.saturation).setHue(hue).setEffect(State.Effect.NONE).setAlert((State.AlertMode.NONE));

        try {
            this.hueBridge.setLightState(light, stateUpdate);
            this.logInfo("set color of light " + light.getId() + " (" + light.getName() + ")" + " to " + logName + " (" + hue + ")");
        } catch (Exception e) {
            this.logError(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean setPulseColor(Light light, String logName, int hue) {

        if (null == this.hueBridge || null == light)
            return false;

        StateUpdate stateUpdate = new StateUpdate().turnOn().setBrightness(this.brightness).setSat(this.saturation).setHue(hue).setEffect(State.Effect.COLORLOOP).setAlert(State.AlertMode.NONE);

        try {
            this.hueBridge.setLightState(light, stateUpdate);
            this.logInfo("set pulse color of light " + light.getId() + " (" + light.getName() + ")" + " to " + logName + " (" + hue + ")");
        } catch (Exception e) {
            this.logError(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean setPulseBreathe(Light light, String logName, int hue) {

        if (null == this.hueBridge || null == light)
            return false;

        StateUpdate stateUpdate = new StateUpdate().turnOn().setBrightness(this.brightness).setSat(this.saturation).setHue(hue).setEffect(State.Effect.NONE).setAlert(State.AlertMode.LSELECT);

        try {
            this.hueBridge.setLightState(light, stateUpdate);
            this.logInfo("set breathe color of light " + light.getId() + " (" + light.getName() + ")" + " to " + logName + " (" + hue + ")");
        } catch (Exception e) {
            this.logError(e.getMessage());
            return false;
        }

        return true;
    }

    private void logError(String msg) {
        this.logger.println("hue-light-error: " + msg);
    }

    private void logInfo(String msg) {
        this.logger.println("hue-light: " + msg);
    }
}
