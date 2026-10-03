#!/usr/bin/env python3
"""Release-locked Kotlin TextUnit compile hotfix over exact OpenGLESScope 2.2.16."""
from __future__ import annotations
import hashlib, json, re
from gate_common import ROOT, require, main_guard
from verify_2_2_16_status_navigation import ui_oracle
from verify_2_2_15_evidence_integrity import native_oracle, host_compiled_oracle

MAIN='app/src/main/java/com/efishell/openglesscope/MainActivity.kt'
GRADLE='app/build.gradle.kts'
CONTRACT='tests/golden/2_2_17_release_regression_contract.json'

def compile_hotfix_oracle(s: str) -> None:
    require('val compactLabelFontSize = if (accessibilityScale) 10.sp else if (landscape) 8.sp else 9.sp' in s,
            'shared navigation label size drift')
    require('val compactOpenGlesLabelFontSize = if (landscape) 7.5.sp else 8.5.sp' in s,
            'explicit compile-safe OpenGL ES label size missing')
    require('fontSize = if (item.page == Page.OpenGLES && !accessibilityScale) compactOpenGlesLabelFontSize else compactLabelFontSize' in s,
            'OpenGL ES label does not use compile-safe explicit size')
    require('compactLabelFontSize - 0.5.sp' not in s,
            'unsupported TextUnit subtraction reintroduced')
    require(not re.search(r'compactLabelFontSize\s*-\s*[^\n,)]*\.sp', s),
            'TextUnit-minus-sp regression present')

def verify() -> None:
    c=json.loads((ROOT/CONTRACT).read_text(encoding='utf-8'))
    require(c['release']=='2.2.17' and c['predecessor']=='2.2.16','release lineage drift')
    require(c['predecessorZipSha256']=='ea73dbc97caa0136791a954c70b9d55e6459ebba7226e41e10b6f47c02d237b5',
            'immutable 2.2.16 predecessor ZIP drift')
    require(len(c['predecessorHashes'])==160 and set(c['allowlistedChanges'])=={MAIN,GRADLE},
            'production source census/allowlist drift')
    for path, old in c['predecessorHashes'].items():
        now=hashlib.sha256((ROOT/path).read_bytes()).hexdigest()
        if path in c['allowlistedChanges']:
            ch=c['allowlistedChanges'][path]
            require(ch['predecessorSha256']==old and ch['successorSha256']==now and old!=now and ch['reason'],
                    'unreviewed allowlisted change: '+path)
        else:
            require(now==old,'unrelated production mutation: '+path)
    g=(ROOT/GRADLE).read_text(encoding='utf-8')
    require('releaseVersionName = "2.2.17"' in g and 'releaseVersionCode = 2217' in g,'2.2.17 identity drift')
    require('minorApiLevel = 2' in g and 'targetSdk = 37' in g,'Android API 37.2 / target 37 drift')
    s=(ROOT/MAIN).read_text(encoding='utf-8')
    compile_hotfix_oracle(s)
    ui_oracle(s)
    n=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    native_oracle(n); host_compiled_oracle(n)
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    require('## Release 2.2.17 Kotlin TextUnit compile hotfix' in rules,'2.2.17 rules section missing')
    require((ROOT/'rules/2.2.17_KOTLIN_TEXTUNIT_COMPILE_HOTFIX_AUDIT.md').is_file(),'2.2.17 audit missing')
    print('verify_2_2_17_kotlin_compile_hotfix: PASS (160-path lineage, TextUnit compile fix, 2.2.16 UI/native semantics retained)')

if __name__=='__main__': main_guard('verify_2_2_17_kotlin_compile_hotfix',verify)
