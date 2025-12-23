package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class SeleniumUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SeleniumUtils.class);
    private WebDriver driver;
    private WebDriverWait wait;

    public SeleniumUtils(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void navigateTo(String url) {
        try {
            driver.navigate().to(url);
            logger.info("Navigated to: {}", url);
        } catch (Exception e) {
            logger.error("Failed to navigate to {}", url, e);
            throw e;
        }
    }

    public void click(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            logger.info("Clicked on element: {}", locator);
        } catch (Exception e) {
            logger.error("Failed to click on element: {}", locator, e);
            throw e;
        }
    }

    public void sendKeys(By locator, String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            element.sendKeys(text);
            logger.info("Entered text '{}' in element: {}", text, locator);
        } catch (Exception e) {
            logger.error("Failed to send keys to element: {}", locator, e);
            throw e;
        }
    }

    public String getText(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            String text = element.getText();
            logger.info("Retrieved text from element: {} -> {}", locator, text);
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element: {}", locator, e);
            throw e;
        }
    }

    public boolean isElementPresent(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            logger.warn("Element not found: {}", locator);
            return false;
        }
    }

    public void waitForElement(By locator, int seconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(seconds));
            customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            logger.info("Element found within {} seconds: {}", seconds, locator);
        } catch (Exception e) {
            logger.error("Timeout waiting for element: {}", locator, e);
            throw e;
        }
    }

    public void selectDropdownByVisibleText(By locator, String visibleText) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            Select select = new Select(element);
            select.selectByVisibleText(visibleText);
            logger.info("Selected dropdown option by visible text: {}", visibleText);
        } catch (Exception e) {
            logger.error("Failed to select dropdown option: {}", visibleText, e);
            throw e;
        }
    }

    public void selectDropdownByValue(By locator, String value) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            Select select = new Select(element);
            select.selectByValue(value);
            logger.info("Selected dropdown option by value: {}", value);
        } catch (Exception e) {
            logger.error("Failed to select dropdown by value: {}", value, e);
            throw e;
        }
    }

    public List<WebElement> findElements(By locator) {
        try {
            return driver.findElements(locator);
        } catch (Exception e) {
            logger.error("Failed to find elements: {}", locator, e);
            throw e;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}
