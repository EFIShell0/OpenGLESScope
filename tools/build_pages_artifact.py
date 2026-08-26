from pathlib import Path
import shutil
import sys

root=Path(__file__).resolve().parents[1]
out=(Path(sys.argv[1]) if len(sys.argv)>1 else root/'_site').resolve()
if out.exists(): shutil.rmtree(out)
out.mkdir(parents=True)
files=['.nojekyll','index.html','config.js','report.schema.json','400.html','401.html','403.html','404.html','405.html','408.html','409.html','413.html','415.html','429.html','500.html','502.html','503.html','504.html','error.html']
assets=['app.v075.js','site.v075.css','apple-touch-icon-v017.png','favicon-v017.ico','favicon-v017.png','egl-logo-v027.png','egl-logo-white-v028.png','egl-logo-white-v029.png','opengles-gl-es-v028.png','openglesscope_logo_horizontal-v017.png']
for group in ['gpu-vendors','hdr']:
    for p in sorted((root/'assets'/group).rglob('*')):
        if p.is_file(): assets.append(p.relative_to(root/'assets').as_posix())
for rel in files:
    src=root/rel
    if not src.is_file(): raise SystemExit(f'missing Pages source file: {rel}')
    dst=out/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
for rel in assets:
    src=root/'assets'/rel
    if not src.is_file(): raise SystemExit(f'missing Pages source asset: {rel}')
    dst=out/'assets'/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
(out/'data').mkdir(parents=True,exist_ok=True)
for src in sorted((root/'data').glob('*.json')):
    shutil.copy2(src,out/'data'/src.name)
print(f'OpenGLESScope Database Pages artifact staged: {out}')
