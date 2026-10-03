#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import re

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text(encoding='utf-8')
    require(any(x in gradle for x in ['releaseVersionName = "1.5.0"','releaseVersionName = "1.5.1"','releaseVersionName = "1.6.0"']) and any(x in gradle for x in ['releaseVersionCode = 1500','releaseVersionCode = 1501','releaseVersionCode = 1600']),'1.5+/1.6 release identity missing')
    require('SUBMISSION_SCHEMA_VERSION = 2' in main and 'TECHNICAL_REPORT_SCHEMA_VERSION = 4' in main,'schema constants missing')
    require('"$SUBMISSION_SCHEMA_VERSION · technical report $TECHNICAL_REPORT_SCHEMA_VERSION"' in main,'Info schema label is not bound to schema constants')
    require('technical report 2' not in main,'stale technicalReport schema label remains')
    egl_runtime_surfaces={
      'surfaceMipmapTexture': ['mipmapTexture=${r.eglRuntime.surfaceMipmapTexture', '"Pbuffer mipmap texture" to (r.eglRuntime.surfaceMipmapTexture'],
      'surfaceMipmapLevel': ['mipmapLevel=${r.eglRuntime.surfaceMipmapLevel', '"Pbuffer mipmap level" to (r.eglRuntime.surfaceMipmapLevel'],
      'surfaceMultisampleResolve': ['multisampleResolve=${r.eglRuntime.surfaceMultisampleResolve', '"Pbuffer multisample resolve" to (r.eglRuntime.surfaceMultisampleResolve'],
    }
    for token,needles in egl_runtime_surfaces.items():
        for needle in needles: require(needle in main,f'{token} missing report surface: {needle}')
    for token in ['recordableAndroid','framebufferTargetAndroid','colorComponentTypeExt','unavailableAttributes']:
        require(re.search(r'appendLine\("\$\{c\.id\}.*'+re.escape(token),main) is not None,f'TXT EGL config missing {token}')
    require('.filter { !it.optBoolean("draft", false) && !it.optBoolean("prerelease", false) }' in main,'stable updater must exclude draft/prerelease releases')
    require('GITHUB_API_VERSION = "2026-03-10"' in main and '.header("X-GitHub-Api-Version", GITHUB_API_VERSION)' in main,'current GitHub API contract missing')
    require('NO_REDIRECT_HTTP_CLIENT' in main and '.followRedirects(false)' in main and '.followSslRedirects(false)' in main,'no-redirect base client missing')
    require('UPDATE_METADATA_HTTP_CLIENT' in main and 'UPDATE_DOWNLOAD_HTTP_CLIENT' in main,'separate update clients missing')
    require('.followRedirects(true)' not in main and '.followSslRedirects(true)' not in main,'automatic redirects must not be enabled anywhere')
    for token in ['MAX_UPDATE_REDIRECTS = 3','isAllowedOfficialUpdateRedirect','release-assets.githubusercontent.com','objects.githubusercontent.com','redirectCount > MAX_UPDATE_REDIRECTS']:
        require(token in main,'manual bounded GitHub asset redirect control missing: '+token)
    require('Regex("^sha256:([0-9a-fA-F]{64})$")' in main and 'sha256FileHex(temp)' in main and 'does not match the official GitHub release asset digest' in main,'optional GitHub asset SHA-256 verification missing')
    require('packageSigningCertificatesMatch' in main,'APK signature identity verification missing')
    require('cleanupStaleUpdateArtifacts()' in main and 'endsWith(".part", true)' in main,'stale updater cache cleanup missing')
    require('Release 1.5.0' in rules,'1.5.0 rules section missing')

if __name__=='__main__': main_guard('verify_1_5_0_quality',verify)
