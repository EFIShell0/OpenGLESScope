#!/usr/bin/env python3
from __future__ import annotations
import re
from pathlib import Path
from gate_common import ROOT,require,main_guard

def part(source,start,end='\n@Composable\n'):
    i=source.find(start);require(i>=0,'missing UI component: '+start)
    j=source.find(end,i+len(start));return source[i:j if j>=0 else len(source)]

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    require(any(f'releaseVersionName = "{v}"' in gradle and f'releaseVersionCode = {c}' in gradle for v,c in [('2.2.3',2203),('2.2.4',2204),('2.2.5',2205),('2.2.6',2206),('2.2.7',2207),('2.2.8',2208),('2.2.9',2209),('2.2.10',2210),('2.2.11',2211),('2.2.13',2213),('2.2.14',2214)]),'2.2.3+ release identity drift')
    require('## Release 2.2.3 full branding/copy/HDR/extension interaction parity audit' in rules,'release contract missing')
    header=part(main,'private fun AppHeader(')
    require(('painterResource(R.drawable.openglesscope_logo_horizontal)' in header or 'painterResource(R.drawable.openglesscope_logo_horizontal_aligned)' in header) and 'contentDescription = "OpenGLESScope"' in header,'header not using the complete product wordmark')
    require('painterResource(R.drawable.openglesscope_scope_wordmark)' not in header,'cropped SCOPE-only header returned')
    clipboard=part(main,'private fun copyEvidenceText(',end='\n@Composable\n')
    require('clipboard.setPrimaryClip' in clipboard,'clipboard action lost')
    require('Toast' not in clipboard and '"Copied"' not in clipboard,'duplicate Android clipboard toast returned')
    for p in (ROOT/'app/src/main').rglob('*'):
        if p.suffix in {'.kt','.java','.xml'} and p.is_file():
            text=p.read_text(encoding='utf-8')
            require(re.search(r'(?i)\b(?:vulkan|vulkanscope|turnip)\b',text) is None,'foreign-product prose in app: '+str(p.relative_to(ROOT)))
    hdr=part(main,'private fun DisplayPage(d: DisplayInfo)')
    for token in ['HdrCapabilitiesCarousel(d.hdrTypes)','"None reported"','"Unknown / not exposed"','"Unavailable"','"HDR capability status"','desiredMinLuminance','desiredMaxLuminance','desiredMaxAverageLuminance']:
        require(token in hdr,'HDR presentation/data regression: '+token)
    carousel=part(main,'private fun HdrCapabilitiesCarousel(')
    for token in ['rememberScrollState()','horizontalScroll(scrollState)','HdrTypeCard(it)','canMoveLeft','canMoveRight','scrollState.animateScrollTo','ic_chevron_left','ic_chevron_right','contentDescription = "Scroll HDR capabilities left"','contentDescription = "Scroll HDR capabilities right"']:
        require(token in carousel,'HDR navigation regression: '+token)
    egl=part(main,'private fun EglBrandArtwork(')
    if any(f'releaseVersionName = "{v}"' in gradle for v in ('2.2.7','2.2.8','2.2.9','2.2.10','2.2.11','2.2.13','2.2.14')):
        for token in ['Image(','painterResource(R.drawable.ic_egl_official)','ColorFilter.tint(tint)','ContentScale.Fit']:
            require(token in egl,'official EGL recolored image missing: '+token)
    else:
        for token in ['Text("EGL"','BrandSoft','FontWeight.Black','mergeDescendants = true']:
            require(token in egl,'historic typographic EGL artwork missing: '+token)
    semantic=part(main,'private fun SemanticArtwork(')
    require('icon == R.drawable.ic_egl_official -> EglBrandArtwork(modifier, contentDescription)' in semantic,'old hard-red EGL bitmap returned to destination card')
    section=part(main,'private fun SectionHeaderIcon(')
    require('sectionIcon == R.drawable.ic_egl_official ->' in section and 'EglBrandArtwork(' in section,'EGL section header artwork regressed')
    require('tint = BrandSoft, modifier = Modifier.size(22.dp)' in section and 'ic_opening_animation_toggle' in section,'opening animation monochrome icon no longer follows theme')
    quick=part(main,'private fun QuickAccessCard(')
    require('SemanticArtwork(pageIcon(destination)' in quick,'quick-access EGL artwork not themed')
    extension=part(main,'private fun ExtensionsPage(')
    require('CapabilityKeyValue(ext, "ENUMERATED · $scopeName")' in extension,'extension token long-press evidence actions missing')
    require('ChevronAffordance("Details"' in extension,'explicit extension detail action missing')
    if 'releaseVersionName = "2.2.14"' in gradle:
        for token in ['CapabilityKeyValue("Scope"','CapabilityKeyValue("Runtime evidence"','CapabilityKeyValue("Dedicated query handler"','Unknown · query evidence unavailable','extensionRegistryUrl(ext)','"Open Khronos specification"']:
            require(token in extension,'2.2.12 reference evidence details missing: '+token)
        require('TransientActionButton("Copy name + value"' not in extension and 'ExpressiveActionButton("Share evidence"' not in extension,'extra action cards returned')
    else:
        if any(f'releaseVersionName = "{v}"' in gradle for v in ('2.2.7','2.2.8','2.2.9','2.2.10','2.2.11','2.2.13','2.2.14')):
            for token in ['DetailEvidenceRow("Scope"','DetailEvidenceRow("Runtime evidence"','DetailEvidenceRow("Implemented query gates"','TransientActionButton("Copy name + value"','ExpressiveActionButton("Share evidence"','TransientActionButton("Add to watched evidence"','ExpressiveActionButton("Open in Encyclopedia"','Unknown · query evidence unavailable','extensionRegistryUrl(ext)']:
                require(token in extension,'2.2.7 reference evidence detail/menu parity regressed: '+token)
        else:
            for token in ['CapabilityKeyValue("Scope"','CapabilityKeyValue("Runtime evidence"','CapabilityKeyValue("Implemented query gates"','TransientActionButton("Copy extension"','Unknown · query evidence unavailable','extensionRegistryUrl(ext)']:
                require(token in extension,'historic extension detail evidence boundary regressed: '+token)
    static=part(main,'private fun CapabilityKeyValueStatic(')
    require('key.length > 28 || value.length > 52' in static,'long canonical extension/limit tokens squeezed into half-width layout')
    cap=part(main,'private fun CapabilityKeyValue(key: String, value: String)')
    for token in ['onLongPress = { showActions = true }','delay(550L)','CustomAccessibilityAction("Evidence actions")','DropdownMenu(expanded = showQuickMenu','Copy name + value','Open in Encyclopedia']:
        require(token in cap,'extension/common evidence keyboard-pointer-menu parity regressed: '+token)
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'SUBMISSION_SCHEMA_VERSION = 2' in main,'unrelated report schema changed')
if __name__=='__main__': main_guard('verify_2_2_3_full_ui',verify)
