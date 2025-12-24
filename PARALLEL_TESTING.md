# Parallel Testing Guide

## Overview
Your QA-MCP-Testing project now supports **complete parallel testing** at multiple levels:
1. **Local Parallel** - Multiple threads on one browser
2. **Multi-Browser Parallel** - Different browsers simultaneously
3. **GitHub Actions Parallel** - Configurable thread pool per browser

---

## 🚀 Quick Start

### Enable Parallel Testing Locally
```bash
# Default (sequential)
mvn clean test

# Enable parallel with 4 threads
mvn clean test -Dparallel.enabled=true -Dparallel.threads=4

# Specific browser with parallel
mvn clean test -Dbrowser=firefox -Dparallel.enabled=true -Dparallel.threads=6

# Headless + Parallel
mvn clean test -Dheadless=true -Dparallel.enabled=true -Dparallel.threads=4
```

### Enable Parallel Testing in GitHub Actions
1. Go to **Actions** → **Automated Manual Test Run**
2. Click **Run workflow**
3. Select:
   - Browser: chrome | firefox | edge | all
   - Tags: (optional)
   - Headless: true/false
   - **Parallel: true** ← NEW!
   - **Threads: 4** ← NEW!
4. Click **Run workflow**

---

## 📊 Execution Models

### Model 1: Single Browser, Sequential Tests
```
mvn clean test -Dbrowser=chrome
```
**Use Case**: Local development, quick validation
**Time**: Baseline
**Resources**: 1 browser instance

### Model 2: Single Browser, Parallel Tests
```
mvn clean test -Dbrowser=chrome -Dparallel.enabled=true -Dparallel.threads=4
```
**Use Case**: Local fast testing, CI/CD pipelines
**Time**: ~25% of baseline (4 threads)
**Resources**: 1 browser × 4 threads

### Model 3: Multi-Browser, Parallel (GitHub Actions)
**Workflow Input:**
- Browser: all
- Parallel: true
- Threads: 4

**Execution:**
```
Job 1: Chrome   → 4 threads in parallel
Job 2: Firefox  → 4 threads in parallel
Job 3: Edge     → 4 threads in parallel
```
**Use Case**: Full cross-browser validation
**Time**: ~25% of baseline per browser (parallel overhead)
**Resources**: High (3 browsers × 4 threads each)

---

## ⚙️ Configuration

### Maven Properties

| Property | Default | Options | Purpose |
|----------|---------|---------|---------|
| `parallel.enabled` | `false` | true, false | Enable/disable parallel execution |
| `parallel.threads` | `4` | 1-8+ | Number of parallel threads |
| `browser` | `chrome` | chrome, firefox, edge | Browser selection |
| `headless` | `false` | true, false | Headless mode |

### TestNG Configuration

**File**: `testng.xml`

```xml
<test name="Chrome Tests" parallel="methods" thread-count="2">
    <parameter name="browser" value="chrome"/>
    <classes>
        <class name="runners.CucumberTestRunner"/>
    </classes>
</test>
```

**Parallel Modes:**
- `parallel="false"` - Sequential execution
- `parallel="methods"` - Run test methods in parallel
- `parallel="classes"` - Run test classes in parallel
- `parallel="tests"` - Run test elements in parallel

**Thread Count:**
- `thread-count="2"` - 2 concurrent threads per test
- `thread-count="4"` - 4 concurrent threads per test

---

## 🔧 Implementation Details

### Maven Surefire Configuration
```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>${parallel.enabled}</parallel>
        <threadCount>${parallel.threads}</threadCount>
        <reuseForks>false</reuseForks>
        <argLine>-Xmx1024m -XX:MaxPermSize=256m</argLine>
    </configuration>
</plugin>
```

**Key Settings:**
- `parallel` - Type of parallelism (methods, classes, tests)
- `threadCount` - Number of concurrent threads
- `reuseForks` - Don't reuse JVM forks for stability
- `argLine` - JVM memory settings for parallel execution

