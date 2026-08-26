from pathlib import Path
import argparse
import json
import re
import shutil
import subprocess
import sys
from urllib.parse import urlsplit

parser=argparse.ArgumentParser(description='Audit OpenGLESScope Database source or staged Pages artifact')
parser.add_argument('--source-tree',type=Path)
parser.add_argument('--artifact-tree',type=Path)
parser.add_argument('--version',action='store_true')
args=parser.parse_args()
AUDIT_VERSION='0.7.5'
print(f'OpenGLESScope Database audit tool {AUDIT_VERSION}')
if args.version: sys.exit(0)

public_files={'.nojekyll','index.html','config.js','report.schema.json','400.html','401.html','403.html','404.html','405.html','408.html','409.html','413.html','415.html','429.html','500.html','502.html','503.html','504.html','error.html'}
public_assets={'app.v075.js','site.v075.css','apple-touch-icon-v017.png','favicon-v017.ico','favicon-v017.png','egl-logo-v027.png','egl-logo-white-v028.png','egl-logo-white-v029.png','opengles-gl-es-v028.png','openglesscope_logo_horizontal-v017.png','gpu-vendors/gpu_vendor_amd.png','gpu-vendors/gpu_vendor_arm.png','gpu-vendors/gpu_vendor_broadcom.png','gpu-vendors/gpu_vendor_huawei.png','gpu-vendors/gpu_vendor_imagination.png','gpu-vendors/gpu_vendor_intel.png','gpu-vendors/gpu_vendor_nvidia.png','gpu-vendors/gpu_vendor_qualcomm.png','gpu-vendors/gpu_vendor_samsung.png','gpu-vendors/gpu_vendor_unknown.png','gpu-vendors/gpu_vendor_vivante.png','gpu-vendors/gpu_vendor_vsi.png','hdr/dolby_vision.png','hdr/dolby_vision_2.png','hdr/hdr10.svg','hdr/hdr10_plus.png','hdr/hdr10_plus_advanced.png','hdr/hdr_vivid.webp'}

def local_ref_errors(root):
    out=[]
    pattern=re.compile(r'(?:href|src)=["\']([^"\']+)["\']',re.I)
    for html in root.glob('*.html'):
        body=html.read_text(encoding='utf-8')
        for ref in pattern.findall(body):
            if ref.startswith(('http://','https://','data:','#','mailto:','javascript:')): continue
            clean=urlsplit(ref).path
            if not clean or clean in {'.','./','/','/OpenGLESScope_database/'} or clean.endswith('/'): continue
            if clean.startswith('/OpenGLESScope_database/'):
                target=(root/clean[len('/OpenGLESScope_database/'):]).resolve()
            else:
                target=(html.parent/clean).resolve()
            try: target.relative_to(root.resolve())
            except ValueError:
                out.append(f'local asset escapes tree {html.name}: {ref}')
                continue
            if not target.is_file(): out.append(f'broken local asset {html.name}: {ref}')
    return out

