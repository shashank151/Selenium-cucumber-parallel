# Browser Configuration & Multi-Browser Testing Guide

## Overview
This project now supports running tests across multiple browsers (Chrome, Firefox, Edge) with flexible configuration options. You can run tests locally or through GitHub Actions with browser selection and headless mode support.

## Key Features

### 1. **Default Browser: Chrome**
- Chrome is the default browser for all test runs
- If no browser is specified, tests automatically run on Chrome

### 2. **Multi-Browser Support**
- **Chrome** - Fully supported
- **Firefox** - Fully supported  
- **Edge** - Fully supported
- **All** - Run tests sequentially on all three browsers

### 3. **Headless Mode Support**
- Run tests in headless mode for CI/CD environments
- Configurable via GitHub Actions or Maven command line
- Default: `false` (runs with UI)

### 4. **Configuration Management**
- Centralized browser configuration in `src/test/resources/config/browser.config.json`
- Browser-specific options (arguments, window size, timeouts)
- Easy to add new browsers or modify existing configurations

---

## Local Testing

### Run Tests on Default Browser (Chrome)
```bash
mvn clean test
```

### Run Tests on Specific Browser
```bash
# Chrome
mvn clean test -Dbrowser=chrome

# Firefox
mvn clean test -Dbrowser=firefox

# Edge
mvn clean test -Dbrowser=edge
```

### Run Tests in Headless Mode
```bash
# Default browser in headless mode
mvn clean test -Dheadless=true

# Specific browser in headless mode
mvn clean test -Dbrowser=firefox -Dheadless=true
```

### Run with Cucumber Tags
```bash
mvn clean test -Dcucumber.filter.tags="@smoke"
```

### Run on All Browsers (Local)
```bash
# Chrome
mvn clean test -Dbrowser=chrome

# Firefox
mvn clean test -Dbrowser=firefox

# Edge
mvn clean test -Dbrowser=edge
```

---

## GitHub Actions Workflow

The workflow is configured in `.github/workflows/automated_run.yml` and provides a user-friendly interface for test execution.

### Workflow Triggers
The workflow runs via **Manual Dispatch** (workflow_dispatch) with configurable inputs.

### Available Inputs

#### 1. **Browser Selection** (Required, Default: chrome)
- `chrome` - Run on Chrome only
- `firefox` - Run on Firefox only
- `edge` - Run on Edge only
- `all` - Run on all three browsers (Chrome, Firefox, Edge)

#### 2. **Cucumber Tags** (Optional)
- Specify Cucumber tags to filter test scenarios (e.g., `@smoke`, `@regression`)
- Leave empty to run all scenarios

#### 3. **Headless Mode** (Optional, Default: false)
- `true` - Run in headless mode
- `false` - Run with UI

### How to Trigger Workflow

1. Go to **GitHub Repository** → **Actions** tab
2. Select **"Automated Manual Test Run"** workflow
3. Click **"Run workflow"** button
4. Fill in the inputs:
   - **Browser**: Select the desired browser or "all"
   - **Tags**: (Optional) Enter Cucumber tags
   - **Headless**: Select true/false
5. Click **"Run workflow"**

### Workflow Examples

**Example 1: Run Chrome with @smoke tests**
- Browser: `chrome`
- Tags: `@smoke`
- Headless: `false`

**Example 2: Run all browsers in headless mode**
- Browser: `all`
- Tags: (leave empty)
- Headless: `true`

**Example 3: Run Firefox with specific tags in headless**
- Browser: `firefox`
- Tags: `@regression`
- Headless: `true`

---

## Browser Configuration File

