#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard
from pathlib import Path
import hashlib

REFERENCE_SHA256 = '9c89153fb34352f567ccdec7b3685ac1feab56c45b46b22f320375dfed66e7ca'
REFERENCE_SECTION_COUNT = 199

def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()

def verify():
    rules=(ROOT/'rules/PROJECT_RULES.md').read_text()
    gate=(ROOT/'tools/quality_gate.py').read_text()
    reference=ROOT/'rules/VULKANSCOPE_3.0.12_PROJECT_RULES_REFERENCE.md'
    snapshot=ROOT/'tests/golden/vulkanscope_3_0_12_rule_headings.txt'

    require(reference.is_file(),'exact VulkanScope 3.0.12 PROJECT_RULES reference missing')
    require(sha256(reference)==REFERENCE_SHA256,'VulkanScope 3.0.12 PROJECT_RULES reference drifted')
    headings=[line[3:] for line in reference.read_text().splitlines() if line.startswith('## ')]
    require(len(headings)==REFERENCE_SECTION_COUNT,f'VulkanScope methodology section census drifted: {len(headings)}')
    require(len(headings)==len(set(headings)),'VulkanScope methodology reference contains duplicate level-2 headings')
    require(snapshot.is_file(),'VulkanScope methodology heading snapshot missing')
    expected=[line for line in snapshot.read_text().splitlines() if line]
    require(expected==headings,'VulkanScope methodology heading snapshot does not match exact 3.0.12 reference')
    applicability_path=ROOT/'tests/golden/vulkanscope_3_0_12_rule_applicability.json'
    require(applicability_path.is_file(),'VulkanScope methodology applicability manifest missing')
    import json
    applicability=json.loads(applicability_path.read_text(encoding='utf-8'))
    entries=applicability.get('entries',[])
    require(applicability.get('referenceHeadingCount')==REFERENCE_SECTION_COUNT and len(entries)==REFERENCE_SECTION_COUNT,'VulkanScope methodology applicability census drift')
    require([entry.get('heading') for entry in entries]==headings,'VulkanScope methodology applicability heading/order drift')
    allowed={'adopted','adapted','api_specific_reference','historical_methodology_reference'}
    require(all(entry.get('status') in allowed and str(entry.get('coverage','')).strip() for entry in entries),'VulkanScope methodology applicability contains unclassified entries')

    rule_topics=['Non-negotiable','Architecture','Features and limits','Complete report contract','Mandatory evidence workflow for every future change','Android build gate','Khronos registry lock','Probe process and terminal publication','Native ownership and bounds','Security contract','Negative-mutation gate','Deterministic package and clean-extract gate','Exact VulkanScope 3.0.12 methodology reference','File Manager parity','Icon semantics','Settings information architecture','Encyclopedia parity','Performance contract','Release 2.1.1 full-report/UI/Analysis evidence parity audit','Release 2.1.2 current toolchain/spec/query/detail parity audit','Release 2.1.3 NDK r30 native compile-correctness hotfix','Release 2.1.4 Kotlin/Compose compile-correctness hotfix','Release 2.2.0 VulkanScope 3.0.12 visual/interaction parity contract','Release 2.2.1 Kotlin/Compose parity compile-correctness hotfix','Release 2.2.2 VulkanScope video-detail parity audit']
    for topic in rule_topics:
        require(topic in rules,f'methodology rule topic missing: {topic}')

    scripts=['verify_2_2_0_ui_parity.py','verify_2_2_1_kotlin_compile_hotfix.py','verify_2_2_2_video_ui_parity.py','verify_2_2_2_regression_contract.py','test_2_2_2_negative_mutations.py','audit_release.py','verify_spec_regressions.py','verify_registry_snapshot.py','verify_cmake_registry_lock.py','verify_registry_canonical_names.py','verify_query_coverage.py','verify_egl_registry_coverage.py','verify_registry_catalog.py','verify_compile_regressions.py','verify_concurrency_resource_contracts.py','verify_probe_lifecycle.py','test_probe_lifecycle_state_machine.py','verify_report_semantics.py','test_report_state_machine.py','verify_security_contracts.py','verify_build_contract.py','verify_ui_accessibility.py','verify_gl_getpname_disposition.py','verify_egl_query_legality.py','verify_memory_resource_contracts_1_4_0.py','verify_report_contract_alignment_1_4_0.py','verify_package_hygiene.py','verify_package_reproducibility.py']
    archive=json.loads((ROOT/'tests/golden/2_2_15_archived_verifiers.json').read_text(encoding='utf-8'))
    require(archive.get('predecessor')=='2.2.14','methodology predecessor archive version mismatch')
    archived=archive.get('archivedFileHashes',{})
    require('verify_2_2_22_evidence_parity.py' in gate and 'test_2_2_22_negative_mutations.py' in gate, 'current release coverage missing')
    require((ROOT/'tools/verify_2_2_21_gpu_navigation.py').is_file() and (ROOT/'tools/test_2_2_21_negative_mutations.py').is_file(),'historical 2.2.21 verifier evidence missing')
    require((ROOT/'tools/verify_2_2_20_device_dma_integrity.py').is_file() and (ROOT/'tools/test_2_2_20_negative_mutations.py').is_file(), 'historical EGL query verification missing')
    predecessor=json.loads((ROOT/'tests/golden/2_2_18_archived_predecessor_verifiers.json').read_text())
    require(predecessor['reference']=='exact OpenGLESScope 2.2.17 ZIP', 'historic release source identity drift')
    require(len(predecessor['archivedVerifierHashes'])==4, '2.2.17 active test archive incomplete')
    for script, digest in predecessor['archivedVerifierHashes'].items():
        file=ROOT/'tools'/script
        require(file.is_file() and sha256(file)==digest, 'earlier active test mutated or removed: '+script)
    for script in scripts:
        if script not in gate:
            require(script in archived,'methodology historical test missing: '+script)
    for script,digest in archived.items():
        p=ROOT/'tools'/script
        require(p.is_file() and sha256(p)==digest,'historical verifier modified or removed: '+script)

    for archived in ['verify_2_2_1_regression_contract.py','test_2_2_1_negative_mutations.py']:
        require((ROOT/'tools'/archived).is_file(),'historical predecessor contract missing: '+archived)

    for phrase in ['Vulkan-only','Not applicable','OpenGL ES/EGL','byte-for-byte','199 level-2 rule']:
        require(phrase in rules,f'API adaptation/reference boundary missing: {phrase}')

if __name__=='__main__':
    main_guard('verify_methodology_parity',verify)
