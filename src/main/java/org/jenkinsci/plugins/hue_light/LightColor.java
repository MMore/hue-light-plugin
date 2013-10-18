package org.jenkinsci.plugins.hue_light;


public enum LightColor {
    RED(0, "red"), YELLOW(12750, "yellow"), GREEN(36210, "green"), BLUE(46920, "blue");

    private final int hue;
    private final String name;

    private LightColor(int hue, String name) {
        this.hue = hue;
        this.name = name;
    }

    public int getHue() {
        return this.hue;
    }

    public String getName() {
        return this.name;
    }
}
