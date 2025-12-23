package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ScenarioContext;
import utils.SeleniumUtils;

public class GoogleSearchSteps {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleSearchSteps.class);
    private WebDriver driver;
    private SeleniumUtils seleniumUtils;
    private ScenarioContext scenarioContext;

    // Locators
    private By searchBox = By.name("q");
    private By searchButton = By.name("btnK");
    private By resultStats = By.id("result-stats");

    public GoogleSearchSteps(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
        this.driver = scenarioContext.getDriver();
        this.seleniumUtils = new SeleniumUtils(driver);
    }

    @Given("User navigates to Google home page")
    public void userNavigatesToGoogle() {
        logger.info("Navigating to Google");
        seleniumUtils.navigateTo("https://www.google.com");
        
        String pageTitle = seleniumUtils.getPageTitle();
        Assert.assertEquals(pageTitle, "Google", "Page title mismatch");
        logger.info("Google home page loaded successfully");
    }

    @When("User searches for {string}")
    public void userSearchesFor(String searchTerm) {
        logger.info("Searching for: {}", searchTerm);
        
        // Click on search box
        seleniumUtils.click(searchBox);
        
        // Type search term
        seleniumUtils.sendKeys(searchBox, searchTerm);
        
        // Press Enter or click search button
        seleniumUtils.click(searchButton);
        
        logger.info("Search initiated for: {}", searchTerm);
    }

    @Then("Search results should be displayed")
    public void verifySearchResults() {
        logger.info("Verifying search results are displayed");
        
        // Wait for result stats to be visible
        seleniumUtils.waitForElement(resultStats, 10);
        
        // Verify results are displayed
        boolean resultsDisplayed = seleniumUtils.isElementPresent(resultStats);
        Assert.assertTrue(resultsDisplayed, "Search results not displayed");
        
        logger.info("Search results verified successfully");
    }

    @Then("Results should contain {string}")
    public void verifyResultsContain(String keyword) {
        logger.info("Verifying results contain keyword: {}", keyword);
        
        String pageSource = driver.getPageSource();
        Assert.assertTrue(pageSource.contains(keyword), 
            "Search results do not contain: " + keyword);
        
        logger.info("Results contain the expected keyword");
    }
}
