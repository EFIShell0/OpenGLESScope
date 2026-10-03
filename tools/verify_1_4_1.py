#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    changelog=(ROOT/'changelog.md').read_text(encoding='utf-8')
    audit=(ROOT/'BUILD_AUDIT.md').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    for heading in ['Release 1.4.1 real-compiler build correction','Build gate','Negative-mutation gate','Deterministic package and clean-extract gate']:
        require(heading in rules,f'1.4.1 rules heading/topic missing: {heading}')
    require(changelog.startswith('# OpenGLESScope 1.4.1'),'changelog missing current 1.4.1 entry')
    require('releaseVersionName = "1.4.1"' in gradle and 'releaseVersionCode = 1401' in gradle,'1.4.1 identity missing')
    require((ROOT/'rules/1.4.1_REAL_COMPILER_BUILD_CORRECTION_AUDIT.md').is_file(),'1.4.1 audit file missing')
    for phrase in ['1.4.0 real Android build', 'unused variable', 'Argument type mismatch', '@Composable invocations can only happen', '1.4.1']:
        require(phrase in audit or phrase in changelog or phrase in rules,f'compiler-evidence phrase missing: {phrase}')
if __name__=='__main__': main_guard('verify_1_4_1',verify)
