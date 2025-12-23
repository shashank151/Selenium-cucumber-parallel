package hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.AfterStep;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DriverFactory;
import utils.ScenarioContext;

public class CucumberHooks {
    
    private static final Logger logger = LoggerFactory.getLogger(CucumberHooks.class);
    private ScenarioContext scenarioContext;

    public CucumberHooks(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Before
    public void setupBrowser() {
        String browser = scenarioContext.getBrowser();
        logger.info("Starting scenario with browser: {}", browser);
        
        WebDriver driver = DriverFactory.initializeDriver(browser);
        scenarioContext.setDriver(driver);
        
        logger.info("Browser initialization completed for: {}", browser);
    }

    @After
    public void tearDownBrowser() {
        logger.info("Closing browser after scenario");
        DriverFactory.quitDriver();
        scenarioContext.setDriver(null);
    }

    @BeforeStep
    public void beforeStep() {
        logger.debug("Executing step");
    }

    @AfterStep
    public void afterStep() {
        logger.debug("Step execution completed");
    }
}
