#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import re

def between(text,start,end):
    a=text.find(start); b=text.find(end,a+len(start))
    require(a>=0 and b>a,f'parity range missing: {start} .. {end}')
    return text[a:b]

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    require(any(x in gradle for x in ['releaseVersionName = "1.9.0"','releaseVersionName = "1.9.1"','releaseVersionName = "1.9.2"','releaseVersionName = "1.9.3"','releaseVersionName = "2.0.0"','releaseVersionName = "2.1.0"']) and any(x in gradle for x in ['releaseVersionCode = 1900','releaseVersionCode = 1901','releaseVersionCode = 1902','releaseVersionCode = 1903','releaseVersionCode = 2000','releaseVersionCode = 2100']),'1.9.0+ parity identity missing')

    # SAF is intentionally absent. VulkanScope-style in-app shared-storage browser owns all common imports/exports.
    for forbidden in ['ActivityResultContracts.OpenDocument','ActivityResultContracts.CreateDocument','rememberLauncherForActivityResult','ACTION_OPEN_DOCUMENT','ACTION_CREATE_DOCUMENT','MediaStore']:
        require(forbidden not in main,'SAF/provider flow returned: '+forbidden)
    require('android.permission.MANAGE_EXTERNAL_STORAGE' in manifest,'VulkanScope shared-storage permission contract missing')
    require('android.permission.WRITE_EXTERNAL_STORAGE' not in manifest,'legacy WRITE_EXTERNAL_STORAGE fallback must not return')
    for token in [
        'private enum class SharedStorageBrowserMode { IMPORT, EXPORT }',
        'private fun sharedStorageRoot(): File = Environment.getExternalStorageDirectory().canonicalFile',
        'children.size >= 4096',
        'file.absoluteFile.path != canonical.path',
        'private fun validatedSharedStorageImportFile(',
        'private fun validatedSharedStorageDestination(',
        'private fun atomicWriteSharedStorageFile(',
        'private fun SharedStoragePermissionActionButton(',
        'private fun SharedStorageBrowserDialog(',
        'QuestionDialogTitle("Overwrite existing file?")',
        'ExpressiveContainedIconTextButton("Replace", R.drawable.ic_save',
    ]: require(token in main,'shared-storage browser parity missing: '+token)
    for label in ['Import analysis snapshot','Export analysis snapshot','Import profile JSON','Export profile JSON','Export technicalReport JSON','Export TXT','Export HTML']:
        require(label in main,'common import/export route missing: '+label)

    # Every common button/dialog primitive uses the same VulkanScope Material3 geometry; OpenGLESScope keeps its own colors.
    for token in [
        'private fun ExpressiveActionButton(',
        'pressedShape = RoundedCornerShape(26.dp)',
        'private fun ExpressivePrimaryButton(',
        'private fun ExpressiveTextButton(',
        'private fun ExpressiveContainedIconTextButton(',
        'private fun ExpressivePrimaryIconTextButton(',
        'private fun ExpressiveCancelButton(',
        'private fun ExpressiveCloseButton(',
        'private fun ChevronAffordance(',
        'modifier = Modifier.size(48.dp).onFocusChanged',
        'shape = MaterialTheme.shapes.extraLarge',
    ]: require(token in main,'common VulkanScope button/dialog geometry missing: '+token)

    # Visible action-to-icon parity: icons must be attached to actions, not just exist in res/drawable.
    mappings=[
        ('Use as baseline', 'R.drawable.ic_baseline'),
        ('Add to watch list', 'R.drawable.ic_watch_add'),
        ('idleTrailingIcon = R.drawable.ic_add', 'R.drawable.ic_add'),
        ('Import profile JSON', 'R.drawable.ic_action_import'),
        ('Export profile JSON', 'R.drawable.ic_export'),
        ('Submit complete report', 'R.drawable.ic_database_submit'),
        ('idleTrailingIcon = R.drawable.ic_upload', 'R.drawable.ic_upload'),
        ('Copy all', 'R.drawable.ic_copy'),
    ]
    for label,icon in mappings:
        pos=main.find(label); require(pos>=0,'action label missing: '+label)
        window=main[max(0,pos-400):pos+900]
        require(icon in window or label.startswith('idleTrailingIcon'),f'expected icon {icon} not wired near {label}')
    require(re.search(r'SharedStoragePermissionActionButton\(\s*"Export technicalReport JSON"[^\n]*R\.drawable\.ic_export', main) is not None,
            'expected icon R.drawable.ic_export not wired to Export technicalReport JSON action')

    # The in-app browser body is structurally the VulkanScope generic browser, with only app name/color substitution.
    browser=between(main,'private fun SharedStorageBrowserDialog(','@Composable\nprivate fun SharedStorageFolderRow')
    for token in ['Search folders and files','File name','Reading shared storage…','Only ${request.allowedExtensions.joinToString { ".$it" }} files are shown','Choose a folder and name. Saving uses an atomic temporary write and asks before replacing an existing file.']:
        require(token in browser,'shared browser structure drift: '+token)

    # OpenGLESScope branding must remain OpenGLESScope, not VulkanScope red.
    for token in ['private val Brand = ComposeColor(0xFFBA2A8D)','private val BrandSoft = ComposeColor(0xFFF06BC7)','private val BrandContainer = ComposeColor(0xFF3A1831)']:
        require(token in main,'OpenGLESScope color identity drift: '+token)
    for forbidden in ['private val Brand = ComposeColor(0xFFA41E22)','private val BrandSoft = ComposeColor(0xFFE2676A)','private val BrandContainer = ComposeColor(0xFF351719)']:
        require(forbidden not in main,'VulkanScope red leaked into OpenGLESScope brand palette')

    # Section artwork keeps only VulkanScope-backed common compositions; OpenGL ES/EGL-only sections use actual single icons.
    for token in ['private fun SectionHeaderIcon(', 'SectionVectorBadgeIcon(R.drawable.ic_extensions, overlayIcon = R.drawable.ic_search)', 'SectionVectorBadgeIcon(R.drawable.ic_formats, overlayIcon = R.drawable.ic_search)']:
        require(token in main,'section-header artwork parity missing: '+token)
    for forbidden in ['SectionVectorBadgeIcon(R.drawable.ic_qr, overlayIcon = R.drawable.ic_share)', 'SectionVectorBadgeIcon(R.drawable.ic_graph, overlayIcon = R.drawable.ic_search)', 'SectionVectorBadgeIcon(R.drawable.ic_test, overlayIcon = R.drawable.ic_check)']:
        require(forbidden not in main,'invented section artwork returned: '+forbidden)

if __name__=='__main__': main_guard('verify_1_9_0_ui_storage_parity',verify)
