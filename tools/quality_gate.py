#!/usr/bin/env python3
from __future__ import annotations
import json, os, shutil, subprocess, sys
from concurrent.futures import ThreadPoolExecutor
from gate_common import ROOT

SCRIPTS=[
 'verify_3_0_0_angle_pairing.py',
 'verify_3_0_1_angle_families.py',
 'verify_3_0_2_database_notice.py',
 'verify_3_0_3_metric_parity.py',
 'verify_3_0_4_failure_navigation.py',
 'verify_3_0_5_txt_json_contract.py',
 'verify_3_0_6_emulator_probe.py',
 'verify_3_0_7_translator_identity.py',
 'verify_2_2_22_evidence_parity.py',
 'test_2_2_22_negative_mutations.py',
 'audit_release.py',
 'verify_spec_regressions.py',
 'verify_registry_snapshot.py',
 'verify_registry_catalog.py',
 'verify_cmake_registry_lock.py',
 'verify_registry_canonical_names.py',
 'verify_query_coverage.py',
 'verify_egl_registry_coverage.py',
 'verify_egl_query_legality.py',
 'verify_gl_getpname_disposition.py',
 'verify_compile_regressions.py',
 'verify_concurrency_resource_contracts.py',
 'verify_memory_resource_contracts_1_4_0.py',
 'verify_probe_lifecycle.py',
 'test_probe_lifecycle_state_machine.py',
 'verify_report_semantics.py',
 'verify_report_contract_alignment_1_4_0.py',
 'test_report_state_machine.py',
 'verify_security_contracts.py',
 'verify_build_contract.py',
 'verify_ui_accessibility.py',
 'verify_methodology_parity.py',
 'verify_release_docs.py',
]
POST_SCRIPTS=[
 'verify_package_hygiene.py',
 'verify_package_reproducibility.py',
]


def main():
    for cache in ROOT.rglob('__pycache__'): shutil.rmtree(cache,ignore_errors=True)
    failures=[]; env=os.environ.copy(); env['PYTHONDONTWRITEBYTECODE']='1'; env['OPENGLESSCOPE_SKIP_KOTLINC_SYNTAX_PROBE']='1'
    # Same release-method boundary as VulkanScope: the gate source and generated JSON
    # artifacts must themselves parse before constituent verification is trusted.
    for path in sorted((ROOT/'tools').glob('*.py')):
        try: compile(path.read_text(encoding='utf-8'),str(path),'exec')
        except Exception as e: failures.append((str(path.relative_to(ROOT)),f'python syntax: {e}'))
    for path in sorted((ROOT/'registry/generated').glob('*.json'))+[ROOT/'registry/registry_lock.json',ROOT/'registry/gl_registry_manifest.json',ROOT/'registry/egl_registry_manifest.json',ROOT/'registry/registry_catalog_manifest.json']+sorted((ROOT/'tests/golden').glob('*_regression_contract.json')):
        try: json.loads(path.read_text(encoding='utf-8'))
        except Exception as e: failures.append((str(path.relative_to(ROOT)),f'json parse: {e}'))
    if failures:
        print('QUALITY_GATE: FAIL')
        for item in failures: print(' -',item[0],item[1])
        raise SystemExit(1)
    def run_script(script):
        path=ROOT/'tools'/script
        if not path.is_file(): return script, None, '', '', 'missing'
        cp=subprocess.run([sys.executable,'-B',str(path)],cwd=ROOT,text=True,capture_output=True,env=env)
        return script, cp.returncode, cp.stdout or '', cp.stderr or '', None
    # Earlier source-version-bound mutation verifiers remain immutable in tools/ and run
    # against their exact original archive. The 2.2.22 predecessor-hash oracle prevents
    # UI/source drift while current generic gates audit the upgraded native collector.
    # Constituent gates are source-read-only; mutation tests operate on isolated temp clones.
    # Run a bounded set concurrently so the aggregate remains practical without weakening evidence.
    with ThreadPoolExecutor(max_workers=4) as pool:
        results=list(pool.map(run_script,SCRIPTS))
    for script,code,out,err,missing in results:
        print(out,end='')
        if err: print(err,end='',file=sys.stderr)
        if missing: failures.append((script,missing))
        elif code: failures.append((script,f'exit {code}'))
    # Package hygiene/reproducibility inspect the shared source tree. Run them
    # only after all parallel readers/mutation-clone setup has finished so a
    # transient interpreter cache cannot race the hygiene scan.
    for cache in ROOT.rglob('__pycache__'):
        shutil.rmtree(cache,ignore_errors=True)
    for script in POST_SCRIPTS:
        script,code,out,err,missing=run_script(script)
        print(out,end='')
        if err: print(err,end='',file=sys.stderr)
        if missing: failures.append((script,missing))
        elif code: failures.append((script,f'exit {code}'))
    if failures:
        print('QUALITY_GATE: FAIL')
        for item in failures: print(' -',item[0],item[1])
        raise SystemExit(1)
    print('QUALITY_GATE: PASS')

if __name__=='__main__': main()