def audit_artifact(root):
    root=root.resolve(); errors=[]
    if not root.is_dir(): errors.append(f'artifact tree missing: {root}')
    if errors:
        print('\n'.join(errors)); sys.exit(1)
    top={p.name for p in root.iterdir()}
    allowed=public_files|{'assets','data'}
    for x in sorted(top-allowed): errors.append(f'forbidden Pages artifact top-level entry {x}')
    for x in public_files: 
        if not (root/x).is_file(): errors.append(f'missing Pages artifact entry {x}')
    assets=root/'assets'
    data=root/'data'
    if not assets.is_dir(): errors.append('missing Pages artifact entry assets')
    if not data.is_dir(): errors.append('missing Pages artifact entry data')
    forbidden={'.git','.github','worker','tools','rules','.gradle','build','__pycache__','.idea','node_modules','.wrangler'}
    for p in root.rglob('*'):
        rel=p.relative_to(root)
        if any(part in forbidden for part in rel.parts): errors.append(f'forbidden Pages artifact {rel}')
        if rel.as_posix()!='.nojekyll' and any(part.startswith('.') for part in rel.parts): errors.append(f'forbidden hidden Pages artifact {rel}')
        if p.is_symlink(): errors.append(f'symlink not permitted in Pages artifact {rel}')
        if p.is_file() and rel.parts and rel.parts[0]=='assets':
            asset_rel=Path(*rel.parts[1:]).as_posix()
            if asset_rel not in public_assets: errors.append(f'unexpected/stale Pages asset {rel}')
        if p.is_file() and rel.parts and rel.parts[0]=='data' and p.suffix.lower()!='.json': errors.append(f'non-JSON Pages data {rel}')
    idx=root/'index.html'
    if idx.is_file():
        body=idx.read_text(encoding='utf-8')
        for token in ['OpenGLESScope Database <strong>0.7.5</strong>','site.v075.css','app.v075.js','config.js?v=075']:
            if token not in body: errors.append(f'Pages artifact current reference missing {token}')
    errors.extend(local_ref_errors(root))
    if errors:
        print('\n'.join(errors)); sys.exit(1)
    print('OpenGLESScope Database 0.7.5 Pages artifact audit: PASS')
    sys.exit(0)

if args.artifact_tree: audit_artifact(args.artifact_tree)
root=(args.source_tree or Path(__file__).resolve().parents[1]).resolve()
errors=[]
def check(cond,msg):
    if not cond: errors.append(msg)
def read(rel): return (root/rel).read_text(encoding='utf-8')

for rel in ['index.html','assets/app.v075.js','assets/site.v075.css','worker/src/index.js','worker/package.json','worker/wrangler.jsonc','report.schema.json','data/index.json','rules/PROJECT_RULES.md','tools/pages.workflow.yml','.github/workflows/pages.yml','tools/test_statistics_filters.mjs','tools/test_ui_parity.mjs']:
    check((root/rel).is_file(),f'missing source file {rel}')
if errors:
    print('\n'.join(errors)); sys.exit(1)
index=read('index.html'); app=read('assets/app.v075.js'); css=read('assets/site.v075.css'); worker=read('worker/src/index.js'); rules=read('rules/PROJECT_RULES.md'); workflow=read('.github/workflows/pages.yml'); template=read('tools/pages.workflow.yml'); build_index=read('tools/build_index.py')
workflow_dir=root/'.github/workflows'
workflows=sorted(p.name for p in workflow_dir.iterdir() if p.is_file() and p.suffix.lower() in {'.yml','.yaml'})
check(workflows==['pages.yml'],f'exactly one GitHub Actions workflow is permitted; remove stale workflows: {workflows}')
check(workflow==template,'pages.yml must exactly match tools/pages.workflow.yml; run python tools/repair_repository.py --apply')
check('OpenGLESScope Database <strong>0.7.5</strong>' in index,'index version')
check('site.v075.css' in index and 'app.v075.js' in index and 'config.js?v=075' in index,'0.7.5 cache-busted asset refs')
check("connect-src 'self' https://openglesscope-database-api.openglesscope.workers.dev" in index,'CSP API pin')
check('Common evidence only' in app and 'Cross-producer comparison' in app,'compare producer/common-evidence controls')
for token in ['Common fields','One-sided fields','Visible differences','Visible sections','Unknown / Not reported','av!==bv||ac!==bc','commonOnly?commonKeys:technicalUniverse']:
    check(token in app,f'compare contract token {token}')
check("parts.length===2||parts.length===3" in app and "#reports/${id}/${DETAIL_ROUTE" in app,'canonical report hash routes')
check("#compare/${a}/${b}" in app,'canonical compare hash route')
check('#statistics' in app and "parts[0]==='trends'" in app,'statistics canonical route and legacy alias')
check('Submission statistics' in app and 'not device-population or market-share estimates' in app,'statistics evidence-scope disclaimer')
for token in ['statisticsSliceLimit','statisticsExtensionScope','statisticsExtensionNamespace','statisticsExtensionMinShare','statisticsExtensionSearch','data-stat-filter','Extension enumeration ranking','Submission timeline']:
    check(token in app,f'statistics/filter contract token {token}')
