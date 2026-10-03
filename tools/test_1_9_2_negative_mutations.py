#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
import tempfile, shutil, subprocess, sys

def run_verify(root):
    cp=subprocess.run([sys.executable,'-B',str(root/'tools/verify_1_9_2_ui_encyclopedia.py')],cwd=root,text=True,capture_output=True)
    return cp.returncode

def verify():
    mutations=[
        ('filterchip', 'private fun ExpressiveSingleFilterSelector(', 'private fun ExpressiveSingleFilterSelector('),
        ('limit', 'private const val ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 250', 'private const val ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 1000'),
        ('malformed-row', 'val o = array.optJSONObject(i) ?: continue', 'val o = array.getJSONObject(i)'),
    ]
    for name,needle,repl in mutations:
        with tempfile.TemporaryDirectory(prefix='ogles192-'+name+'-') as td:
            dst=shutil.copytree(ROOT,td+'/tree',dirs_exist_ok=True); dst=__import__('pathlib').Path(dst)
            target=dst/'app/src/main/java/com/efishell/openglesscope'/('RegistryCatalog.kt' if name=='malformed-row' else 'MainActivity.kt')
            text=target.read_text(); require(needle in text,'mutation fixture missing: '+name)
            if name=='filterchip':
                text=text.replace(needle,'FilterChip(\n    selected = true, onClick = {}\n)\n\n'+needle,1)
            else: text=text.replace(needle,repl,1)
            target.write_text(text)
            require(run_verify(dst)!=0,'negative mutation was not rejected: '+name)
if __name__=='__main__': main_guard('test_1_9_2_negative_mutations',verify)
