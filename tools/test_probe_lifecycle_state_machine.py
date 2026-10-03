#!/usr/bin/env python3
from gate_common import require, main_guard

TERMINAL={'available','unavailable','completed','completed_with_failures'}

def model(events, self_test=False):
    state='idle'; marker=False; valid=False; process='dead'; result=None
    for event in events:
        if event=='start':
            require(state=='idle','double start accepted')
            state='running'; process='alive'
        elif event=='partial_write':
            require(state=='running','partial write outside run')
            valid=False
        elif event=='valid_available':
            require(state=='running' and not self_test,'base result published in invalid state')
            valid=True; result='available'
        elif event=='valid_selftest':
            require(state=='running' and self_test,'self-test result published in invalid state')
            valid=True; result='completed'
        elif event=='unavailable':
            require(state=='running','unavailable outside run')
            valid=True; result='unavailable'
        elif event=='marker':
            require(valid and result in TERMINAL,'terminal marker accepted before valid terminal payload')
            marker=True; state='terminal'
        elif event=='timeout':
            require(state in {'running','terminal'},'timeout outside probe')
            if state!='terminal': result='unavailable'; valid=True; state='terminal'
            process='dead'
        elif event=='cancel':
            require(state=='running','cancel outside run')
            process='dead'; state='cancelled'
        elif event=='kill':
            process='dead'
        else: raise AssertionError(event)
    return state,marker,valid,process,result

def verify():
    require(model(['start','partial_write','valid_available','marker','kill']) == ('terminal',True,True,'dead','available'),'normal base terminal sequence failed')
    require(model(['start','valid_selftest','marker','kill'],True) == ('terminal',True,True,'dead','completed'),'normal self-test terminal sequence failed')
    state,marker,valid,process,result=model(['start','partial_write','timeout'])
    require(state=='terminal' and not marker and valid and process=='dead' and result=='unavailable','timeout must end bounded and unavailable')
    state,marker,valid,process,result=model(['start','cancel'])
    require(state=='cancelled' and process=='dead' and not marker,'cancellation must teardown one-shot process')
    failed=False
    try: model(['start','partial_write','marker'])
    except AssertionError: failed=True
    require(failed,'marker-before-terminal negative state was accepted')

if __name__=='__main__': main_guard('test_probe_lifecycle_state_machine',verify)
