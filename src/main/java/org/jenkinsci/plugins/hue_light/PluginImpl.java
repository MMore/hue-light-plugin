package org.jenkinsci.plugins.hue_light;

import hudson.Plugin;

import java.util.logging.Logger;


public class PluginImpl extends Plugin {
    private final static Logger LOG = Logger.getLogger(PluginImpl.class.getName());

    public void start() throws Exception {
        super.start();
        LOG.info("starting hue-light-plugin");
    }
}
