package utils;

import org.openqa.selenium.WebDriver;

public class ScenarioContext {
    
    private WebDriver driver;
    private String browser;

    public ScenarioContext() {
        // Determine browser from parameter passed by TestNG
        String browserParam = System.getProperty("browser", "chrome");
        this.browser = browserParam.toLowerCase();
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }
}
