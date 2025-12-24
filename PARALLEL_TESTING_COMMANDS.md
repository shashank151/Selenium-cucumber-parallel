# 🚀 Parallel Testing - Command Reference

## Quick Commands

### Sequential Testing (Default - No Changes)
```bash
# Basic test run (sequential)
mvn clean test

# Chrome (sequential)
mvn clean test -Dbrowser=chrome

# Firefox (sequential)
mvn clean test -Dbrowser=firefox

# With tags (sequential)
mvn clean test -Dcucumber.filter.tags="@smoke"
```

---

## Local Parallel Testing

### Enable Parallel - 2 Threads (Safe Starting Point)
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=2
```

### Enable Parallel - 4 Threads (Recommended)
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=4
```

### Enable Parallel - 6 Threads (High Performance)
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=6
```

### Browser Specific + Parallel
```bash
# Firefox + parallel
mvn clean test -Dbrowser=firefox -Dparallel.enabled=true -Dparallel.threads=4

# Edge + parallel
mvn clean test -Dbrowser=edge -Dparallel.enabled=true -Dparallel.threads=4
```

### Headless + Parallel (CI/CD)
```bash
mvn clean test -Dheadless=true -Dparallel.enabled=true -Dparallel.threads=4
```

### Tags + Parallel
```bash
# Smoke tests with parallel
mvn clean test -Dcucumber.filter.tags="@smoke" -Dparallel.enabled=true -Dparallel.threads=4

# Regression tests with parallel
mvn clean test -Dcucumber.filter.tags="@regression" -Dparallel.enabled=true -Dparallel.threads=6
```

### Everything Combined
```bash
mvn clean test \
  -Dbrowser=chrome \
  -Dheadless=true \
  -Dcucumber.filter.tags="@smoke" \
  -Dparallel.enabled=true \
  -Dparallel.threads=4
```

---

## GitHub Actions Parallel

### Step-by-Step
1. Go to **Actions** tab
2. Click **"Automated Manual Test Run"**
3. Click **"Run workflow"**
4. Fill in:
   - **Browser**: chrome | firefox | edge | **all**
   - **Tags**: (optional) @smoke
   - **Headless**: true | false
   - **Parallel**: **true** ← Enable parallel
   - **Threads**: 2 | 4 | 6 | **8** ← Select thread count
5. Click **"Run workflow"**

### Recommended GitHub Actions Configurations

**Fast Smoke Test (All Browsers)**
- Browser: all
- Headless: true
- Parallel: true
- Threads: 4

**Full Regression (All Browsers)**
- Browser: all
- Headless: true
- Parallel: true
- Threads: 6

**Single Browser Fast Test**
- Browser: chrome
- Headless: true
- Parallel: true
- Threads: 4

---

## Property Reference

### Maven Properties (System)
```
-Dparallel.enabled=true      Enable parallel mode (default: false)
-Dparallel.threads=4         Number of threads (default: 4)
-Dbrowser=chrome             Browser selection (default: chrome)
-Dheadless=true              Headless mode (default: false)
-Dcucumber.filter.tags="@smoke"   Cucumber tags
```

### GitHub Actions Inputs
```
parallel: "true"             Enable parallel (default: false)
threads: "4"                 Thread count: 2, 4, 6, 8 (default: 4)
browser: "chrome"            Browser: chrome, firefox, edge, all
headless: "true"             Headless: true, false (default: false)
tags: "@smoke"               Cucumber tags (optional)
```

---

## Common Scenarios

### Scenario 1: Local Development Check (Fast)
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=2
```
**Time**: ~55% of sequential
**Use**: Daily local development
**Resources**: Low

### Scenario 2: Before Commit (Complete)
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=4
```
**Time**: ~35% of sequential
**Use**: Before pushing to GitHub
**Resources**: Medium

### Scenario 3: Full Suite Locally (Optimized)
```bash
mvn clean test \
  -Dheadless=true \
  -Dparallel.enabled=true \
  -Dparallel.threads=6
```
**Time**: ~30% of sequential
**Use**: Full regression locally
**Resources**: Medium-High

### Scenario 4: GitHub Actions Multi-Browser
GitHub Actions UI:
- Browser: all
- Headless: true
- Parallel: true
- Threads: 4

**Time**: ~35-40% vs sequential single browser
**Use**: Final CI/CD validation
**Coverage**: All 3 browsers × 4 threads each

### Scenario 5: Quick Smoke Test
```bash
mvn clean test \
  -Dcucumber.filter.tags="@smoke" \
  -Dparallel.enabled=true \
  -Dparallel.threads=2
```
**Time**: Very fast
**Use**: Quick feedback before full test
**Resources**: Low

### Scenario 6: Specific Browser Testing
```bash
mvn clean test \
  -Dbrowser=firefox \
  -Dparallel.enabled=true \
  -Dparallel.threads=4