for token in ['eglVersion','driverMode','driverVersion','android','abi','appVersion','extensionToken','clearFilters']:
    check(token in app or token in index,f'cohort-filter token {token}')
check('displayOnly=state.view' in app and 'state.android' in app,'Display/HDR filter isolation contract')
for token in ['Share report','Copy permalink','Share comparison link','routeReportUrl','routeCompareUrl','hashchange','popstate']:
    check(token in app,f'routing/share token {token}')
check('compareSection' in app and 'compareFieldSearch' in app,'Compare section and field filtering')
check('id="compareFilters" class="subfilters"' in app and 'subfilter-control' in app,'Compare secondary filter row hierarchy')
check('compare-diff-input' in css and 'input:not(.compare-diff-input)' in css and '#contentView[data-main-view="compare"] .compare-picker' in css,'Compare checkbox style isolation and compact scoped layout')
check('compare-share-button' in app and 'compare-share-button' in css,'Compare share action separated from picker controls')
for token in ['#contentView[data-main-view="compare"] .compare-picker{align-items:center;gap:7px;margin:0 0 12px;max-width:760px}','#compareFilters{margin:0 0 12px;max-width:760px;width:100%}','.subfilters{display:flex;gap:8px;flex-wrap:wrap;align-items:flex-end;margin:0 0 14px;width:auto}','.brand img{width:min(226px,36vw)}','.nav-shell{margin-left:34px;gap:6px}','.hero-heading-row{gap:28px}','.cards{grid-template-columns:repeat(auto-fit,minmax(290px,1fr))}','.filter-clear-button[hidden],#clearFilters[hidden]{display:none!important}']:
    check(token in css,f'shared UI parity token {token}')
check('<div id="compareFilters" class="subfilters"></div>' in app and "filterHost.innerHTML=selectControl('compareSection','Section'" in app,'Compare dynamic subfilter hierarchy')
check("clear.hidden=!(active||String(state.query||'').trim())" in app,'inactive Clear filters must stay hidden')
for token in ['extensionRowSearch','limitRowSearch','formatRowSearch','precisionRowSearch']:
    check(token in app,f'view-scoped search token {token}')
for token in ['deviceModelFilter','submissionAgeFilter','resolutionFilter','refreshRateFilter','wideColorFilter','hdrStateFilter','hdrTypeFilter']:
    check(token in index,f'extended cohort/display filter {token}')

check('.distribution-grid{' in css and '.donut-chart{' in css and '.chart-filter-button{' in css,'first-party statistics chart styles')
check('https://' not in css and '@import' not in css,'CSS must not load remote chart/font resources')

