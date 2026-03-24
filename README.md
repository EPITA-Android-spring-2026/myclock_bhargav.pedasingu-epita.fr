# ClockApp - Kotlin Version

## How to Open in Android Studio

1. Open Android Studio
2. Click **"Open"** → select the `ClockAppKotlin` folder
3. Wait for Gradle sync (progress bar at bottom)
4. Connect Pixel 9a via USB (enable USB Debugging in Settings → Developer Options)
5. Press the green **Run ▶** button

---

## What Changed from Java → Kotlin

### 1. No Semicolons
```java
// Java
String name = "hello";
int x = 5;
```
```kotlin
// Kotlin
val name = "hello"   // val = immutable (like final)
var x = 5            // var = mutable
```

### 2. Shorter Click Listeners (Lambdas)
```java
// Java - verbose
button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        doSomething();
    }
});
```
```kotlin
// Kotlin - clean lambda
button.setOnClickListener { doSomething() }
```

### 3. `when` instead of `switch`
```kotlin
// Kotlin
val fragment = when (screenName) {
    "clock"     -> ClockFragment()
    "alarm"     -> AlarmFragment()
    else        -> ClockFragment()  // must have "else" (like default)
}
```

### 4. Null Safety with `?`
```kotlin
var timer: CountDownTimer? = null  // "?" = can be null
timer?.cancel()   // "?." = only calls cancel() if timer is not null
```

### 5. String Templates
```kotlin
// Instead of "Hour: " + hour + " Minute: " + minute
val msg = "Hour: $hour Minute: $minute"   // $ embeds a variable
val msg2 = "Time: ${hour + 1}:${minute}"  // ${} for expressions
```

### 6. `data class` instead of POJO
```java
// Java - need constructor, getters, etc.
public class AlarmItem {
    public int id, hour, minute;
    public AlarmItem(int id, int hour, int minute) { ... }
}
```
```kotlin
// Kotlin - one line!
data class AlarmItem(val id: Int, val hour: Int, val minute: Int)
```

### 7. `.apply {}` Scope Function
```kotlin
// Set multiple properties on an object cleanly
val card = LinearLayout(activity).apply {
    orientation = LinearLayout.HORIZONTAL
    setPadding(20, 20, 20, 20)
    setBackgroundColor(0xFFEEEEFF.toInt())
}
```

### 8. `companion object` instead of `static`
```kotlin
// Kotlin has no "static" - use companion object instead
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "alarm_channel"  // like Java's static final
    }
}
```

### 9. `::class.java` instead of `.class`
```kotlin
// Java:  Intent(context, AlarmReceiver.class)
// Kotlin:
val intent = Intent(context, AlarmReceiver::class.java)
```

### 10. `or` instead of `|` for bitwise OR
```kotlin
// Java:  FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE
// Kotlin:
PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
```

---

## Project Structure

```
ClockAppKotlin/
├── app/src/main/
│   ├── kotlin/com/example/clockapp/    ← Kotlin source files (.kt)
│   │   ├── MainActivity.kt
│   │   ├── ClockFragment.kt
│   │   ├── AlarmFragment.kt
│   │   ├── AlarmReceiver.kt
│   │   ├── TimerFragment.kt
│   │   └── StopwatchFragment.kt
│   ├── res/layout/                     ← XML layouts (same as Java version)
│   └── AndroidManifest.xml
├── app/build.gradle                    ← Added: kotlin.android plugin
├── build.gradle                        ← Added: kotlin plugin version
└── settings.gradle
```

---

## Key build.gradle Differences

The main difference in `build.gradle` for Kotlin:

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'   // ← This line makes it Kotlin!
}

// ...

kotlinOptions {
    jvmTarget = '1.8'    // ← Compile to Java 8 bytecode
}
```