```
**Time**: ~35% of sequential single browser
**Use**: Debug specific browser issues
**Resources**: Medium

---

## Performance Expectations

### Single Browser Parallel
| Threads | Estimated Time | Speed Up |
|---------|---|---|
| 1 (Sequential) | 100% | Baseline |
| 2 | ~55% | 45% faster |
| 4 | ~35% | 65% faster |
| 6 | ~30% | 70% faster |

### Multi-Browser (GitHub Actions)
| Config | Estimated Time | Speed Up |
|---|---|---|
| Sequential (1 browser) | 100% | Baseline |
| Parallel all (4 threads) | ~35-40% | 60-65% faster |
| Parallel all (6 threads) | ~30% | 70% faster |

---

## Thread Count Guide

### Safe for Everyone
- **2 threads**: Safe starting point
- **4 threads**: Recommended default
- **6 threads**: If tests are stable

### Based on CPU
```
2-core machine:   1-2 threads
4-core machine:   2-4 threads (recommended: 2)
8-core machine:   4-6 threads (recommended: 4)
16-core machine:  8+ threads (recommended: 6)
```

### CI/CD Environment
- **GitHub Actions**: 4 threads recommended
- **Local Jenkins**: 2-4 threads
- **Cloud runners**: 6+ threads if available

---

## Debugging Commands

### Check if Parallel is Working
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=4 2>&1 | grep -i "thread\|parallel"
```

### Run with Verbose Logging
```bash
mvn clean test -X -Dparallel.enabled=true -Dparallel.threads=2
```

### Run Specific Test Class
```bash
mvn clean test -Dtest=GoogleSearchSteps -Dparallel.enabled=true -Dparallel.threads=2
```

### Disable Parallel for Debugging
```bash
# When tests fail with parallel, run sequentially
mvn clean test
```

---

## Memory Configuration

### Default (1GB)
```
-Xmx1024m -XX:MaxPermSize=256m
```
Good for: 2-4 threads

### High Performance (2GB)
Edit pom.xml:
```xml
<argLine>-Xmx2048m -XX:MaxPermSize=512m</argLine>
```
Good for: 6-8 threads

### Very High Performance (4GB+)
Edit pom.xml:
```xml
<argLine>-Xmx4096m -XX:MaxPermSize=1024m</argLine>
```
Good for: 8+ threads, enterprise

---

## Troubleshooting Commands

### Tests fail with parallel but pass sequential
```bash
# Reduce threads to 2
mvn clean test -Dparallel.enabled=true -Dparallel.threads=2

# If still failing, disable parallel
mvn clean test
```

### Out of memory errors
Edit pom.xml and increase `-Xmx` value, then retry:
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=4
```

### Slow execution despite parallel
```bash
# Check actual thread usage
mvn clean test -Dparallel.enabled=true -Dparallel.threads=4 -X 2>&1 | grep thread
```

### WebDriver session errors
```bash
# Use headless + parallel + sequential debugging
mvn clean test -Dheadless=true -Dparallel.enabled=true -Dparallel.threads=2
```

---

## Copy-Paste Quick Commands

### Local Development (Safe)
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=2
```

### Local Development (Fast)
```bash
mvn clean test -Dparallel.enabled=true -Dparallel.threads=4
```

### Full Local Test (Optimized)
```bash
mvn clean test -Dheadless=true -Dparallel.enabled=true -Dparallel.threads=4
```

### Smoke Test Only
```bash
mvn clean test -Dcucumber.filter.tags="@smoke" -Dparallel.enabled=true -Dparallel.threads=2
```

### Firefox Specific
```bash
mvn clean test -Dbrowser=firefox -Dparallel.enabled=true -Dparallel.threads=4
```

### Edge Specific
```bash
mvn clean test -Dbrowser=edge -Dparallel.enabled=true -Dparallel.threads=4
```

### Debug Mode (Sequential)
```bash
mvn clean test -Dbrowser=chrome
```

---

## Environment Variables

### Linux/Mac
```bash
export PARALLEL_THREADS=4
mvn clean test -Dparallel.enabled=true -Dparallel.threads=$PARALLEL_THREADS
```

### Windows (PowerShell)
```powershell
$env:PARALLEL_THREADS=4
mvn clean test -Dparallel.enabled=true -Dparallel.threads=$env:PARALLEL_THREADS
```

---

## One-Liners for Common Tasks

```bash
# Quick check (2 threads)
mvn clean test -Dparallel.enabled=true -Dparallel.threads=2

# Full regression (4 threads, headless)
mvn clean test -Dheadless=true -Dparallel.enabled=true -Dparallel.threads=4

# Smoke only (2 threads)
mvn clean test -Dcucumber.filter.tags="@smoke" -Dparallel.enabled=true -Dparallel.threads=2

# Browser test (Firefox, 4 threads)
mvn clean test -Dbrowser=firefox -Dparallel.enabled=true -Dparallel.threads=4

# Debug failing test (sequential)
mvn clean test

# High performance (6 threads, headless)
mvn clean test -Dheadless=true -Dparallel.enabled=true -Dparallel.threads=6
```

---

## GitHub Actions Keyboard Shortcuts

| Action | Steps |
|--------|-------|
| Open Actions | Press `g` then `a` |
| Run workflow | Click "Run workflow" button |
| Scroll logs | Use arrow keys |
| Search logs | Press `Ctrl+F` |

---

**Last Updated**: 24 December 2025  
**Status**: ✅ Complete  
**Ready for Use**: Yes
