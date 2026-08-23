# OpenGLESScope 0.3.4

- Fixed the 0.3.3 duplicate query-diagnostic regression that could make real complete reports fail strict OpenGLESScope Database validation.
- Canonical diagnostic names are now unique even when a count value is read by both Limits and enumeration code.
- Repeated internal reads merge conservatively; inconsistent evidence remains unavailable instead of being silently hidden.
- Preserves separate count and values-array diagnostics and all existing UI/TXT/HTML/Database reporting.
- Version: 0.3.4, versionCode 304.
