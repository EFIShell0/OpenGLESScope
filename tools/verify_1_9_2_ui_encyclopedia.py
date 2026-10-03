#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
from pathlib import Path
import gzip, json, re, hashlib

def function_block(text, name):
    marker='@Composable\nprivate fun '+name
    start=text.find(marker); require(start>=0,'function missing: '+name)
    m=re.search(r'\n@Composable\nprivate fun ', text[start+10:])
    end=start+10+m.start() if m else len(text)
    return text[start:end]

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    reg=(ROOT/'app/src/main/java/com/efishell/openglesscope/RegistryCatalog.kt').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    require((('releaseVersionName = "1.9.2"' in gradle and 'releaseVersionCode = 1902' in gradle) or ('releaseVersionName = "1.9.3"' in gradle and 'releaseVersionCode = 1903' in gradle) or ('releaseVersionName = "2.0.0"' in gradle and 'releaseVersionCode = 2000' in gradle) or ('releaseVersionName = "2.1.0"' in gradle and 'releaseVersionCode = 2100' in gradle)),'1.9.2+ identity missing')

    ref=(ROOT/'rules/VULKANSCOPE_1.4.3_MAINACTIVITY_REFERENCE.kt')
    if not ref.is_file():
        # The exact reference MainActivity is not shipped in the app ZIP; methodology reference is.
        # Use checked-in normalized golden body when present.
        golden=ROOT/'tests/golden/vulkanscope_1_4_3_single_filter_selector.txt'
        require(golden.is_file(),'VulkanScope exact filter golden missing')
        expected=golden.read_text(encoding='utf-8')
    else:
        expected=function_block(ref.read_text(encoding='utf-8'),'ExpressiveSingleFilterSelector')
    actual=function_block(main,'ExpressiveSingleFilterSelector')
    current210='releaseVersionName = "2.1.0"' in gradle
    if current210:
        require('pageSize = COLLECTION_PAGE_SIZE' in actual and 'private const val COLLECTION_PAGE_SIZE = 25' in main,'2.1.0 filter selector must use shared 25-row page size')
        require('commitFilterPage()' in actual and 'requestFilterPageChange' in actual and 'pageFieldFocused' in actual,'2.1.0 filter focus/commit behavior missing')
    else:
        require(actual==expected,'single-filter selector body drifted from exact adapted VulkanScope 1.4.3 golden')
    require('FilterChip(' not in main and 'FilterChipDefaults' not in main,'legacy FilterChip returned to production')
    for token in ['rememberFilterScrollBoundaryConnection()', 'TextFieldValue("1")', 'AnimatedContent(', 'filterPageTransition', 'Previous filter page', 'Next filter page', 'nestedScroll(boundaryScrollConnection)']:
        require(token in actual,'exact filter behavior missing: '+token)

    # Common drawable files must be exact VulkanScope copies; OpenGLESScope-only vector invention is forbidden.
    manifest=json.loads((ROOT/'tests/golden/1_9_0_common_icon_hashes.json').read_text())
    shared=manifest.get('sharedIcons',{})
    require(len(shared)==75,'expected 75 shared API-neutral drawable assets')
    for name, expected_hash in shared.items():
        p=ROOT/'app/src/main/res/drawable'/name; require(p.is_file(),'shared drawable missing: '+name)
        require(hashlib.sha256(p.read_bytes()).hexdigest()==expected_hash,'shared drawable hash drift: '+name)
    drawable_dir=ROOT/'app/src/main/res/drawable'
    shared_names=set(shared)
    extra_200={
        'ic_sort.xml':'4938255cc2ba9ceee462a9339cfcbe0053b4f8f4b8f6d3834ace97df4f825bb9',
        'ic_sort_created_newest.xml':'7ae6032654655407d480199abd80e04c2308065a42889741b1637879246b2e4b',
        'ic_sort_created_oldest.xml':'db68547aee7e943b4bca8eed8315da42335ddf40482b95107f33577e1132d6c2',
        'ic_sort_modified_newest.xml':'7ef4592cc2924c4c3a7e4115b6d81fdf0b8c860e8b38458c1f55e2ca1a94ecea',
        'ic_sort_modified_oldest.xml':'e9bcfdc165199b37cf1ed3665f13d08f6b75c5d46419e193d0ada36fbc7e0418',
        'ic_sort_name_asc.xml':'2f38ad65ce4ca76accc4d9d8739d76efe9d630a66d70f3e9c24d35e6a76fc0a9',
        'ic_sort_name_desc.xml':'2e77f1b201c215f7d9aac2b9482efa5b1d5292d434db5bb2de8678362a2f5d06',
        'ic_view_grid_dense.xml':'0b961bf3feea152804ed310190d60ce2b60d255fae0619d07128683f8aff23ce',
        'ic_view_tiles_large.xml':'bb5a56ad7bc98292930acf246b9aa06e910435dce60f7614a56690e0c56f67f0',
    }
    current200=('releaseVersionName = "2.0.0"' in gradle or 'releaseVersionName = "2.1.0"' in gradle)
    allowed=shared_names | (set(extra_200) if current200 else set())
    actual_names={p.name for p in drawable_dir.iterdir() if p.is_file()}
    require(actual_names==allowed,'invented/missing API-neutral drawable set: '+str(sorted(actual_names^allowed)))
    if current200:
        for name, expected_hash in extra_200.items():
            p=drawable_dir/name; require(hashlib.sha256(p.read_bytes()).hexdigest()==expected_hash,'VulkanScope 3.0.12 shared drawable drift: '+name)

    # Only reference-backed composite artwork remains. These were OpenGLESScope-only inventions and are forbidden.
    for forbidden in [
        'SectionVectorBadgeIcon(R.drawable.ic_qr, overlayIcon = R.drawable.ic_share)',
        'SectionVectorBadgeIcon(R.drawable.ic_graph, overlayIcon = R.drawable.ic_search)',
        'SectionVectorBadgeIcon(R.drawable.ic_test, overlayIcon = R.drawable.ic_check)',
        'SectionVectorBadgeIcon(R.drawable.ic_compare, overlayIcon = R.drawable.ic_evidence)',
        'SectionVectorBadgeIcon(R.drawable.ic_export, overlayIcon = R.drawable.ic_save)',
    ]: require(forbidden not in main,'invented section icon composition returned: '+forbidden)
    for token in [
        'SectionVectorBadgeIcon(R.drawable.ic_registry, "REG")',
        'SectionVectorBadgeIcon(R.drawable.ic_extensions, overlayIcon = R.drawable.ic_search)',
        'SectionVectorBadgeIcon(R.drawable.ic_formats, overlayIcon = R.drawable.ic_search)',
        'R.drawable.ic_action_database', 'R.drawable.ic_compare',
    ]: require(token in main,'reference-backed common section artwork missing: '+token)

    # Encyclopedia crash-safety and bounded-search contract.
    for token in [
        'private const val ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 250',
        'term in entry.searchText',
        'lowercase(java.util.Locale.ROOT)',
        '.take(ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT + 1)',
        'withContext(Dispatchers.IO) { runCatching { RegistryCatalog.load(context) } }',
    ]: require(token in main,'Encyclopedia UI/search guard missing: '+token)
    for token in [
        'MAX_CATALOG_BYTES = 2 * 1024 * 1024',
        'MAX_CATALOG_ENTRIES = 6_000',
        'val o = array.optJSONObject(i) ?: continue',
        'if (name.isEmpty() || kind.isEmpty() || api.isEmpty()) continue',
        'if (!seen.add("$api|$kind|$name")) continue',
        'val searchText = buildString',
        'require(out.isNotEmpty())',
    ]: require(token in reg,'Encyclopedia runtime decoder guard missing: '+token)
    # Top-level corruption still fails closed; individual malformed rows do not take down the whole surface.
    require('require(root.optString("schema") == "OpenGLESScopeRegistryCatalog1")' in reg,'catalog schema gate missing')
    require('require(name.isNotEmpty()' not in reg,'individual malformed row still terminates entire catalog')

    asset=ROOT/'app/src/main/assets/registry_catalog.json.gz'
    raw=gzip.decompress(asset.read_bytes())
    require(len(raw)<=2*1024*1024,'catalog exceeds runtime decompression bound')
    obj=json.loads(raw)
    require(obj.get('schema')=='OpenGLESScopeRegistryCatalog1','catalog schema drift')
    entries=obj.get('entries'); require(isinstance(entries,list) and len(entries)==5261,'catalog census drift')
    for sample in entries[:32]+entries[-32:]:
        require(isinstance(sample,dict) and sample.get('name') and sample.get('kind') and sample.get('api'),'generated catalog row malformed')

if __name__=='__main__': main_guard('verify_1_9_2_ui_encyclopedia',verify)
