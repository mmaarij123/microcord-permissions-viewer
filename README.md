# Permissions Viewer for Microcord

A read-only plugin for Microcord on Android. See which permissions a server role has, where a member gets their permissions, and what a channel explicitly allows or denies.

## What you can view

- **Server roles** show which permissions are granted or unset for each role.
- **Members** show server-level permissions and where each grant comes from, including `@everyone`, Administrator roles, and server ownership. Switch the attribution between the highest and lowest granting role. The plugin remembers your choice.
- **Channels and threads** show explicit allows and denies for individual roles and members. Threads display the overwrites from their parent channel.

## Download and install

1. Open the [latest GitHub release](https://github.com/mmaarij123/microcord-permissions-viewer/releases/latest) and download **PermissionsViewer-debug.apk** under **Assets**. You can also [download the APK directly](https://github.com/mmaarij123/microcord-permissions-viewer/releases/latest/download/PermissionsViewer-debug.apk).
2. Open the downloaded APK on the Android device that runs Microcord and install it.
3. In **Microcord → Settings → Plugins**, select **Permissions Viewer**, approve the signer if prompted, and enable the plugin.

If you installed v1.0.1, disable that copy in Microcord before enabling the new one. Android installs v1.0.2 as a separate app.

## Open the viewer

- Long-press a server, channel, or thread and choose **View Permissions**.
- For a member, choose **View Permissions** from their actions menu or server profile.
- Use the dropdown to inspect an individual role or overwrite. The details screen also lets you copy the selected target's ID.

## Technical details

The plugin is written in Kotlin and uses Microcord's plugin API 1.0.6 to register its menus and profile button. It reads server roles, members, and channel overwrites from Microcord's local stores. If an overwrite target's name is unavailable, the viewer displays its ID. The UI uses the active Microcord theme's fonts.

The Android application ID is `io.github.mmaarij123.permissionsviewer`. Microcord discovers the APK through its manifest and loads the entry point under plugin ID `manticord.permissionsviewer`.

The Android integration follows the [Microcord plugin template](https://github.com/microcord/plugin-template). The feature was inspired by [Vencord’s Permissions Viewer](https://github.com/Vendicated/Vencord/tree/main/src/plugins/permissionsViewer) by Nuckyz and Vendicated.

### Build from source

Clone this repository and install JDK 21 and Android SDK Platform 36.1. Set `ANDROID_HOME` to your SDK directory or set `sdk.dir` in `local.properties`.

**Windows**

```powershell
.\gradlew.bat assembleDebug
```

**macOS or Linux**

```sh
./gradlew assembleDebug
```

Gradle writes the debug APK to `build/outputs/apk/debug/`. It uses Microcord's published plugin SDK by default. A sibling checkout named `manticordrebuild` can supply the local Gradle plugin and SDK JAR.

## License

The original Android Permissions Viewer implementation is licensed under MIT. Copyright © 2026 Maarij. You may use, copy, modify, and redistribute the code, including for commercial use. Include the copyright notice and permission notice with copies or substantial portions of the code. The code is provided as is, without warranty. The full terms include a limitation of liability. [Read the MIT license](LICENSE)

The APK includes a copy of the MIT license. See the notices for the Vencord credit and the Gradle Wrapper license. [Third-party notices](THIRD_PARTY_NOTICES.md)
