# Selenium Java Cucumber TestNG Parallel Execution Project

A complete BDD automation framework using Selenium WebDriver, Cucumber, and TestNG with support for parallel test execution across multiple browsers.

## 📋 Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [Test Reports](#test-reports)
- [Project Architecture](#project-architecture)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

## ✨ Features

- **Multi-Browser Support**: Run tests on Chrome, Firefox, and Edge browsers
- **Parallel Execution**: Execute tests in parallel on multiple browsers simultaneously
- **BDD Framework**: Cucumber for writing human-readable test scenarios
- **TestNG Integration**: Powerful testing framework with advanced features
- **Thread-Safe**: ThreadLocal implementation for browser driver management
- **WebDriverManager**: Automatic browser driver download and management
- **Comprehensive Logging**: SLF4J and Logback for detailed test logging
- **JSON Configuration**: Browser configuration via JSON file
- **Cucumber Reports**: Beautiful HTML reports with test results
- **Page Object Ready**: Easy to implement Page Object Model pattern

## 📦 Prerequisites

- **Java**: JDK 11 or higher
- **Maven**: 3.6.0 or higher
- **Git**: For version control (optional)
- **Browsers**: Chrome and Firefox (or any supported browser)

### Installation

1. **Install Java**
   ```bash
   java -version  # Verify Java installation
   ```

2. **Install Maven**
   ```bash
   mvn -version  # Verify Maven installation
   ```

## 📂 Project Structure

```
selenium-cucumber-testng-parallel/
├── pom.xml                                    # Maven configuration
├── testng.xml                                # TestNG suite configuration
├── README.md                                 # This file
├── src/
│   └── test/
│       ├── java/
│       │   ├── hooks/
│       │   │   └── CucumberHooks.java       # Browser setup/teardown
│       │   ├── runners/
│       │   │   └── CucumberTestRunner.java  # Cucumber test runner
│       │   ├── stepdefinitions/
│       │   │   └── GoogleSearchSteps.java   # Step implementations
│       │   └── utils/
│       │       ├── DriverFactory.java       # Browser driver factory
│       │       ├── ScenarioContext.java     # Test context manager
│       │       └── SeleniumUtils.java       # Selenium helper methods
│       └── resources/
│           ├── features/
│           │   └── GoogleSearch.feature     # Sample feature file
│           ├── config/
│           │   └── browser.config.json      # Browser configuration
│           └── cucumber.properties          # Cucumber configuration
└── target/                                   # Build output
```

## 🔧 Installation & Setup

### 1. Clone/Download the Project

```bash
# Option 1: If using Git
git clone <repository-url>
cd selenium-cucumber-testng-parallel

# Option 2: Extract provided ZIP file
unzip selenium-cucumber-testng-parallel.zip
cd selenium-cucumber-testng-parallel
```

### 2. Install Dependencies

```bash
mvn clean install
```

This will download all required dependencies (Selenium, Cucumber, TestNG, WebDriverManager, etc.)

### 3. Verify Installation

```bash
mvn -v
mvn clean validate
```

## ⚙️ Configuration

### Browser Configuration (browser.config.json)

Located at: `src/test/resources/config/browser.config.json`

```json
{
  "browsers": {
    "chrome": {
      "driverPath": "",
      "headless": false,
      "windowSize": "1920,1080",
      "arguments": [
        "--no-sandbox",
        "--disable-dev-shm-usage",
        "--start-maximized"
      ]
    },
    "firefox": {
      "driverPath": "",
      "headless": false,
      "windowSize": "1920,1080",
      "arguments": ["-no-remote"]
    }
  },
  "implicitWait": 10,
  "explicitWait": 15,
  "pageLoadTimeout": 30
}
```

**Key Settings:**
- `headless`: Set to `true` for headless mode, `false` for visible browser
- `arguments`: Browser-specific launch arguments
- `implicitWait`: Implicit wait duration in seconds
- `explicitWait`: Explicit/WebDriverWait duration in seconds

### TestNG Configuration (testng.xml)

Controls parallel execution:

```xml
<suite name="Parallel Browser Test Suite" parallel="tests" thread-count="4">
    <test name="Chrome Tests" parallel="methods" thread-count="2">
        <parameter name="browser" value="chrome"/>
        ...
    </test>
    <test name="Firefox Tests" parallel="methods" thread-count="2">
        <parameter name="browser" value="firefox"/>
        ...
    </test>
</suite>
```

**Parallel Execution Levels:**
- `tests`: Run multiple tests in parallel
- `methods`: Run multiple methods within a test in parallel
- `classes`: Run multiple classes in parallel
- `instances`: Run multiple instances in parallel

### Cucumber Configuration (cucumber.properties)

Located at: `src/test/resources/cucumber.properties`

```properties
cucumber.publish.quiet=true
cucumber.plugin=json:target/cucumber-json-reports/cucumber.json
```

## 🚀 Running Tests

### Run All Tests (Parallel on All Browsers)

```bash
mvn clean test
```

This will execute tests in parallel on both Chrome and Firefox as configured in `testng.xml`.

### Run Tests on Specific Browser

```bash
# Chrome only
mvn clean test -Dbrowser=chrome

# Firefox only
mvn clean test -Dbrowser=firefox
```

### Run Specific Feature File

```bash
mvn clean test -Dcucumber.options="src/test/resources/features/GoogleSearch.feature"
```

### Run with Headless Mode

Edit `browser.config.json` and set `"headless": true` for all browsers:

```json
"chrome": {
  "headless": true,
  ...
}
```

### Run with Custom Thread Count

Edit `testng.xml` to modify `thread-count`:

```xml
<suite name="..." parallel="tests" thread-count="8">
```

Increase `thread-count` to run more tests in parallel.

### Run from IDE (IntelliJ IDEA / Eclipse)

1. Right-click on `testng.xml`
2. Select "Run → testng.xml"
3. Or right-click on test class and select "Run"

## 📊 Test Reports

### Cucumber Reports

After test execution, reports are generated at:

```
target/cucumber-reports/index.html
```

Open this file in a browser to view detailed test results with:
- Pass/Fail status
- Execution time
- Screenshots (if added)
- Step-by-step details

### TestNG Reports

Generated at:

```
target/surefire-reports/
```

## 🏗️ Project Architecture

### Thread-Safe Browser Management

The project uses `ThreadLocal` to manage browser drivers safely:

```java
// In DriverFactory.java
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

public static WebDriver getDriver() {
    return driverThreadLocal.get();
}
```

This ensures each thread has its own isolated driver instance for parallel execution.

### Cucumber Hooks (Setup & Teardown)

Implemented in `CucumberHooks.java`:

```java
@Before
public void setupBrowser() {
    // Initialize browser before each scenario
}

@After
public void tearDownBrowser() {
    // Close browser after each scenario
}
```

### Step Definitions

Implement Gherkin steps from feature files:

```java
@Given("User navigates to Google home page")
public void userNavigatesToGoogle() {
    // Step implementation
}
```

## 📝 Writing New Tests

### 1. Create Feature File

Create in `src/test/resources/features/`:

```gherkin
Feature: Login Functionality

  Scenario: Valid user login
    Given User navigates to login page
    When User enters username "user@example.com"
    And User enters password "password123"
    And User clicks login button
    Then User should be logged in successfully
```

### 2. Create Step Definitions

Create in `src/test/java/stepdefinitions/`:

```java
package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ScenarioContext;
import utils.SeleniumUtils;

public class LoginSteps {
    private WebDriver driver;
    private SeleniumUtils seleniumUtils;

    public LoginSteps(ScenarioContext scenarioContext) {
        this.driver = scenarioContext.getDriver();
        this.seleniumUtils = new SeleniumUtils(driver);
    }

    @Given("User navigates to login page")
    public void navigateToLoginPage() {
        seleniumUtils.navigateTo("https://example.com/login");
    }

    @When("User enters username {string}")
    public void enterUsername(String username) {
        seleniumUtils.sendKeys(By.id("username"), username);
    }

    @When("User enters password {string}")
    public void enterPassword(String password) {
        seleniumUtils.sendKeys(By.id("password"), password);
    }

    @When("User clicks login button")
    public void clickLoginButton() {
        seleniumUtils.click(By.id("loginBtn"));
    }

    @Then("User should be logged in successfully")
    public void verifyLogin() {
        String currentUrl = seleniumUtils.getCurrentUrl();
        assert currentUrl.contains("dashboard");
    }
}
```

### 3. Run New Tests

```bash
mvn clean test
```

## 🎯 Best Practices

1. **Locator Strategy**
   - Use ID selectors when available
   - Use CSS selectors for complex scenarios
   - Avoid XPath when possible (slower)
   - Keep locators in constants or page object classes

2. **Waits**
   - Prefer explicit waits over implicit waits
   - Use `WebDriverWait` for dynamic elements
   - Set appropriate timeout values

3. **Test Data**
   - Externalize test data from test code
   - Use parameterization for data-driven tests
   - Keep test data minimal and isolated

4. **Assertions**
   - Use TestNG assertions (`Assert.*`)
   - Meaningful assertion messages
   - Validate critical functionality

5. **Logging**
   - Log important test steps
   - Use appropriate log levels (INFO, WARN, ERROR)
   - Helps with debugging failed tests

6. **Page Object Model (POM)**
   - Create page classes for web pages
   - Encapsulate element locators
   - Reuse page methods across tests

Example POM:

```java
public class GoogleSearchPage {
    private WebDriver driver;
    private SeleniumUtils utils;
    
    private By searchBox = By.name("q");
    private By searchButton = By.name("btnK");
    private By resultStats = By.id("result-stats");

    public GoogleSearchPage(WebDriver driver) {
        this.driver = driver;
        this.utils = new SeleniumUtils(driver);
    }

    public void searchFor(String term) {
        utils.click(searchBox);
        utils.sendKeys(searchBox, term);
        utils.click(searchButton);
    }

    public boolean areResultsDisplayed() {
        return utils.isElementPresent(resultStats);
    }
}
```

## 🐛 Troubleshooting

### Issue: Tests not running in parallel

**Solution:** Verify `testng.xml` has `parallel="tests"` and appropriate `thread-count`.

### Issue: Driver not initialized

**Solution:** Check that `CucumberHooks.java` `@Before` method is being called. Verify hooks are in the glue path in `CucumberOptions`.

### Issue: Browser not found

**Solution:** Ensure browser is installed or check WebDriverManager logs for download errors.

### Issue: StaleElementReferenceException

**Solution:** Re-locate element within the step or use explicit waits before interaction.

### Issue: Test hangs/times out

**Solution:** 
- Reduce explicit wait duration
- Check browser compatibility
- Verify internet connection for remote element loading

### Issue: Reports not generated

**Solution:** Verify plugin configuration in `pom.xml` and check `target/` directory permissions.

## 📚 Additional Resources

- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [TestNG Documentation](https://testng.org/doc/)
- [WebDriverManager](https://github.com/bonigarcia/webdrivermanager)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is open source and available under the MIT License.

---

**Happy Testing! 🎉**

For issues or questions, please open an issue in the repository.
