#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    cmake=(ROOT/'app/src/main/cpp/CMakeLists.txt').read_text(); main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(); service=(ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeService.kt').read_text()
    for token in ['-Wall','-Wextra','-Werror','-fstack-protector-strong','-Wl,-z,relro','-Wl,-z,now']:
        require(token in cmake,f'native compile/link hardening missing: {token}')
    require('GLESv3' in cmake and 'EGL' in cmake,'native GLES/EGL link contract missing')
    # Compile-sensitive source guards for code added/retained by this release.
    require('import androidx.compose.foundation.layout.' in main,'Compose layout imports missing')
    require(main.count('private fun ExpressiveMetricGrid(metrics: List<Pair<String, String>>, modifier: Modifier = Modifier)') == 1, 'duplicate ExpressiveMetricGrid Kotlin overload ambiguity')
    require('rememberSaveable' in main,'rememberSaveable compile/use contract missing')
    require('OpenGLESProbeContract.isCompleteAvailableReport(o)' in main,'shared report contract callsite missing')
    require('OpenGLESProbeContract.isTerminalJson' in service,'service contract callsite missing')
    require('InternalFormatEntry(' in main and 'NvSampleProperty(' in main,'internal-format Kotlin model missing')
    require('Regex("\\\\d{4}-\\\\d{2}-\\\\d{2}")' in main,'security-patch Kotlin regex escape regressed')
    require('Regex("\\d{4}-\\d{2}-\\d{2}")' not in main,'invalid Kotlin regex escape present')
if __name__=='__main__': main_guard('verify_compile_regressions',verify)
