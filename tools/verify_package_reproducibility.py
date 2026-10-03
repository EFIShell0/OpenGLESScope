#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
from pathlib import Path
import hashlib, os, tempfile, zipfile

EXCLUDED_PARTS={'.git','.gradle','build','__pycache__'}
def package_files(root: Path):
    out=[]
    for p in root.rglob('*'):
        if not p.is_file(): continue
        rel=p.relative_to(root)
        if any(part in EXCLUDED_PARTS for part in rel.parts): continue
        if p.suffix in {'.pyc','.pyo'}: continue
        out.append(rel.as_posix())
    return sorted(out)
def deterministic_zip(root: Path, dest: Path):
    with zipfile.ZipFile(dest,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
        for rel in package_files(root):
            info=zipfile.ZipInfo(rel,(2026,9,16,0,0,0)); info.compress_type=zipfile.ZIP_DEFLATED; info.external_attr=(0o755 if rel in {'gradlew'} or rel.startswith('tools/') else 0o644)<<16
            z.writestr(info,(root/rel).read_bytes(),compress_type=zipfile.ZIP_DEFLATED,compresslevel=9)
def verify():
    manifest=ROOT/'files.txt'; require(manifest.is_file(),'files.txt package manifest missing')
    expected=[x for x in manifest.read_text().splitlines() if x]
    actual=package_files(ROOT)
    require(expected==actual,'files.txt does not exactly match clean source package')
    require(len(expected)==len(set(expected)),'files.txt contains duplicates')
    with tempfile.TemporaryDirectory(prefix='ogles-repro-') as td:
        a=Path(td)/'a.zip'; b=Path(td)/'b.zip'; deterministic_zip(ROOT,a); deterministic_zip(ROOT,b)
        require(hashlib.sha256(a.read_bytes()).digest()==hashlib.sha256(b.read_bytes()).digest(),'deterministic ZIP reproduction failed')
if __name__=='__main__': main_guard('verify_package_reproducibility',verify)