File: `src/test/resources/config/browser.config.json`

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
      "arguments": [
        "-no-remote"
      ]
    },
    "edge": {
      "driverPath": "",
      "headless": false,
      "windowSize": "1920,1080",
      "arguments": []
    }
  },
  "implicitWait": 10,
  "explicitWait": 15,
  "pageLoadTimeout": 30
}
```

### Configuration Options
- **driverPath**: Path to driver executable (auto-managed by WebDriverManager if empty)
- **headless**: Default headless setting (can be overridden by system property)
- **windowSize**: Browser window dimensions (format: "width,height")
- **arguments**: Browser-specific command-line arguments
- **implicitWait**: Default implicit wait in seconds
- **explicitWait**: Default explicit wait in seconds
- **pageLoadTimeout**: Page load timeout in seconds

---

## Project Structure Changes

### New Files Added
1. **BrowserConfig.java** (`src/test/java/utils/`)
   - Utility class to load and parse browser configuration
   - Provides methods to get browser-specific options

### Modified Files
1. **DriverFactory.java**
   - Enhanced to use BrowserConfig for browser options
   - Added headless mode support
   - Improved timeout and window size management

2. **pom.xml**
   - Added `<headless>false</headless>` property
   - Updated Maven Surefire plugin to pass headless property

3. **.github/workflows/automated_run.yml**
   - Added `determine-browsers` job to dynamically set browser matrix
   - Added headless input parameter
   - Improved dynamic matrix generation based on user selection

---

## System Architecture

### Browser Initialization Flow
1. **System Property Check**: `System.getProperty("browser", "chrome")`
2. **ScenarioContext**: Reads browser from system properties
3. **CucumberHooks.setupBrowser()**: Initializes driver via DriverFactory
4. **DriverFactory**: 
   - Loads BrowserConfig
   - Creates WebDriver with browser-specific options
   - Applies timeouts and headless settings

### Headless Mode Flow
1. **Maven/CLI**: `-Dheadless=true` passed to system properties
2. **DriverFactory.isHeadless()**: Reads system property
3. **Browser Initialization**: Adds `--headless` or `--headless=new` argument
4. **WebDriver**: Launches without GUI

---

## Maven Properties

### Available System Properties
| Property | Default | Options | Usage |
|----------|---------|---------|-------|
| `browser` | `chrome` | chrome, firefox, edge | `-Dbrowser=chrome` |
| `headless` | `false` | true, false | `-Dheadless=true` |
| `cucumber.filter.tags` | (empty) | Any valid Cucumber tag | `-Dcucumber.filter.tags="@smoke"` |

---

## Troubleshooting

### Tests run on wrong browser
- Verify system property is passed correctly
- Check browser name in system property (case-insensitive)
- Check ScenarioContext.java initialization

### Headless mode not working
- Ensure `-Dheadless=true` is passed to Maven
- Check browser log output for headless argument confirmation
- Some browsers may need different headless syntax

### Browser driver not found
- WebDriverManager auto-downloads drivers
- Ensure internet connectivity for first run
- Check logs for WebDriverManager errors

### GitHub workflow not running
- Verify workflow file syntax (`.github/workflows/automated_run.yml`)
- Check repository Actions permissions
- Ensure runner is available (self-hosted runner must be running)

---

## Best Practices

1. **Use specific browser for feature testing**: Chrome is generally fastest for local development
2. **Use "all" in CI/CD**: Ensures cross-browser compatibility
3. **Use headless for CI/CD**: Faster execution and better resource utilization
4. **Tag your scenarios**: Use Cucumber tags to organize tests by type (@smoke, @regression, etc.)
5. **Update browser.config.json**: As new browser versions are released, update arguments accordingly

---

## Future Enhancements

Potential improvements:
- Add Safari support (macOS runner)
- Add parallel execution within browsers
- Add screenshot/video recording on failure
- Add performance metrics per browser
- Integration with cloud-based browser services (BrowserStack, Sauce Labs)

---

## Questions or Issues?

For issues with:
- **Local testing**: Check Maven output and system properties
- **GitHub Actions**: Check workflow run logs in Actions tab
- **Browser configuration**: Review browser.config.json and DriverFactory.java
