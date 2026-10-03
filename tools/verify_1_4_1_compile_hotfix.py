#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import re

def verify():
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    m=re.search(r'releaseVersionCode\s*=\s*(\d+)',gradle); require(m is not None and int(m.group(1))>=1401,'1.4.1 compiler-fix baseline requires versionCode >= 1401')
    # Exact compiler failures supplied from the 1.4.0 release build must stay absent.
    for token in ['EGL_BUFFER_AGE_KHR_VALUE','EGL_SURFACE_COMPRESSION_EXT_VALUE','EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY_KHR_VALUE','EGL_CONTEXT_OPENGL_NO_ERROR_KHR_VALUE']:
        require(token not in native,f'known Clang -Wunused-const-variable regression present: {token}')
    for match in re.finditer(r'^static constexpr\s+[^;=]+\s+(\w+)\s*=', native, re.MULTILINE):
        name=match.group(1)
        require(len(re.findall(r'\b'+re.escape(name)+r'\b',native))>1,f'file-scope static constexpr would fail -Wunused-const-variable under -Werror: {name}')
    require('val evidence=raw ?: "Unavailable"' in main,'nullable CustomMinimumResult evidence hardening missing')
    require('CustomMinimumResult(rule, raw,' not in main,'nullable String? still passed where String is required')
    require('val rawTechnicalEntries = remember(report, display, mode)' in main,'Raw technicalReport remember not hoisted into composable scope')
    require('val technical = remember(report, display)' not in main,'illegal remember remains in LazyListScope branch')
    require('val rawEntries = remember(technical)' not in main,'illegal nested remember remains in LazyListScope branch')
    # No other top-level remember declaration may be introduced inside this LazyColumn DSL branch area.
    start=main.index('LazyColumn(contentPadding = WindowInsets.navigationBars.asPaddingValues()')
    end=main.index('\n}\n\n@Composable',start) if '\n}\n\n@Composable' in main[start:] else len(main)
    lazy=main[start:end]
    for line in lazy.splitlines():
        stripped=line.strip()
        if re.match(r'val\s+\w+\s*=\s*remember\(', stripped):
            raise AssertionError('top-level remember inside LazyListScope: '+stripped)
if __name__=='__main__': main_guard('verify_1_4_1_compile_hotfix',verify)
