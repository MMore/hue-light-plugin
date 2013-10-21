package org.jenkinsci.plugins.hue_light;


public enum LightColor {
    RED(0, "red"), YELLOW(12750, "yellow"), GREEN(25717, "green"), BLUE(46920, "blue");

    private final int hue;
    private final String name;

    private LightColor(int hue, String name) {
        this.hue = hue;
        this.name = name;
    }

    /**
     * Returns the hue-value for the color.
     * @return hue
     */
    public int getHue() {
        return this.hue;
    }

    /**
     * Returns a human readable name of the color.
     * @return name
     */
    public String getName() {
        return this.name;
    }
}
