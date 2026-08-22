# OpenGLESScope 0.2.8 Build Audit

- Application version: 0.2.8
- versionCode: 208
- Change scope: Settings/Info top-app-bar Material 3 Expressive icon parity and action ordering only.
- VulkanScope `ic_settings.xml` and `ic_info.xml` assets copied byte-for-byte into OpenGLESScope.
- Normal-destination action order: Info then Settings, which renders Settings rightmost.
- Settings/Info destinations expose neither action, matching VulkanScope.
- Direct GitHub update defaults/security and Obtainium informational behavior preserved.
- OpenGL ES/EGL native collector, Display/HDR collection, TXT/HTML export and Database submission code unchanged.
