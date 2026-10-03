#!/usr/bin/env python3
from pathlib import Path
from gate_common import ROOT, require, main_guard

def verify():
    forbidden_names={'.gradle','.idea','build','__pycache__','screenshots'}
    bad=[]
    for p in ROOT.rglob('*'):
        rel=p.relative_to(ROOT)
        if any(part in forbidden_names for part in rel.parts): bad.append(str(rel))
        if p.is_file() and p.suffix in {'.pyc','.pyo'}: bad.append(str(rel))
    require(not bad,'transient/forbidden package content: '+', '.join(bad[:20]))
    require(not (ROOT/'release.md').exists(),'root release.md is forbidden by project packaging contract')
    require(not any(p.is_file() and p.name.lower()=='readme.md' for p in ROOT.rglob('*')),'README.md is forbidden by current OpenGLESScope package contract')
    for required in ['rules/PROJECT_RULES.md','LICENSE','BUILD_AUDIT.md','CAPABILITY_COVERAGE_AUDIT.md','PUBLIC_CAPABILITY_REFERENCE_AUDIT.md','PUBLIC_CAPABILITY_REFERENCE_MATRIX.csv','registry/gl.xml','registry/gl_registry_manifest.json']:
        require((ROOT/required).is_file(),f'required package evidence missing: {required}')

if __name__=='__main__': main_guard('verify_package_hygiene',verify)
