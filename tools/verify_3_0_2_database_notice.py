from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
notice='Compatibility notice: when a newer OpenGLESScope release raises the Database submission floor, reports from older app versions are rejected by the server. A rejection is returned as a submission error and is shown here instead of being treated as a successful upload.'
intro='Submit the complete technical OpenGLESScope report to the public database. Capability fields cannot be selectively omitted, and sensitive device identifiers or private paths are not included.'
assert main.count(notice)==1,'VulkanScope warning text or placement drift'
assert main.count(intro)==1,'Database introduction differs from VulkanScope reference'
assert main.count('Compatibility notice:')==1
assert '3.0.2' not in notice and 'unaudited' not in notice and 'versionCode' not in notice
assert 'releaseVersionName = "3.0.7"' in gradle and 'releaseVersionCode = 3007' in gradle
print('OpenGLESScope 3.0.3 locked reference Database compatibility notice: PASS')
