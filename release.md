# OpenGLESScope 0.2.7

OpenGLESScope 0.2.7 changes the fresh-install update default while preserving both the built-in GitHub updater and optional Obtainium guidance. OpenGL ES/EGL capability coverage is unchanged.

## Changes

- **Direct GitHub updates** are enabled by default on fresh installations.
- Existing installations keep their previously saved update preference.
- Disabling Direct GitHub updates still prevents startup update discovery and APK download.
- Obtainium remains an optional external update manager described in Settings and Info.
- The **Add to Obtainium** button remains removed.
- `obtainium-config.json` remains in the source release with universal-APK selection and architecture auto-filtering disabled.
- No OpenGL ES/EGL capability, report, export or Database behavior was intentionally changed.

## Version

- Version: `0.2.7`
- versionCode: `207`
- Package: `com.efishell.openglesscope`
- OpenGL ES baseline: `3.2`
- GLSL ES baseline: `3.20`
- EGL baseline: `1.5`
