# OpenGLESScope 0.2.2

OpenGLESScope 0.2.2 fixes Android security-patch reporting parity with VulkanScope. The application now collects `Build.VERSION.SECURITY_PATCH`, shows it in the Android UI, includes it in TXT and self-contained HTML exports, and submits it as canonical `device.securityPatch` evidence to OpenGLESScope Database.

The security patch remains Android platform metadata only and is never used as OpenGL ES, EGL, display or HDR capability evidence.
