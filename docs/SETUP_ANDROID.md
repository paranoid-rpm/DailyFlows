# Android setup (Windows/macOS/Linux)

This project intentionally does **not** commit `local.properties` because it contains a machine-specific Android SDK path.

If Android Studio shows something like:

> The SDK path '.../Android/Sdk' does not belong to a directory.

it means your local `local.properties` points to a path that doesn't exist on this computer.

## Fix (recommended)

1) Install Android Studio.
2) Install Android SDK (Android Studio → Settings/Preferences → **Android SDK**).
3) Either:
- Delete `local.properties` (Android Studio will recreate it on Sync), or
- Edit/create `local.properties` at the project root:

### Windows example
```
sdk.dir=C\\:\\Users\\<YOUR_USER>\\AppData\\Local\\Android\\Sdk
```

### macOS example
```
sdk.dir=/Users/<YOUR_USER>/Library/Android/sdk
```

### Linux example
```
sdk.dir=/home/<YOUR_USER>/Android/Sdk
```

Then run **Gradle Sync**.

## Alternative via env var

You can also set `ANDROID_SDK_ROOT` to your SDK directory and (optionally) generate `local.properties` using the scripts in `scripts/`.