check('.notice{' in css and '.notice strong{' in css,'cross-producer notice style')
check("const DATABASE_VERSION='0.7.5'" in worker,'worker database version')
check("currentProducer:'OpenGLESScope 0.7.2'" in worker,'worker current producer')
check("p.application.version==='0.7.2'&&p.application.versionCode!==702" in worker,'worker current producer versionCode gate')
check('MAX_BODY=2*1024*1024' in worker and 'MAX_REPORT_TEXT=2*1024*1024' in worker,'worker body/report bounds')
check("schemaVersion:2" in worker and "technicalReportSchema:2" in worker,'worker schema contract')
check('Unsupported OpenGLESScope producer version' in worker,'worker producer floor diagnostics')
check('TECH_KEYS_V2' in worker and 'EGL_RUNTIME_KEYS' in worker and 'validEglRuntime' in worker,'worker technical report 2 EGL runtime validation')
check('recordableAndroid' in worker and 'framebufferTargetAndroid' in worker and 'colorComponentTypeExt' in worker,'worker EGL config extension validation')
check('OpenGLESScope 0.1.17 through 0.7.2' in worker,'bounded producer compatibility ceiling')
check('EGL runtime' in app and 'recordableAndroid' in app and 'unavailableAttributes' in app,'frontend EGL runtime/config detail coverage')
check(not any(x in app for x in ['0x5143','0x13B5','0x10DE','0x8086','0x1002','0x1010','0x14E4','0x19E5']),'frontend must not fabricate PCI/Vulkan-style vendor ids')
check('hasSensitive' in worker and 'stable(p)' in worker and 'sha(canonical)' in worker,'worker sensitive/canonical hash handling')
check('2026-08-26' in worker,'worker registry audit date')
check('## Release 0.7.4 full shared presentation parity and release-gate hardening' in rules,'rules 0.7.4 section')
check((root/'rules/0.7.4_FULL_SHARED_PRESENTATION_PARITY_AUDIT.md').is_file(),'0.7.4 audit rule file')
check('## Release 0.7.5 current EGL binding evidence compatibility' in rules,'rules 0.7.5 section')
check((root/'rules/0.7.5_CURRENT_EGL_BINDING_EVIDENCE_COMPATIBILITY_AUDIT.md').is_file(),'0.7.5 audit rule file')
check("dm.get('EGL current bindings').status!==(bindingsCurrent?'Available':'Unavailable')" in worker,'worker EGL binding diagnostic consistency gate')
check('Current EGL bindings: context=${e.currentContext}, display=${e.currentDisplay}, draw=${e.currentDrawSurface}, read=${e.currentReadSurface}' in worker,'worker EGL binding TXT consistency gate')
check('APPLICATION_KEYS_V2' in worker and 'applicationAbi' in worker and 'supportedDeviceAbis' in worker,'worker 0.7.2 application ABI metadata contract')
check('GL_NUM_WINDOW_RECTANGLES_EXT' not in worker and 'GL_MAX_SHADER_COMPILER_THREADS_KHR' not in worker,'worker must not require non-capability state/control queries')
check('README.md files are forbidden' in rules,'README archive policy')
check(not any(p.is_file() and p.name.lower()=='readme.md' for p in root.rglob('*')),'README.md must be absent from source release')
forbidden_product=('caps'+'viewer').lower()
check(not any(forbidden_product in p.read_text(encoding='utf-8',errors='ignore').lower() for p in root.rglob('*') if p.is_file()),'forbidden third-party product name content')
check(not any(forbidden_product in str(p.relative_to(root)).lower() for p in root.rglob('*')),'forbidden third-party product name filename')
check(not any(p.is_dir() and p.name.lower()=='fastlane' for p in root.rglob('*')),'packaged store metadata forbidden')
check(not (root/'release.md').exists(),'root release.md forbidden')
app_assets=sorted(p.name for p in (root/'assets').glob('app.v*.js'))
css_assets=sorted(p.name for p in (root/'assets').glob('site.v*.css'))
check(app_assets==['app.v075.js'],f'exactly one versioned frontend app asset is permitted: {app_assets}')
check(css_assets==['site.v075.css'],f'exactly one versioned frontend css asset is permitted: {css_assets}')
static=json.loads(read('data/index.json'))
check(static.get('databaseVersion')=='0.7.5','static databaseVersion')
check(static.get('normalizerVersion')==10,'static normalizerVersion')
check(static.get('currentProducer')=='OpenGLESScope 0.7.2','static currentProducer')
check(static.get('registryAuditDate')=='2026-08-26','static registry audit date')
check('obj["databaseVersion"]="0.7.5"' in build_index,'static index builder database version')
check('obj["currentProducer"]="OpenGLESScope 0.7.2"' in build_index,'static index builder current producer')
pkg=json.loads(read('worker/package.json'))
check(pkg.get('version')=='0.7.5','worker package version')
check(pkg.get('devDependencies',{}).get('wrangler')=='4.124.0','wrangler pin')
wr=json.loads(read('worker/wrangler.jsonc'))
check(wr.get('compatibility_date')=='2026-08-23','Cloudflare accepted compatibility date pin')
check(wr.get('account_id')=='6881527e6e0b9bc4a0c009473428d1bc','Cloudflare account pin')
dbs=wr.get('d1_databases',[])
check(bool(dbs) and dbs[0].get('binding')=='DB' and dbs[0].get('database_id')=='2c945dda-e320-4b3a-9fac-a086373db17c','D1 identity pin')
schema=json.loads(read('report.schema.json'))
check(schema.get('properties',{}).get('technicalReport',{}).get('properties',{}).get('queryDiagnostics',{}).get('maxItems')==16384,'query diagnostic schema bound')
tech_props=schema.get('properties',{}).get('technicalReport',{}).get('properties',{})
check(tech_props.get('schemaVersion',{}).get('enum')==[1,2],'public schema technical report versions')
check('eglRuntime' in tech_props and 'unavailableAttributes' in tech_props.get('eglRuntime',{}).get('properties',{}),'public schema EGL runtime coverage')
config_props=tech_props.get('eglConfigs',{}).get('items',{}).get('properties',{})
check(all(x in config_props for x in ['recordableAndroid','framebufferTargetAndroid','colorComponentTypeExt','unavailableAttributes']),'public schema EGL config extension coverage')
for token in ['actions/checkout@v7','persist-credentials: false','actions/setup-python@v7','python tools/audit_database.py --source-tree .','python tools/repair_repository.py --check','python tools/test_audit_hygiene.py','node --check assets/app.v075.js','node tools/test_routes.mjs','node tools/test_compare_contract.mjs','node tools/test_statistics_filters.mjs','node tools/test_ui_parity.mjs','node worker/tests/contract.mjs','python tools/build_pages_artifact.py _site','python tools/audit_database.py --artifact-tree _site','actions/upload-pages-artifact@v5','include-hidden-files: true','actions/configure-pages@v6','actions/deploy-pages@v5','path: _site']:
    check(token in workflow,f'workflow quality token {token}')