### ThreadLocal WebDriver Management
Your `DriverFactory` uses `ThreadLocal` to safely manage drivers:

```java
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

// Each thread gets its own driver instance
driverThreadLocal.set(driver);
// Later...
driverThreadLocal.get();
```

**Why this works for parallel:**
- ✅ Each thread has isolated driver instance
- ✅ No cross-thread driver interference
- ✅ Proper cleanup with `driverThreadLocal.remove()`
- ✅ Memory-efficient

---

## 📈 Performance Metrics

### Expected Speedup

| Threads | Time Reduction | Use Case |
|---------|---|----------|
| 1 (Sequential) | Baseline | Local development |
| 2 | ~45% | Quick feedback |
| 4 | ~65% | Standard CI/CD |
| 6 | ~70% | Heavy CI/CD |
| 8 | ~72% | Max parallel |

**Note:** Speedup plateaus due to:
- Thread scheduling overhead
- Browser resource constraints
- Network I/O overhead

---

## 🎯 Best Practices

### 1. Thread Count Selection
```
Threads = CPU cores ÷ 2
```
- 4-core machine: 2 threads
- 8-core machine: 4 threads
- 16-core machine: 8 threads

**Why divide by 2?** WebDriver is I/O intensive, not CPU intensive.

### 2. Local Testing
```bash
# Start with sequential
mvn clean test

# Then try 2 threads
mvn clean test -Dparallel.enabled=true -Dparallel.threads=2

# Increase if stable
mvn clean test -Dparallel.enabled=true -Dparallel.threads=4
```

### 3. GitHub Actions
```yaml
# Use parallel for all browsers
parallel: "true"
threads: "4"
```

### 4. Memory Management
Parallel execution needs more memory:

```xml
<argLine>-Xmx1024m -XX:MaxPermSize=256m</argLine>
```

Adjust if you see `OutOfMemoryError`:
```xml
<!-- Increase for high parallel count -->
<argLine>-Xmx2048m -XX:MaxPermSize=512m</argLine>
```

### 5. Headless Mode
Always use headless for parallel in CI/CD:

```bash
mvn clean test -Dheadless=true -Dparallel.enabled=true -Dparallel.threads=4
```

**Why:** Headless saves ~30% system resources.

---

## ⚠️ Troubleshooting

### Issue: Tests fail with parallel but pass sequential

**Causes:**
1. Race conditions in test code
2. Shared test data
3. Insufficient timeouts
4. Port conflicts

**Solutions:**
```bash
# Reduce thread count
mvn clean test -Dparallel.enabled=true -Dparallel.threads=2

# Add delays
# In your step definitions, add explicit waits

# Check for shared state
# Ensure each scenario has isolated data
```

### Issue: Out of Memory errors

**Solution:**
```xml
<!-- Increase memory in pom.xml -->
<argLine>-Xmx2048m -XX:MaxPermSize=512m</argLine>
```

### Issue: Port conflicts with multiple drivers

**Ensure:**
```java
// DriverFactory properly isolates each driver
// Each thread should have separate driver instance
```

### Issue: WebDriver session errors

**Solutions:**
```bash
# Disable parallel temporarily
mvn clean test -Dparallel.enabled=false

# Increase thread timeouts
# Adjust sleep durations in scenarios
```

### Issue: Flaky tests under parallel

**Solutions:**
1. Add explicit waits (WebDriverWait)
2. Avoid Thread.sleep()
3. Use explicit synchronization
4. Reduce thread count to 2

---

## 📊 Execution Scenarios

### Scenario 1: Quick Local Check
```bash
mvn clean test -Dbrowser=chrome -Dparallel.enabled=true -Dparallel.threads=2
```
**Time:** ~50% of sequential
**Resources:** Medium
**Use:** Daily development

