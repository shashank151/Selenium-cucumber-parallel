# Implementation Details & Architecture

## System Design

### Overview
The project now implements a flexible multi-browser testing framework that allows:
1. Running on any single browser (Chrome, Firefox, Edge)
2. Running on all browsers in parallel (via GitHub Actions matrix)
3. Running with or without headless mode
4. Configuring browser-specific options centrally

---

## Core Components

### 1. BrowserConfig.java
**Purpose**: Load and manage browser configuration from JSON file

```java
public class BrowserConfig {
    private JsonObject configJson;
    
    // Methods:
    - getBrowserOptions(String browser) → JsonObject
    - getBrowserArguments(String browser) → List<String>
    - isHeadless(String browser) → boolean
    - getWindowSize(String browser) → String
    - getImplicitWait() → int
    - getExplicitWait() → int
    - getPageLoadTimeout() → int
}
```

**Features**:
- Loads `browser.config.json` from classpath on initialization
- Provides thread-safe access to browser configuration
- Handles missing configurations gracefully (returns defaults)
- Uses GSON for JSON parsing

### 2. Enhanced DriverFactory.java
**Purpose**: Create and manage WebDriver instances

```java
public class DriverFactory {
    // Key Methods:
    - initializeDriver(String browser) → WebDriver
    - getDriver() → WebDriver
    - quitDriver() → void
    - closeDriver() → void
    
    // Private Methods:
    - initializeChromeDriver() → WebDriver
    - initializeFirefoxDriver() → WebDriver
    - initializeEdgeDriver() → WebDriver
    - isHeadless() → boolean
}
```

**Key Changes**:
1. **BrowserConfig Integration**: Loads configuration at static initialization
2. **Headless Mode**: Detects via `System.getProperty("headless")`
3. **Dynamic Options**: Applies browser-specific options from JSON
4. **Timeout Management**: Sets implicit, explicit, and page load timeouts
5. **Window Size**: Sets window size based on configuration (or maximizes if headless)

### 3. ScenarioContext.java
**Purpose**: Store and manage WebDriver instance and browser selection

```java
public class ScenarioContext {
    private WebDriver driver;
    private String browser;
    
    // Constructor reads: System.getProperty("browser", "chrome")
}
```

**Key**:
- Reads browser from system property at construction time
- Default value: "chrome"
- Provides getter/setter for driver and browser

### 4. CucumberHooks.java
**Purpose**: Setup and teardown browser for each scenario

```java
@Before
public void setupBrowser() {
    String browser = scenarioContext.getBrowser();
    WebDriver driver = DriverFactory.initializeDriver(browser);
    scenarioContext.setDriver(driver);
}

@After
public void tearDownBrowser() {
    DriverFactory.quitDriver();
    scenarioContext.setDriver(null);
}
```

---

## Configuration Flow

### Configuration File Structure
```
src/test/resources/config/browser.config.json
└── browsers
    ├── chrome
    │   ├── driverPath
    │   ├── headless
    │   ├── windowSize
    │   └── arguments[]
    ├── firefox
    │   ├── driverPath
    │   ├── headless
    │   ├── windowSize
    │   └── arguments[]
    └── edge
        ├── driverPath
        ├── headless
        ├── windowSize
        └── arguments[]
└── implicitWait
└── explicitWait
└── pageLoadTimeout
```

### Runtime Configuration Loading
```
Startup
  ↓
BrowserConfig() constructor called
  ↓
loadConfig() reads browser.config.json
  ↓
JsonObject parsed and stored
  ↓
DriverFactory uses BrowserConfig on demand
```

---

## Maven Property Hierarchy

### pom.xml Properties
```xml
<properties>
    <browser>chrome</browser>           <!-- Default browser -->
    <headless>false</headless>          <!-- Default: with UI -->
    <cucumber.filter.tags/>             <!-- Optional cucumber tags -->
    <maven.surefire.version/>           <!-- Surefire test runner -->
</properties>
```

### Maven Surefire Configuration
```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <browser>${browser}</browser>
            <headless>${headless}</headless>
            <cucumber.filter.tags>${cucumber.filter.tags}</cucumber.filter.tags>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

These properties are converted to system properties available to the JVM:
- `System.getProperty("browser")` → user provided or "chrome"
- `System.getProperty("headless")` → user provided or "false"
- `System.getProperty("cucumber.filter.tags")` → user provided or null

---

## GitHub Actions Workflow

### Job 1: determine-browsers
**Purpose**: Dynamically generate browser matrix based on user input

```yaml
determine-browsers:
  runs-on: ubuntu-latest
  outputs:
    browsers: ${{ steps.set-browsers.outputs.browsers }}
  steps:
    - name: Determine browsers to test
      id: set-browsers
      run: |
        if [ "${{ inputs.browser }}" == "all" ]; then
          echo "browsers=[\"chrome\",\"firefox\",\"edge\"]" >> $GITHUB_OUTPUT
        else
          echo "browsers=[\"${{ inputs.browser }}\"]" >> $GITHUB_OUTPUT
        fi
```

**Logic**:
- If user selects "all": Output JSON array `["chrome","firefox","edge"]`
- If user selects single browser: Output JSON array `["chrome"]` (or firefox/edge)

### Job 2: test
**Purpose**: Run tests using matrix strategy

```yaml
test:
  needs: determine-browsers
  strategy:
    matrix:
      browser: ${{ fromJson(needs.determine-browsers.outputs.browsers) }}
  steps:
    - Run Maven Tests with:
        -Dbrowser=${{ matrix.browser }}
        -Dheadless=${{ inputs.headless }}
