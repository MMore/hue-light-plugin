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

    public boolean setColor(Light light, LightColor color) {
        if (null == this.hueBridge || null == light || null == color)
            return false;

        StateUpdate stateUpdate = new StateUpdate().turnOn().setBrightness(255).setHue(color.getHue());

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
