#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    app=(ROOT/'app/build.gradle.kts').read_text(); root=(ROOT/'build.gradle.kts').read_text(); wrapper=(ROOT/'gradle/wrapper/gradle-wrapper.properties').read_text(); manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text(); styles=(ROOT/'app/src/main/res/values/styles.xml').read_text()
    require('releaseVersionName = "3.0.7"' in app,'versionName != 3.0.7')
    require('releaseVersionCode = 3007' in app,'versionCode != 3007')
    require('compileSdk {' in app and 'version = release(37)' in app and 'minorApiLevel = 2' in app and 'targetSdk = 37' in app,'compileSdk 37.2 / manifest targetSdk 37 contract missing')
    require('minSdk = 31' in app,'minSdk 31 Android form-factor contract drift')
    require('ndkVersion = "30.0.16248370"' in app,'NDK r30 LTS lock drift')
    require('version "9.4.1"' in root,'AGP 9.4.1 lock missing')
    require('version "2.4.20"' in root,'Kotlin 2.4.20 lock missing')
    require('gradle-9.7.1-bin.zip' in wrapper,'Gradle 9.7.1 wrapper lock missing')
    for dep in [
        'androidx.core:core-ktx:1.19.1','androidx.core:core-splashscreen:1.2.0','androidx.activity:activity-compose:1.13.0',
        'androidx.compose.ui:ui:1.12.1','androidx.compose.foundation:foundation:1.12.1','androidx.compose.animation:animation:1.12.1',
        'androidx.compose.material3:material3:1.5.0-alpha28','androidx.lifecycle:lifecycle-runtime-compose:2.11.0',
        'androidx.lifecycle:lifecycle-runtime-ktx:2.11.0','com.squareup.okhttp3:okhttp:5.5.0','com.google.zxing:core:3.5.4']:
        require(dep in app,'dependency lock missing: '+dep)
    for field in ['AGP_VERSION','KOTLIN_VERSION','GRADLE_VERSION','NDK_VERSION','CORE_KTX_VERSION','SPLASHSCREEN_VERSION','ACTIVITY_COMPOSE_VERSION','COMPOSE_VERSION','MATERIAL3_VERSION','LIFECYCLE_VERSION','OKHTTP_VERSION','ZXING_VERSION','COMPILE_SDK_LEVEL','COMPILE_SDK_MINOR_LEVEL','MIN_SDK_LEVEL','TARGET_SDK_LEVEL']:
        require(f'"{field}"' in app,'BuildConfig build/toolchain field missing: '+field)
    require('android.hardware.type.pc' in manifest and 'android.software.leanback' in manifest and 'android.hardware.touchscreen' in manifest,'Android form-factor feature parity drift')
    require('android:glEsVersion="0x00020000" android:required="true"' in manifest,'OpenGL ES required feature drift')
    require('android:theme="@style/AppTheme.Starting"' in manifest,'platform splash activity theme missing')
    for token in ['Theme.SplashScreen','windowSplashScreenAnimatedIcon','@drawable/openglesscope_logo_foreground','windowSplashScreenAnimationDuration','postSplashScreenTheme']:
        require(token in styles,'platform splash style contract missing: '+token)
    for abi in ['arm64-v8a','armeabi-v7a','x86_64']: require(abi in app,f'ABI missing: {abi}')
    require('include("x86")' not in app,'legacy x86 ABI must remain unsupported')
    cmake=(ROOT/'app/src/main/cpp/CMakeLists.txt').read_text()
    for token in ['-Wall','-Wextra','-Werror','-fstack-protector-strong','-fvisibility=hidden','-Wl,-z,relro','-Wl,-z,now','-Wl,-z,max-page-size=16384']:
        require(token in cmake,f'native hardening/build token missing: {token}')

if __name__=='__main__': main_guard('verify_build_contract',verify)
