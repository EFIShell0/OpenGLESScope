#!/usr/bin/env python3
from gate_common import require, main_guard

REQUIRED={'renderer','vendor','glVersion','glMajor','glMinor','glslVersion','egl','eglRuntime','extensions','limits','compressedFormats','internalFormats','shaderBinaryFormats','programBinaryFormats','precision','eglConfigs','diagnostics'}

def classify(status, keys):
    keys=set(keys)
    if status=='unavailable': return 'Unavailable'
    if status=='available' and REQUIRED <= keys: return 'Available'
    return 'Incomplete'

def submit_allowed(classification, size, report_id=None):
    if classification!='Available' or size<=0 or size>2*1024*1024: return False
    if report_id is None: return True
    return len(report_id)==64 and all(c in '0123456789abcdef' for c in report_id)

def verify():
    require(classify('available',REQUIRED)=='Available','complete report not accepted')
    require(classify('available',REQUIRED-{'eglConfigs'})=='Incomplete','missing top-level evidence promoted to complete')
    require(classify('unavailable',set())=='Unavailable','explicit unavailable collapsed')
    require(not submit_allowed('Incomplete',1024),'partial report submission accepted')
    require(not submit_allowed('Available',2*1024*1024+1),'oversize report submission accepted')
    require(submit_allowed('Available',1024),'complete bounded report rejected')
    require(submit_allowed('Available',1024,'a'*64),'canonical report id rejected')
    require(not submit_allowed('Available',1024,'abc'),'malformed report id accepted')

if __name__=='__main__': main_guard('test_report_state_machine',verify)
