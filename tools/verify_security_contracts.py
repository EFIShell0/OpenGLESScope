#!/usr/bin/env python3
import re
from gate_common import ROOT, require, main_guard

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text()
    paths=(ROOT/'app/src/main/res/xml/file_paths.xml').read_text()
    require('android:usesCleartextTraffic="false"' in manifest,'cleartext traffic must be disabled')
    require('android:allowBackup="false"' in manifest,'backup must remain disabled')
    require('android:exported="false"' in manifest and 'android:process=":opengles_probe"' in manifest,'probe service must remain isolated/non-exported')
    require('android:exported="false"' in manifest and 'FileProvider' in manifest,'FileProvider must remain non-exported')
    require('path="updates/"' in paths and '<root-path' not in paths and 'path="."' not in paths,'FileProvider scope must remain updates-only')
    require('https://openglesscope-database-api.openglesscope.workers.dev' in main,'fixed official database origin missing')
    require('NO_REDIRECT_HTTP_CLIENT' in main and '.followRedirects(false)' in main and '.followSslRedirects(false)' in main,'shared network clients must disable automatic redirects')
    require('.followRedirects(true)' not in main and '.followSslRedirects(true)' not in main,'automatic redirects must not be re-enabled')
    require('base.scheme != "https"' in main and 'base.host != "openglesscope-database-api.openglesscope.workers.dev"' in main,'database endpoint origin validation missing')
    require('2 * 1024 * 1024' in main and '64 * 1024' in main,'database payload/response ceilings missing')
    require('NET_CAPABILITY_INTERNET' in main and 'NET_CAPABILITY_VALIDATED' in main,'validated internet gate missing')
    require('parsedUrl.scheme != "https"' in main and 'parsedUrl.host != "github.com"' in main and '/EFIShell0/OpenGLESScope/releases/download/' in main,'update asset origin confinement missing')
    require('256L * 1024L * 1024L' in main,'update package download ceiling missing')
    require('packageSigningCertificatesMatch' in main and 'archive.packageName != packageName' in main,'APK identity/signature verification missing')
    require('archiveCode <= installedCode' in main,'update versionCode monotonicity guard missing')
    require(re.search(r'if\s*\(archiveVersion\s*!=\s*update\.version\)\s*error\(', main) is not None,'downloaded APK versionName must exactly match selected release')
    require('GITHUB_API_VERSION = "2026-03-10"' in main and 'header("X-GitHub-Api-Version", GITHUB_API_VERSION)' in main,'GitHub API request contract missing')
    require('!it.optBoolean("prerelease", false)' in main,'stable updater must reject prereleases per OpenGLESScope release security contract')
    require('MAX_UPDATE_REDIRECTS = 3' in main and 'isAllowedOfficialUpdateRedirect' in main,'manual bounded update redirect validation missing')
    require('sha256FileHex(temp)' in main and 'packageSigningCertificatesMatch' in main,'update digest/signing verification missing')
    require('Content-Security-Policy' in main and "default-src 'none'" in main,'exported HTML CSP missing')

if __name__=='__main__': main_guard('verify_security_contracts',verify)
