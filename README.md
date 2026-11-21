# Permissions Viewer for Microcord

A read-only Android plugin that uses Microcord's plugin API to inspect Discord permissions. The [Microcord plugin template](https://github.com/microcord/plugin-template) documents the Android integration, and [Vencord’s Permissions Viewer](https://github.com/Vendicated/Vencord/tree/main/src/plugins/permissionsViewer) by Nuckyz and Vendicated inspired the feature.

## Install and use

1. Download the APK from [Releases](https://github.com/mmaarij123/microcord-permissions-viewer/releases) and install it on the device running Microcord. With ADB, use `adb install -r PermissionsViewer-debug.apk`.
2. Open **Microcord → Settings → Plugins**. Select **Permissions Viewer**, approve its signer when prompted, and enable it.
3. Long-press a server, channel, or thread and choose **View Permissions**. For a member, use the actions menu or the **View Permissions** button on their server profile.

Version 1.0.2 installs as the Android package `io.github.mmaarij123.permissionsviewer`. If you installed v1.0.1, disable that copy in Microcord before enabling v1.0.2.

The server view lists the permissions configured on each role. The member summary shows server-level grants, including `@everyone`, Administrator, and server ownership. Select a role for its individual settings. The attribution button switches between the highest and lowest granting role and saves that preference.

The channel view shows explicit role and member overwrites. Threads display the overwrites inherited from their parent channel. The viewer uses Microcord's local store snapshots and displays an ID when an overwrite target's name is unavailable.

The viewer uses the typefaces configured by Microcord's active theme. The stock theme uses Whitney for body text and controls.

## Build from source

Use JDK 21 and Android SDK Platform 36.1. Set `ANDROID_HOME` to your Android SDK directory or configure `sdk.dir` in `local.properties`.

```powershell
.\gradlew.bat assembleDebug
```

On macOS and Linux, run `./gradlew assembleDebug`. Gradle writes the APK to `build/outputs/apk/debug/`. The project uses Microcord's published plugin API 1.0.6. A sibling checkout named `manticordrebuild` can supply the local Gradle plugin and SDK JAR.

## License

Permissions Viewer is available under the [MIT License](LICENSE). The Gradle Wrapper is distributed under [Apache License 2.0](gradle/wrapper/LICENSE).
