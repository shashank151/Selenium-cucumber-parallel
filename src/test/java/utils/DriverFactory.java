package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

public class DriverFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static BrowserConfig browserConfig;

    static {
        browserConfig = new BrowserConfig();
    }

    public static WebDriver initializeDriver(String browser) {
        WebDriver driver = null;
        
        try {
            switch (browser.toLowerCase()) {
                case "chrome":
                    driver = initializeChromeDriver();
                    logger.info("Chrome driver initialized");
                    break;
                case "firefox":
                    driver = initializeFirefoxDriver();
                    logger.info("Firefox driver initialized");
                    break;
                case "edge":
                    driver = initializeEdgeDriver();
                    logger.info("Edge driver initialized");
                    break;
                default:
                    logger.warn("Browser {} not recognized, defaulting to Chrome", browser);
                    driver = initializeChromeDriver();
            }
            
            // Set timeouts from configuration
            driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(browserConfig.getImplicitWait()));
            driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(browserConfig.getPageLoadTimeout()));
            
            // Maximize or set window size
            String windowSize = browserConfig.getWindowSize(browser);
            if (!isHeadless()) {
                driver.manage().window().maximize();
            } else {
                try {
                    String[] dimensions = windowSize.split(",");
                    org.openqa.selenium.Dimension size = new org.openqa.selenium.Dimension(
                        Integer.parseInt(dimensions[0]), 
                        Integer.parseInt(dimensions[1])
                    );
                    driver.manage().window().setSize(size);
                } catch (Exception e) {
                    logger.warn("Could not set window size: {}", e.getMessage());
                }
            }
            
            driverThreadLocal.set(driver);
            
        } catch (Exception e) {
            logger.error("Failed to initialize driver for browser: {}", browser, e);
        }
        
        return driver;
    }

    private static WebDriver initializeChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        
        // Load options from config
        JsonObject chromeConfig = browserConfig.getBrowserOptions("chrome");
        
        // Add headless mode if enabled
        if (isHeadless()) {
            options.addArguments("--headless=new");
        }
        
        // Add arguments from configuration
        if (chromeConfig.has("arguments")) {
            chromeConfig.getAsJsonArray("arguments").forEach(arg -> 
                options.addArguments(arg.getAsString())
            );
        } else {
            // Default arguments
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--start-maximized");
        }
        
        return new ChromeDriver(options);
    }

    private static WebDriver initializeFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        
        // Load options from config
        JsonObject firefoxConfig = browserConfig.getBrowserOptions("firefox");
        
        // Add headless mode if enabled
        if (isHeadless()) {
            options.addArguments("--headless");
        }
        
        // Add arguments from configuration
        if (firefoxConfig.has("arguments")) {
            firefoxConfig.getAsJsonArray("arguments").forEach(arg -> 
                options.addArguments(arg.getAsString())
            );
        } else {
            // Default arguments
            options.addArguments("-no-remote");
        }
        
        return new FirefoxDriver(options);
    }

    private static WebDriver initializeEdgeDriver() {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        
        // Load options from config
        JsonObject edgeConfig = browserConfig.getBrowserOptions("edge");
        
        // Add headless mode if enabled
        if (isHeadless()) {
            options.addArguments("--headless=new");
        }
        
        // Add arguments from configuration
        if (edgeConfig.has("arguments")) {
            edgeConfig.getAsJsonArray("arguments").forEach(arg -> 
                options.addArguments(arg.getAsString())
            );
        } else {
            // Default arguments
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }
        
        return new EdgeDriver(options);
    }

    private static boolean isHeadless() {
        String headless = System.getProperty("headless", "false");
        return "true".equalsIgnoreCase(headless);
    }


    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
            logger.info("Driver quit successfully");
        }
    }

    public static void closeDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.close();
            logger.info("Driver closed");
        }
    }
}

