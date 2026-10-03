#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import os,re,shutil,subprocess,tempfile

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    gradle=(ROOT/'app/build.gradle.kts').read_text(encoding='utf-8')
    require(any((f'releaseVersionName = "{v}"' in gradle and f'releaseVersionCode = {code}' in gradle) for v,code in [('1.9.1',1901),('1.9.2',1902),('1.9.3',1903),('2.0.0',2000),('2.1.0',2100)]),'1.9.1+ identity missing')
    require('import androidx.compose.ui.semantics.Role' in main,'Role type import missing')
    require('import androidx.compose.ui.semantics.role' in main,'semantics role extension import missing')
    decl='val collecting = collectionStatus == CollectionStatus.COLLECTING'
    require(decl in main,'collecting derived state declaration missing')
    require(main.count('ConnectivityStatusHost(collecting,')==1,'connectivity collecting binding drift')
    require(main.count('if (collecting) LoadingView()')==1,'loading collecting binding drift')
    require(main.count('collectionReady = !collecting && current.available')==1,'page collectionReady binding drift')
    require('modifier = Modifier.semantics { role = Role.Button }' in main,'button semantics role binding drift')
    # Parser/name-resolution probe without Android classpath: Android/Compose refs are expected unresolved,
    # but the locally declared collecting symbol must never be unresolved again.
    kotlinc=None if os.environ.get('OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE')=='1' else shutil.which('kotlinc')
    if kotlinc:
        with tempfile.TemporaryDirectory(prefix='ogles191-kotlinc-') as td:
            cp=subprocess.run([kotlinc,str(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt'),'-d',os.path.join(td,'out.jar')],text=True,capture_output=True)
            out=(cp.stdout or '')+'\n'+(cp.stderr or '')
            require(re.search(r'unresolved reference:\s*collecting',out,re.I) is None,'collecting remains unresolved in Kotlin probe')
            for pattern in [r'Syntax error:',r'Unterminated',r'Unexpected tokens']:
                require(re.search(pattern,out,re.I) is None,'Kotlin parser probe failed: '+pattern)
if __name__=='__main__': main_guard('verify_1_9_1_compile_hotfix',verify)
