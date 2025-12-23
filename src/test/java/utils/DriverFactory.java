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

public class DriverFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

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
            
            driver.manage().window().maximize();
            driverThreadLocal.set(driver);
            
        } catch (Exception e) {
            logger.error("Failed to initialize driver for browser: {}", browser, e);
        }
        
        return driver;
    }

    private static WebDriver initializeChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");
        return new ChromeDriver(options);
    }

    private static WebDriver initializeFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-no-remote");
        return new FirefoxDriver(options);
    }

    private static WebDriver initializeEdgeDriver() {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return new EdgeDriver(options);
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
