package org.jenkinsci.plugins.hue_light;


import nl.q42.jue.HueBridge;
import nl.q42.jue.Light;
import nl.q42.jue.StateUpdate;
import java.io.PrintStream;

public class LightController {
    private final PrintStream logger;
    private final String bridgeIp;
    private final String bridgeUsername;
    private HueBridge hueBridge;

    /**
     * Connect with a hue bridge.
     * @param bridgeIp ip of the hue bridge
     * @param bridgeUsername username of the hue bridge
     * @param logger logger stream
     */
    public LightController(String bridgeIp, String bridgeUsername, PrintStream logger) {
        this.bridgeIp = bridgeIp;
        this.bridgeUsername = bridgeUsername;
        this.logger = logger;

        try {
            this.hueBridge = new HueBridge(this.bridgeIp, this.bridgeUsername);
        } catch (Exception e) {
            this.logError(e.getMessage());
        }
    }

    /**
     * Returns a light object for a specific id.
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
                    else {
                        this.logError("no light with id " + id + " found");
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
     * @param light light object that should be manipulated
     * @param color desired color
     * @return true if color update was successful, otherwise false
     */
    public boolean setColor(Light light, LightColor color) {
        if (null == this.hueBridge || null == light || null == color)
            return false;

        StateUpdate stateUpdate = new StateUpdate().turnOn().setBrightness(255).setSat(255).setHue(color.getHue());

        try {
            this.hueBridge.setLightState(light, stateUpdate);
            this.logInfo("set color of light " + light.getId() + " (" + light.getName() + ")" + " to " + color.getName());
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