check('pages: write' not in workflow.split('  deploy:',1)[0],'build job must not have Pages write permission')
check('pages: write' in workflow.split('  deploy:',1)[1] and 'id-token: write' in workflow.split('  deploy:',1)[1],'deploy job write permission')
errors.extend(local_ref_errors(root))
for p in root.rglob('*'):
    if not p.is_file(): continue
    if p.suffix in {'.js','.mjs'}:
        text=p.read_text(encoding='utf-8',errors='ignore')
        if re.search(r'(^|\s)//(?!/)',text,re.M) or '/*' in text: errors.append(f'source-code comments forbidden: {p.relative_to(root)}')
    if p.suffix=='.py':
        text=p.read_text(encoding='utf-8',errors='ignore')
        if re.search(r'^\s*#',text,re.M): errors.append(f'source-code comments forbidden: {p.relative_to(root)}')
for forbidden in ['.gradle','build','__pycache__','.idea','node_modules','.wrangler']:
    found=[p.relative_to(root) for p in root.rglob(forbidden)]
    if found: errors.append(f'transient source entries {forbidden}: {found[:5]}')
node=shutil.which('node')
if node:
    for rel in ['assets/app.v075.js','worker/src/index.js','worker/tests/contract.mjs','tools/test_routes.mjs','tools/test_compare_contract.mjs','tools/test_statistics_filters.mjs','tools/test_ui_parity.mjs']:
        r=subprocess.run([node,'--check',str(root/rel)],capture_output=True,text=True)
        if r.returncode: errors.append(f'node syntax {rel}: {r.stderr.strip()}')
if errors:
    print('\n'.join(errors)); sys.exit(1)
print('OpenGLESScope Database 0.7.5 source audit: PASS')
print('producer=OpenGLESScope 0.7.2/702 schema=2 technicalReport=2 normalizer=10')
