from pathlib import Path
import hashlib, json, re, sys, zipfile
ROOT=Path(__file__).resolve().parents[1]
fail=[]
def need(cond,msg):
    if not cond: fail.append(msg)
b=(ROOT/'app/build.gradle.kts').read_text()
need('releaseVersionName = "1.2.0"' in b,'versionName')
need('releaseVersionCode = 1200' in b,'versionCode')
m=(ROOT/'app/src/main/AndroidManifest.xml').read_text()
for token in ['orientation','screenSize','smallestScreenSize','screenLayout','keyboardHidden']:
    need(token in m,f'configChanges:{token}')
s=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
need('var page by rememberSaveable' in s,'saveable primary navigation')
need('NET_CAPABILITY_VALIDATED' in s,'validated network capability')
need('hasValidatedInternet(this)' in s,'update validated-network gate')
need('hasValidatedInternet(context)' in s,'submission validated-network gate')
need('rememberSaveable { mutableStateOf("") }' in s,'saveable search/filter state')
need('liveRegion = LiveRegionMode.Polite' in s,'polite live-region status semantics')
reg=ROOT/'registry/gl.xml'; man=ROOT/'registry/gl_registry_manifest.json'
need(reg.is_file(),'registry/gl.xml')
need(man.is_file(),'registry manifest')
if reg.is_file() and man.is_file():
    data=reg.read_bytes(); j=json.loads(man.read_text())
    need(hashlib.sha256(data).hexdigest()==j.get('sha256'),'registry sha256')
rules=(ROOT/'rules/PROJECT_RULES.md').read_text()
for token in ['Mandatory evidence workflow','OpenGL ES and EGL evidence','Display and HDR','Release 1.2.0 parity baseline']:
    need(token in rules,f'rules:{token}')
for forbidden in ['screenshots/','.idea/','__pycache__/']:
    need(not (ROOT/forbidden.rstrip('/')).exists(),f'package hygiene:{forbidden}')
if fail:
    print('FAIL'); [print(' - '+x) for x in fail]; sys.exit(1)
print('PASS: OpenGLESScope 1.2.0 parity contracts')
