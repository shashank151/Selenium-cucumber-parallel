package utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class BrowserConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(BrowserConfig.class);
    private static final String CONFIG_FILE = "/config/browser.config.json";
    private JsonObject configJson;

    public BrowserConfig() {
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream inputStream = getClass().getResourceAsStream(CONFIG_FILE);
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            
            Gson gson = new Gson();
            configJson = gson.fromJson(reader, JsonObject.class);
            logger.info("Browser configuration loaded successfully");
            
        } catch (IOException | NullPointerException e) {
            logger.error("Failed to load browser configuration from {}", CONFIG_FILE, e);
            configJson = new JsonObject();
        }
    }

    public JsonObject getBrowserOptions(String browser) {
        if (configJson.has("browsers") && configJson.getAsJsonObject("browsers").has(browser)) {
            return configJson.getAsJsonObject("browsers").getAsJsonObject(browser);
        }
        logger.warn("Browser configuration not found for: {}, returning empty config", browser);
        return new JsonObject();
    }

    public List<String> getBrowserArguments(String browser) {
        JsonObject browserConfig = getBrowserOptions(browser);
        if (browserConfig.has("arguments")) {
            Gson gson = new Gson();
            return gson.fromJson(browserConfig.getAsJsonArray("arguments"), 
                new com.google.gson.reflect.TypeToken<List<String>>(){}.getType());
        }
        return List.of();
    }

    public boolean isHeadless(String browser) {
        JsonObject browserConfig = getBrowserOptions(browser);
        if (browserConfig.has("headless")) {
            return browserConfig.get("headless").getAsBoolean();
        }
        return false;
    }

    public String getWindowSize(String browser) {
        JsonObject browserConfig = getBrowserOptions(browser);
        if (browserConfig.has("windowSize")) {
            return browserConfig.get("windowSize").getAsString();
        }
        return "1920,1080";
    }

    public int getImplicitWait() {
        if (configJson.has("implicitWait")) {
            return configJson.get("implicitWait").getAsInt();
        }
        return 10;
    }

    public int getExplicitWait() {
        if (configJson.has("explicitWait")) {
            return configJson.get("explicitWait").getAsInt();
        }
        return 15;
    }

    public int getPageLoadTimeout() {
        if (configJson.has("pageLoadTimeout")) {
            return configJson.get("pageLoadTimeout").getAsInt();
        }
        return 30;
    }
}