### Scenario 2: Full Smoke Test
```bash
mvn clean test -Dcucumber.filter.tags="@smoke" -Dparallel.enabled=true -Dparallel.threads=4 -Dheadless=true
```
**Time:** ~25% of sequential
**Resources:** High
**Use:** Before commit

### Scenario 3: Multi-Browser CI/CD (GitHub Actions)
**Inputs:**
- Browser: all
- Headless: true
- Parallel: true
- Threads: 4

**Execution:**
```
3 parallel jobs × 4 threads each = 12 concurrent threads total
```
**Time:** Highly optimized
**Resources:** High (runner resource intensive)

### Scenario 4: Nightly Full Regression
**Inputs:**
- Browser: all
- Headless: true
- Parallel: true
- Threads: 6
- Tags: @regression

**Execution:**
```
3 browsers × 6 threads = 18 threads
Full regression suite in parallel
```

---

## 🔍 Monitoring Parallel Execution

### Check if parallel is enabled
```bash
# Look for this in Maven output:
# [INFO] parallel='true'
# [INFO] threadCount='4'

mvn clean test -Dparallel.enabled=true -Dparallel.threads=4 | grep -i parallel
```

### Monitor in GitHub Actions
1. Go to **Actions** → Job → Check logs
2. Look for:
   ```
   parallel.enabled=true
   parallel.threads=4
   ```

### Check driver initialization timing
Logs show:
```
[INFO] Chrome driver initialized
[INFO] Driver creation took XXms
```
Parallel execution should show multiple drivers initializing.

---

## 🎯 Advanced Configuration

### Custom Thread Pool
```bash
# For high-core machines
mvn clean test -Dparallel.enabled=true -Dparallel.threads=12
```

### Mixed Mode (Sequential + Parallel)
```bash
# Tests in parallel, but browsers sequential
# Requires custom setup (not default)
```

### Data-Driven Parallel
```bash
# Each thread gets different test data
# Ensure ScenarioContext handles isolation
```

---

## 📋 Execution Summary

| Mode | Browser | Threads | Time | Resources | Use Case |
|------|---------|---------|------|-----------|----------|
| Sequential | Single | 1 | Baseline | Low | Development |
| Parallel Local | Single | 4 | ~65% | Medium | Local CI |
| Parallel Multi | All | 4 | ~65% per | High | Full CI/CD |
| Headless Parallel | Single | 6 | ~70% | Medium-High | Fast CI/CD |

---

## 🚀 Getting Started

### Step 1: Enable Parallel Locally
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=2
```

### Step 2: Monitor Output
Check Maven logs for thread initialization messages

### Step 3: Increase Threads Gradually
```bash
-Dparallel.threads=2  # Test first
-Dparallel.threads=4  # Then increase
-Dparallel.threads=6  # If stable
```

### Step 4: Use in GitHub Actions
- Enable parallel checkbox
- Select thread count
- Run workflow

---

## ✅ Verification Checklist

- [ ] Parallel execution enabled locally
- [ ] Tests pass with 2 threads
- [ ] Tests pass with 4 threads
- [ ] No memory errors
- [ ] No flaky failures
- [ ] GitHub Actions parallel configured
- [ ] Artifacts collecting properly

---

## 📚 Related Documentation

- [QUICK_START.md](QUICK_START.md) - Basic commands
- [BROWSER_CONFIGURATION.md](BROWSER_CONFIGURATION.md) - Browser config
- [IMPLEMENTATION_DETAILS.md](IMPLEMENTATION_DETAILS.md) - Technical details

---

## 💡 Pro Tips

✓ Start with 2 threads, increase gradually
✓ Always use headless for parallel CI/CD
✓ Monitor memory usage with high thread counts
✓ Disable parallel if tests become flaky
✓ Use parallel for smoke tests, sequential for edge cases
✓ GitHub Actions "all" + parallel = fastest CI/CD

---

**Last Updated:** 24 December 2025  
**Status:** ✅ Complete  
**Compatibility:** Maven 3.6+, TestNG 7.0+, Java 11+
