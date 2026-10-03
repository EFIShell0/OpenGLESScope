#!/usr/bin/env python3
from __future__ import annotations
import re
from gate_common import ROOT, require, main_guard

def function_block(text: str, marker: str, next_marker: str) -> str:
    a=text.index(marker); b=text.index(next_marker,a); return text[a:b]

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    service=(ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt').read_text(encoding='utf-8')
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    catalog=(ROOT/'app/src/main/java/com/efishell/openglesscope/RegistryCatalog.kt').read_text(encoding='utf-8')

    listing=function_block(main,'private fun listAnalysisHistory','private fun stableSnapshotFingerprint')
    require('loadAnalysisHistory(' not in listing and 'GZIPInputStream' not in listing and 'JSONObject(' not in listing,'history listing must not decompress/parse report bodies')
    require('.take(ANALYSIS_HISTORY_MAX_RECORDS)' in listing,'history listing record bound missing')
    require('f.name.startsWith(".session-")' in listing and 'it.delete()' in listing,'orphan history temp cleanup missing')
    require('ANALYSIS_HISTORY_MAX_RECORDS = 8' in main,'history record count bound drift')
    require('ANALYSIS_HISTORY_MAX_COMPRESSED_BYTES = 4 * 1024 * 1024' in main,'history compressed-size bound drift')
    require('ANALYSIS_MAX_SNAPSHOT_BYTES = 8 * 1024 * 1024' in main,'history uncompressed-size bound drift')
    require('MessageDigest.getInstance("SHA-256")' in main and 'stableSnapshotFingerprint(' in main,'collision-resistant history fingerprint missing')
    fingerprint=function_block(main,'private fun stableSnapshotFingerprint','private fun loadAnalysisHistory')
    require('.hashCode()' not in fingerprint,'32-bit hashCode must not be used as snapshot identity')
    require('withContext(Dispatchers.IO) { runCatching { loadAnalysisHistory(record.file) } }' in main,'history baseline load/decompression is not confined to IO dispatcher')
    require('LaunchedEffect(currentSnapshot)' in main and 'LaunchedEffect(currentSnapshot.toString())' not in main,'analysis snapshot effect key must not allocate a full JSON string on the UI thread')
    require('LaunchedEffect(historyRevision)' in main and 'history = withContext(Dispatchers.IO) { listAnalysisHistory(context) }' in main,'history directory/metadata IO must run on Dispatchers.IO')
    require('withContext(Dispatchers.IO) { record.file.delete() }' in main,'history delete filesystem IO must run on Dispatchers.IO')
    # Saving includes compression and filesystem work; every current UI call must execute it under IO.
    for m in re.finditer(r'saveAnalysisHistory\(',main):
        if m.start() < main.index('private fun listAnalysisHistory'): continue
        window=main[max(0,m.start()-220):m.start()+220]
        require('Dispatchers.IO' in window,'history save call is not executed under IO dispatcher')

    # Bounded singleton registry cache is acceptable; retaining an Android Context is not.
    require('object RegistryCatalog' in catalog,'registry catalog singleton contract missing')
    require(not re.search(r'\b(?:val|var)\s+\w*context\w*\s*:',catalog,re.I),'RegistryCatalog must not retain Context in a field')
    asset=ROOT/'app/src/main/assets/registry_catalog.json.gz'
    require(asset.is_file() and asset.stat().st_size < 2*1024*1024,'registry catalog compressed asset exceeds 2 MiB release memory/storage bound')
    require('5261' in (ROOT/'registry/registry_catalog_manifest.json').read_text(encoding='utf-8'),'registry catalog census drift')

    # Isolated process/service and native ownership must have deterministic release paths.
    for token in ['worker.shutdownNow()','mainHandler.removeCallbacksAndMessages(null)']:
        require(token in service,f'probe service lifecycle cleanup missing: {token}')
    require('releaseEgl(d, s, c);' in native,'native EGL terminal cleanup missing')
    require('eglDestroySurface' in native and 'eglDestroyContext' in native and 'eglTerminate' in native and 'eglReleaseThread' in native,'native EGL ownership teardown incomplete')
    require('class ActiveDiagnosticsScope' in native and 'ActiveDiagnosticsScope diagnosticsScope(diagnostics);' in native,'thread-local diagnostics ownership must use RAII lifetime guard')
    require('~ActiveDiagnosticsScope() { reset(); }' in native and 'diagnosticsScope.reset();' in native,'diagnostics scope reset/terminal teardown missing')
    require('kMaxDmaBufFormatCount = 128' in native and 'kMaxDmaBufModifierCountPerFormat = 256' in native and 'kMaxDmaBufModifierCountTotal = 4096' in native,'DMA-BUF query resource ceilings missing')
    require('kMaxEglCapabilityCount = 256' in native and 'eglCapabilities.size() >= kMaxEglCapabilityCount' in native and 'eglCapabilityOverflow = true' in native,'EGL capability collector ceiling/fail-closed overflow guard missing')

if __name__=='__main__': main_guard('verify_memory_resource_contracts_1_4_0',verify)
