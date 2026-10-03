#!/usr/bin/env python3
from __future__ import annotations
import hashlib, json, re
from pathlib import Path
from gate_common import ROOT, require, main_guard

VULKAN_RULES_SHA='9c89153fb34352f567ccdec7b3685ac1feab56c45b46b22f320375dfed66e7ca'
SCOPE_WORDMARK_SHA='9167a36fe6f31e21d796bc01d42276d1e0f2397193ab6e814133b7bf0dc72ed9'
GL_SHA='b9ca2cfa5c676e901c20d34af3407f1687cde0f1336a5ff7a8974d04c7494ad3'
EGL_SHA='3327619123cdaa999400b41a7060180616df42571a0fdd11e9726e026f6d0fb8'
def sha(p:Path)->str:return hashlib.sha256(p.read_bytes()).hexdigest()

def function_slice(text:str,name:str,next_marker='\n@Composable\n')->str:
    start=text.find(name)
    require(start>=0,f'missing source marker: {name}')
    end=text.find(next_marker,start+len(name))
    return text[start:end if end>=0 else len(text)]

def verify():
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    require(any(f'releaseVersionName = "{n}"' in gradle and f'releaseVersionCode = {c}' in gradle for n,c in [('2.2.0',2200),('2.2.1',2201),('2.2.2',2202),('2.2.3',2203),('2.2.4',2204),('2.2.5',2205),('2.2.6',2206),('2.2.7',2207),('2.2.8',2208),('2.2.9',2209),('2.2.10',2210),('2.2.11',2211),('2.2.13',2213),('2.2.14',2214)]),'2.2.0+ parity identity drift')
    require('## Release 2.2.0 VulkanScope 3.0.12 visual/interaction parity contract' in rules,'2.2.0 rules contract missing')
    ref=ROOT/'rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md'
    require(ref.is_file() and sha(ref)==VULKAN_RULES_SHA,'VulkanScope 3.0.12 rule reference drift')
    require(sha(ROOT/'registry/gl.xml')==GL_SHA and sha(ROOT/'registry/egl.xml')==EGL_SHA,'Khronos registry lock drift')

    # SCOPE branding parity. Full OpenGLESScope logo remains only on OpenGLESScope-specific launch/report branding.
    scope=ROOT/'app/src/main/res/drawable-nodpi/openglesscope_scope_wordmark.png'
    require(scope.is_file() and sha(scope)==SCOPE_WORDMARK_SHA,'SCOPE wordmark must remain byte-identical to VulkanScope 3.0.12 SCOPE artwork')
    header=function_slice(main,'private fun AppHeader(')
    version=function_slice(main,'private fun ExpressiveVersionBlock(')
    about=function_slice(main,'private fun AboutSectionIcon(')
    # The 2.2.3 full product-logo correction supersedes the historical 2.2.0 SCOPE-only header;
    # the immutable SCOPE art still remains mandatory in the version/about surfaces.
    if any(f'releaseVersionName = "{v}"' in gradle for v in ('2.2.3','2.2.4','2.2.5','2.2.6','2.2.7','2.2.8','2.2.9','2.2.10','2.2.11','2.2.13','2.2.14')):
        require(('painterResource(R.drawable.openglesscope_logo_horizontal)' in header or 'painterResource(R.drawable.openglesscope_logo_horizontal_aligned)' in header) and 'contentDescription = "OpenGLESScope"' in header,'full OpenGLESScope header logo missing')
    else:
        require('R.drawable.openglesscope_scope_wordmark' in header and 'openglesscope_logo_horizontal' not in header,'historical SCOPE-only header drift')
    if any(f'releaseVersionName = "{v}"' in gradle for v in ('2.2.8','2.2.9','2.2.10','2.2.11','2.2.13','2.2.14')):
        require('R.drawable.openglesscope_logo_foreground' in version and 'R.drawable.openglesscope_scope_wordmark' not in version,'current Application version block must use complete product artwork')
    else:
        require('R.drawable.openglesscope_scope_wordmark' in version,'historical Application version block artwork regression')
    require('R.drawable.openglesscope_scope_wordmark' in about,'About badge is not using SCOPE artwork')
    require('title.equals("Application", true) -> R.drawable.openglesscope_scope_wordmark' in main,'Application section semantic artwork drift')

    # 2.2.4 supersedes the historic Display-as-primary mapping with EGL-as-primary,
    # retaining the exact four-action bottom bar and both GL and EGL detail routes.
    nav=function_slice(main,'private fun navigationItems()',next_marker='\nprivate fun pageTransitionIndex')
    if any(f'releaseVersionName = "{v}"' in gradle for v in ('2.2.4','2.2.5','2.2.6','2.2.7','2.2.8','2.2.9','2.2.10','2.2.11','2.2.13','2.2.14')):
        required=['NavigationItem(Page.Overview, "Overview"','NavigationItem(Page.OpenGLES, "OpenGL® ES™"' if 'releaseVersionName = "2.2.14"' in gradle else 'NavigationItem(Page.OpenGLES, "OpenGL® ES"','NavigationItem(Page.EGL, "EGL™"','NavigationItem(Page.Extensions, "Extensions"']
        for token in required:require(token in nav,'current four-destination navigation missing: '+token)
        require('NavigationItem(Page.Display' not in nav,'Display must be secondary after 2.2.4')
        require('Page.Display, Page.Features' in main and 'Page.EGL -> Page.OpenGLES' not in main,'current page-ownership drift')
        require('Page.EGL -> EglPage(report)' in main,'independent EGL route lost')
    else:
        for token in ['NavigationItem(Page.Overview, "Overview"','NavigationItem(Page.OpenGLES, "OpenGL® ES"','NavigationItem(Page.Display, "Display"','NavigationItem(Page.Extensions, "Extensions"']:
            require(token in nav,'historical primary navigation drift: '+token)
        require('NavigationItem(Page.EGL' not in nav,'historical EGL primary-nav ownership drift')
        require('Page.EGL -> Page.OpenGLES' in main,'historical EGL nesting drift')
    require('private enum class OpenGlesSection' in main and 'EGL("EGL™"' in main,'nested EGL destination missing')
    require('OpenGlesSection.EGL -> EglPage(report)' in main,'nested EGL routing missing')

    # Official API artwork/marks: artwork is never tinted as a generic vector.
    require('private fun isOfficialApiArtwork' in main and 'icon == R.drawable.ic_opengles_gl_es' in main and 'icon == R.drawable.ic_egl_official' in main,'official API artwork classifier missing')
    require('private fun SemanticArtwork' in main and 'Image(painter = painterResource(icon)' in main,'official API artwork rendering missing')
    for token in ['OpenGL® ES','EGL™','SPIR-V™']:
        require(token in main,'official user-facing API mark missing: '+token)
    require('OPENGL_ES_TRADEMARK_DISPLAY_REGEX' in main and 'EGL_TRADEMARK_DISPLAY_REGEX' in main and 'SPIRV_TRADEMARK_DISPLAY_REGEX' in main,'API trademark display normalizer missing')
    require('R.drawable.ic_precision' in main and 'title.equals("Shader precision", true) -> R.drawable.ic_precision' in main,'Shader precision semantic icon drift')
    require('title.equals("EGL Configs", true) || title.startsWith("EGL Config ", true) -> R.drawable.ic_configs' in main,'EGL Config semantic icon drift')
    require('"Check for updates"' in main and 'R.drawable.ic_download' in main and 'trailingIcon = R.drawable.ic_receive' in main,'update action semantic icon drift')
    require('R.drawable.ic_check_updates' not in main,'obsolete/wrong check-updates icon is wired in production UI')

    # Shared VulkanScope common icons stay byte-identical. The 2.2.0 census expands this to every common drawable resource.
    drawable_golden=json.loads((ROOT/'tests/golden/2_2_0_vulkanscope_common_drawable_hashes.json').read_text(encoding='utf-8'))
    drawable_files=drawable_golden.get('files',{})
    require(len(drawable_files)>=100,'shared VulkanScope drawable census unexpectedly small')
    for rel,expected in drawable_files.items():
        p=ROOT/'app/src/main/res'/rel
        require(p.is_file(),f'shared VulkanScope drawable missing: {rel}')
        require(sha(p)==expected,f'shared VulkanScope drawable byte drift: {rel}')
    golden=json.loads((ROOT/'tests/golden/2_1_0_common_icon_hashes.json').read_text(encoding='utf-8'))
    files=golden.get('files',{})
    require(len(files)>=80,'shared semantic icon census unexpectedly small')
    for rel,expected in files.items():
        p=ROOT/'app/src/main/res'/rel
        require(p.is_file(),f'shared VulkanScope icon missing: {rel}')
        require(sha(p)==expected,f'shared VulkanScope icon byte drift: {rel}')

    # True frosted chrome, edge-to-edge layout and overlay coordination.
    for token in ['AndroidRenderEffect.createBlurEffect','24.dp.toPx()','private fun Modifier.frostedChromeBackdrop','drawLayer(backdropLayer)','drawRect(GlassTint)','contentWindowInsets = WindowInsets(0, 0, 0, 0)','LocalAppHeaderContentInset','LocalBottomNavigationContentInset','LocalTransientOverlayContentInset','Modifier.matchParentSize().zIndex(6f)','onSizeChanged { bottomNavigationHeightPx = it.height }.zIndex(10f)']:
        require(token in main,'VulkanScope live-blur/chrome parity missing: '+token)
    lazy=function_slice(main,'private fun OpenGLESScopeLazyPage(')
    lazy_insets=['sourceLayer.record','blurredLayer.record']
    if any(f'releaseVersionName = "{v}"' in gradle for v in ('2.2.4','2.2.5','2.2.6','2.2.7','2.2.8','2.2.9','2.2.10','2.2.11','2.2.13','2.2.14')):
        lazy_insets += ['top = pageTopContentInset + coordinatedTransientOverlayInset','bottom = bottomNavigationContentInset','LocalPinnedPagerContentInset','activePagerVisual','coordinatedTransientOverlayInset']
    else:
        lazy_insets += ['headerContentInset + PageChromeSeparation + transientOverlayContentInset','bottomNavigationContentInset + navigationPadding.calculateBottomPadding()']
    for token in lazy_insets:
        require(token in lazy,'page chrome content-inset/backdrop drift: '+token)

    # Long-press/TV/right-click evidence actions and accent-bordered dialogs.
    cap=function_slice(main,'private fun CapabilityKeyValue(key: String, value: String)')
    for token in ['onLongPress = { showActions = true }','delay(550L)','event.buttons.isSecondaryPressed','DropdownMenu(expanded = showQuickMenu','Copy name + value','Share evidence','Add to watched evidence','Open in Encyclopedia','More details','CustomAccessibilityAction("Evidence actions")','consumeDesktopSecondaryMouseInput(suppressDesktopQuickMenu)','isChromeOsRuntime(context)','isAndroidPcFormFactor(context)','hasFreeformWindowManagement(context)']:
        require(token in cap,'evidence action parity missing: '+token)
    evidence=function_slice(main,'private fun EvidenceCopyDialog(')
    require('ExpressiveDetailDialog("Evidence provenance", onDismiss)' in evidence and 'border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.46f))' in main,'scrollable evidence dialog accent border missing')
    for token in ['Evidence provenance','Evidence class','Query/API path','Registry relationship','Add to watched evidence','Open in Encyclopedia']:
        require(token in evidence,'evidence provenance dialog parity missing: '+token)
    for m in re.finditer(r'AlertDialog\(',main):
        head=main[m.start():m.start()+500]
        require('modifier = Modifier.border(' in head,'AlertDialog without accent border near offset '+str(m.start()))
    require('border = androidx.compose.foundation.BorderStroke(1.dp, BrandSoft.copy(alpha = 0.34f))' in main,'custom dialog accent border treatment missing')

    # Analysis workspace parity: labels alone are insufficient; each shared tool must have a real branch.
    for branch in ['Requirements','Minimums','Graph','Presentation','Raw JSON','Database','History','Watched','Quality','Share','Tests']:
        require(f'\"{branch}\" ->' in main,'Analysis implementation branch missing: '+branch)
    for token in ['OpenGLESDependencyGraph(visualNodes','OPENGL_ES_32_MINIMUMS.map','evaluateCustomMinimumRules(report, minimumProfileRules)','Raw structured technicalReport','Presentation evidence']:
        require(token in main,'Analysis shared implementation parity missing: '+token)

    # Database result/submission shape parity.
    for token in ['private fun DatabaseSubmittedAt(raw: String)','ExpressiveInfoPill("Date", display.date','ExpressiveInfoPill("Time", display.time','Device local time · ${display.zone}','var submissionSuccessId by remember','Text("Report ID"','Copy report ID','ExpressiveContainedIconTextButton("Open report"','DatabaseSubmittedAt(row.submittedAt)','Compare this report','var databaseLookupMode by rememberSaveable','var databaseListQuery by rememberSaveable','ExpressiveFilterBar(listOf("Database list", "Report ID")','Filter loaded GPU, device, API or report ID…','databaseLookupMode = 1']:
        require(token in main,'Database presentation parity missing: '+token)

    # Generic user-facing components must apply official API display marks while raw registry/report tokens stay untouched.
    for token in ['Text(trademarkApiDisplayText(label)','Text(trademarkApiDisplayText(title)','Text(trademarkApiDisplayText(subtitle)','metric("OpenGL® ES™"' if 'releaseVersionName = "2.2.14"' in gradle else 'metric("OpenGL® ES"','metric("EGL™"']:
        require(token in main,'official API display-mark propagation missing: '+token)
    require('title.equals("Session History", true) || title.equals("Local session history", true) -> R.drawable.ic_history' in main,'Local session history semantic icon drift')

    # No capability/report correctness regression is permitted in a UI parity release.
    require('TECHNICAL_REPORT_SCHEMA_VERSION = 5' in main and 'SUBMISSION_SCHEMA_VERSION = 2' in main,'report schema drift')
    for stale in ['GL_EXT_fragment_shading_rate_attachment']:
        require(stale not in native,'fabricated registry name returned: '+stale)
    require('std::binary_search(extensions.begin(), extensions.end(), std::string(name))' in native,'extension membership performance regression')

if __name__=='__main__': main_guard('verify_2_2_0_ui_parity',verify)
