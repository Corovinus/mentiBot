package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class AppConfig {
    private static final Properties P = new Properties();

    static {
        try (InputStream in = Files.newInputStream(Paths.get("config.properties"))) {
            P.load(in);
        } catch (IOException e) {
            try (InputStream in =
                         AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (in != null) P.load(in);
                else System.err.println("config.properties not found, using defaults");
            } catch (IOException ex) {
                throw new ExceptionInInitializerError("Cannot load config.properties");
            }
        }
    }

    private static String get(String key, String def) {
        return P.getProperty(key, def);
    }

    public static boolean isCssEnabled() {
        return Boolean.parseBoolean(get("http.cssEnabled","false"));
    }
    public static boolean throwOnScriptError() {
        return Boolean.parseBoolean(get("http.throwOnScriptError","false"));
    }
    public static boolean throwOnFailingStatusCode() {
        return Boolean.parseBoolean(get("http.throwOnFailingStatusCode","false"));
    }
    public static boolean isJavascriptEnabled() {
        return Boolean.parseBoolean(get("webdriver.javascriptEnabled","true"));
    }
    public static int getWaitTimeoutSeconds() {
        return Integer.parseInt(get("webdriver.waitTimeoutSeconds","10"));
    }

    public static boolean isMetricsEnabled() {
        return Boolean.parseBoolean(get("metrics.enabled","false"));
    }

}
