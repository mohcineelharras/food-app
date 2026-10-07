# food-app

On-device dish recommender for Android. A diet filter and a kitchen note stay in app-private storage. The app does not request network or location access, and it does not ship a third-party API key.

## Build

Use JDK 17 or newer and Android SDK platform 36.

```
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease releaseHygiene
```

Release builds are minified and are not debuggable.
