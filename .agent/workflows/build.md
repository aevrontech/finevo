---
description: How to build and test the FinEvo Android app
---

// turbo-all

## Build Commands

1. **Compile Debug Android**
```bash
.\gradlew.bat :composeApp:compileDebugKotlinAndroid --no-daemon
```

2. **Run Unit Tests**
```bash
.\gradlew.bat :composeApp:testDebugUnitTest --no-daemon
```

3. **Build Debug APK**
```bash
.\gradlew.bat :composeApp:assembleDebug --no-daemon
```

4. **Clean Build**
```bash
.\gradlew.bat clean :composeApp:compileDebugKotlinAndroid --no-daemon
```

## Notes
- All commands in this workflow will auto-run without asking for approval
- The `// turbo-all` annotation enables this automation
- Commands are safe read-only operations (compile, test, build)
