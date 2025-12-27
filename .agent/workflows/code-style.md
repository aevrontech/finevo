---
description: Code formatting and style guidelines for FinEvo project
---

# FinEvo Code Style Guidelines

## CRITICAL FORMATTING RULES

1. **Indentation**: Use 4 spaces (NOT tabs)
2. **NO excessive blank lines** - maximum 1 blank line between functions
3. **NO trailing whitespace**
4. **Brace style**: Opening brace on same line as function/class declaration

## Kotlin Formatting

```kotlin
// CORRECT - Clean, minimal spacing
class Example {
    fun doSomething() {
        val x = 1
        if (x > 0) {
            println("positive")
        }
    }
}

// WRONG - Excessive indentation/spacing
class Example {
        fun doSomething() {
                val x = 1
        }
}
```

## IDE Settings (Android Studio / IntelliJ)

1. **File > Settings > Editor > Code Style > Kotlin**
   - Tab size: 4
   - Indent: 4
   - Continuation indent: 4
   - Use tab character: OFF (use spaces)

2. **Enable EditorConfig support**:
   - File > Settings > Editor > Code Style
   - Check "Enable EditorConfig support"

3. **Auto-format on save**:
   - File > Settings > Tools > Actions on Save
   - Check "Reformat code"
   - Check "Optimize imports"

4. **Apply ktlint or detekt** for consistent formatting

## To Fix Existing Files

Run in Android Studio:
- Select all files in Project view
- Right-click > Reformat Code (Ctrl+Alt+L)
- Check "Optimize imports"
- Check "Rearrange entries"