```

**Execution**:
1. Single browser: Creates 1 job instance
2. "all" option: Creates 3 parallel job instances (chrome, firefox, edge)

---

## Headless Mode Implementation

### Detection
```java
private static boolean isHeadless() {
    String headless = System.getProperty("headless", "false");
    return "true".equalsIgnoreCase(headless);
}
```

### Chrome Headless
```java
if (isHeadless()) {
    options.addArguments("--headless=new");  // Chrome 96+ syntax
}
```

### Firefox Headless
```java
if (isHeadless()) {
    options.addArguments("--headless");      // Standard Firefox syntax
}
```

### Edge Headless
```java
if (isHeadless()) {
    options.addArguments("--headless=new");  // Similar to Chrome
}
```

---

## Error Handling

### BrowserConfig Loading Failures
```java
try {
    // Load config
} catch (IOException | NullPointerException e) {
    logger.error("Failed to load browser configuration", e);
    configJson = new JsonObject();  // Empty fallback
}
```

### Missing Browser Configuration
```java
if (configJson.has("browsers") && 
    configJson.getAsJsonObject("browsers").has(browser)) {
    return config;
}
logger.warn("Browser configuration not found: {}", browser);
return new JsonObject();  // Empty fallback
```

### Driver Initialization Failure
```java
try {
    // Initialize driver
} catch (Exception e) {
    logger.error("Failed to initialize driver for browser: {}", browser, e);
    // Returns null, tests will fail with clear error
}
```

---

## Threading & Thread Safety

### ThreadLocal WebDriver
```java
private static final ThreadLocal<WebDriver> driverThreadLocal 
    = new ThreadLocal<>();
```

**Usage**:
- `driverThreadLocal.set(driver)` - Store driver for current thread
- `driverThreadLocal.get()` - Retrieve driver for current thread
- `driverThreadLocal.remove()` - Clean up after test

**Benefits**:
- Thread-safe when running parallel tests
- Each thread has its own driver instance
- No cross-thread interference

### Static BrowserConfig
```java
private static BrowserConfig browserConfig;

static {
    browserConfig = new BrowserConfig();
}
```

**Design**:
- Loaded once at class initialization
- Reused across all thread instances
- Configuration is read-only (thread-safe)

---

## Performance Considerations

### Configuration Loading
- Loaded once at startup (static initialization block)
- Reused across all tests
- No runtime performance impact

### Multi-Browser Execution
- Via GitHub Actions matrix: Parallel execution
- Via local Maven: Sequential execution (run multiple times)

### WebDriver Management
- Driver created in @Before hook (per scenario)
- Driver quit in @After hook (per scenario)
- No driver leaks with proper ThreadLocal cleanup

---

## Extensibility

### Adding a New Browser
1. Add configuration in `browser.config.json`
2. Add case in DriverFactory switch statement
3. Create `initializeNewBrowserDriver()` method
4. Update GitHub workflow options
5. Update documentation

Example (adding Safari):
```json
"safari": {
  "driverPath": "",
  "headless": false,
  "windowSize": "1920,1080",
  "arguments": []
}
```

```java
case "safari":
    driver = initializeSafariDriver();
    break;

private static WebDriver initializeSafariDriver() {
    SafariOptions options = new SafariOptions();
    return new SafariDriver(options);
}
```

---

## Testing Strategy

### Local Testing
1. Single browser tests for development
2. Multi-browser manual runs before commit
3. Headless mode for CI/CD validation

### GitHub Actions Testing
1. Manual workflow dispatch for ad-hoc testing
2. Options for browser selection, tags, headless
3. Parallel matrix execution for "all" option
4. Artifact collection on failure

### Regression Prevention
1. Default browser (Chrome) prevents breakage
2. Configuration file centralizes browser options
3. System properties allow override when needed
4. Clear error logging for troubleshooting

---

## Backward Compatibility

### Breaking Changes: None
- Existing tests work without modification
- Default browser is Chrome (existing behavior)
- New features are additive (headless, config loading)

### Migration Path for Existing Projects
1. Copy BrowserConfig.java to utils folder
2. Update DriverFactory.java
3. Update pom.xml (add headless property)
4. Update GitHub workflow (optional but recommended)

---

## Monitoring & Logging

### Log Levels
- **INFO**: Driver initialization, browser selection
- **WARN**: Unrecognized browsers, missing configuration
- **ERROR**: Failed to load configuration, driver crashes
- **DEBUG**: Step execution details (in hooks)

### Key Log Messages
```
"Chrome driver initialized"
"Browser initialization completed for: chrome"
"Starting scenario with browser: chrome"
"Failed to initialize driver for browser: firefox"
"Browser configuration not found for: safari"
```

---

## Future Enhancements

### Planned Improvements
1. **Cloud Browsers**: BrowserStack, Sauce Labs integration
2. **Performance Metrics**: Timing per browser
3. **Screenshots/Video**: On failure recording
4. **Parallel Local**: Multi-threaded local execution
5. **Mobile Browsers**: Appium integration
6. **Safari Support**: macOS runner support

### Potential Issues & Solutions
| Issue | Solution |
|-------|----------|
| Driver not found | WebDriverManager auto-downloads, check network |
| Headless doesn't work | Verify `-Dheadless=true` passed correctly |
| Wrong browser selected | Check system property logging |
| Tests timeout | Increase timeouts in browser.config.json |
| Out of memory | Limit parallel jobs in GitHub Actions |

---

**Last Updated**: 24 December 2025  
**Compatibility**: Maven 3.6+, Java 11+, Selenium 4.15+
