from pathlib import Path
import subprocess
import sys
import tempfile

root=Path(__file__).resolve().parents[1]
python=sys.executable
audit=root/'tools/audit_database.py'
builder=root/'tools/build_pages_artifact.py'
repair=root/'tools/repair_repository.py'

def run(args,ok=True,contains=None):
    p=subprocess.run(args,cwd=root,text=True,capture_output=True)
    out=(p.stdout or '')+(p.stderr or '')
    if (p.returncode==0)!=ok: raise SystemExit(f'unexpected return code {p.returncode}: {out}')
    if contains and contains not in out: raise SystemExit(f'missing expected text {contains!r}: {out}')
    return out

out=run([python,str(audit),'--source-tree',str(root)])
if 'OpenGLESScope Database audit tool 0.7.5' not in out: raise SystemExit('audit version fingerprint missing')
run([python,str(repair),'--check'])
stale=root/'assets/app.v0000.js'
try:
    stale.write_text('stale',encoding='utf-8')
    run([python,str(audit),'--source-tree',str(root)],False,'exactly one versioned frontend app asset')
finally:
    stale.unlink(missing_ok=True)
stale_workflow=root/'.github/workflows/stale.yml'
try:
    stale_workflow.write_text('name: stale\n',encoding='utf-8')
    run([python,str(audit),'--source-tree',str(root)],False,'exactly one GitHub Actions workflow')
finally:
    stale_workflow.unlink(missing_ok=True)
with tempfile.TemporaryDirectory(prefix='openglesscope-pages-audit-') as tmp:
    site=Path(tmp)/'_site'
    stale.write_text('stale',encoding='utf-8')
    try:
        run([python,str(builder),str(site)])
        if (site/'assets/app.v0000.js').exists(): raise SystemExit('stale source asset leaked into Pages artifact')
    finally:
        stale.unlink(missing_ok=True)
    run([python,str(audit),'--artifact-tree',str(site)])
    (site/'.git').mkdir()
    (site/'.git/HEAD').write_text('x',encoding='utf-8')
    run([python,str(audit),'--artifact-tree',str(site)],False,'forbidden Pages artifact')
print('OpenGLESScope Database audit hygiene regression tests: ALL PASS')
