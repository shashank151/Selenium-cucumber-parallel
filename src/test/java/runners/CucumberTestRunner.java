package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.BeforeTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"hooks", "stepdefinitions"},
    plugin = {
        "pretty",
        "json:target/cucumber-json-reports/cucumber.json",
        "html:target/cucumber-reports/index.html",
        "junit:target/cucumber-reports/cucumber.xml"
    },
    monochrome = true,
    publish = false
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {
    
    private static final Logger logger = LoggerFactory.getLogger(CucumberTestRunner.class);

    @Parameters({"browser"})
    @BeforeTest
    public void setup(String browser) {
        System.setProperty("browser", browser);
        logger.info("Browser set to: {}", browser);
    }

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
