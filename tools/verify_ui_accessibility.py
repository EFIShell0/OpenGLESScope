#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text()
    require('android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden"' in manifest,'declared configuration-retention contract missing')
    require('var page by rememberSaveable' in main,'primary navigation state is not saveable')
    require(main.count('var query by rememberSaveable') >= 6,'data-surface search state retention regressed')
    require('var diffQuery by rememberSaveable' in main and 'var minimumQuery by rememberSaveable' in main and 'var graphQuery by rememberSaveable' in main,'analysis filter state retention regressed')
    require('LiveRegionMode.Polite' in main and '.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }' in main,'status live-region semantics missing')
    require('BringIntoViewRequester' in main and 'FocusRequester' in main,'Android TV/focus bring-into-view contract missing')
    require('private fun OpenGLESScopeLazyPage(' in main and 'LazyColumn(' in main and main.count('OpenGLESScopeLazyPage(') >= 10,'large data surfaces must retain centralized lazy containers')
    require('WindowInsets.navigationBars.asPaddingValues()' in main,'navigation-bar inset handling missing')
    require('ShortNavigationBar' in main,'shared portrait/landscape bottom navigation surface missing')
    require('CompactNavigationRail' not in main,'legacy landscape navigation rail must not return')
    require('contentDescription = null' in main,'decorative icon accessibility ownership contract missing')
    require('horizontalScroll(rememberScrollState())' in main,'bounded horizontal technical-content fallback missing')

if __name__=='__main__': main_guard('verify_ui_accessibility',verify)
