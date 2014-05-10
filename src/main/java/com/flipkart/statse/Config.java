package com.flipkart.statse;

import java.util.Properties;

public class Config {
    private final Properties properties;

    public Config(Properties properties) {
        this.properties = properties;
    }

    public int getInt(String key, int defaultValue) {
        String property = properties.getProperty(key);
        if (property == null) {
            return defaultValue;
        }
        return Integer.parseInt(property);
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
