#!/usr/bin/env python3
"""Fail closed when 2.2.18 notification and EGL evidence corrections are broken."""
from __future__ import annotations
from gate_common import ROOT,require,main_guard
from verify_2_2_18_state_and_evidence import notification_oracle,egl_oracle,MAIN,NATIVE

MAIN_MUTANTS=[
 ('mask failed probe as success','collectionStatus = if (parsed.available) CollectionStatus.COMPLETED else CollectionStatus.FAILED','collectionStatus = CollectionStatus.COMPLETED'),
 ('drop exception failure state','collectionStatus = CollectionStatus.FAILED\n        } finally','collectionStatus = CollectionStatus.IDLE\n        } finally'),
 ('hide failure on success timer','if (collectionStatus == CollectionStatus.COMPLETED) {\n            delay(2200L)','if (collectionStatus != CollectionStatus.COLLECTING) {\n            delay(2200L)'),
 ('hide old failure after transient','if (collectionStatus == CollectionStatus.COMPLETED) collectionStatus = CollectionStatus.IDLE','collectionStatus = CollectionStatus.IDLE'),
 ('alter success notification duration','delay(2200L)','delay(500L)'),
 ('swap offline informational icon','painterResource(R.drawable.ic_info)','painterResource(R.drawable.ic_network_disconnected)'),
 ('swap connected icon','if (connected) R.drawable.ic_network_connected else R.drawable.ic_network_disconnected','if (connected) R.drawable.ic_network_disconnected else R.drawable.ic_network_disconnected'),
 ('lose persistent offline gate','if (networkStateKnown && !networkAvailable && transitionState == NetworkBannerState.HIDDEN)','if (networkStateKnown && transitionState == NetworkBannerState.HIDDEN)'),
 ('drop update checking animation','UpdateStatus.Checking -> { ExpressiveLinearProgressIndicator','UpdateStatus.Checking -> { Text'),
 ('drop update failure indicator','is UpdateStatus.Failed -> Text(status.message','is UpdateStatus.Failed -> Unit'),
]
NATIVE_MUTANTS=[
 ('ignore display extension EGL error','displayExtensionError == EGL_SUCCESS && runtimeStringValid(rawDisplayExtensionText)','runtimeStringValid(rawDisplayExtensionText)'),
 ('bypass EGL query result error','return error == EGL_SUCCESS && runtimeStringValid(raw) ? raw : nullptr;','return runtimeStringValid(raw) ? raw : nullptr;'),
 ('wrong vendor query','queryEglDisplayString(EGL_VENDOR, rawEglVendorText, eglVendorError)','queryEglDisplayString(EGL_VERSION, rawEglVendorText, eglVendorError)'),
 ('lose EGL_VERSION per-query checking','queryEglDisplayString(EGL_VERSION, rawEglVersionText, eglVersionError)','rawEglVersionText'),
 ('lose EGL_CLIENT_APIS per-query checking','queryEglDisplayString(EGL_CLIENT_APIS, rawEglClientApisText, eglClientApisError)','rawEglClientApisText'),
 ('erase pre-1.5 EGL_BAD_DISPLAY distinction','eglCode < 150 && clientExtError == EGL_BAD_DISPLAY && rawClientExtensionText == nullptr','eglCode < 150'),
 ('accept incomplete vendor as available','if (!eglVendorText || !eglVersionText || (eglCode >= 120 && !eglClientApisText))','if (!eglVersionText || (eglCode >= 120 && !eglClientApisText))'),
 ('accept incomplete EGL_VERSION','Required EGL_VERSION identity query failed:','EGL version maybe missing'),
 ('misgate EGL_CLIENT_APIS to 1.5','eglCode >= 120 && !eglClientApisText','eglCode >= 150 && !eglClientApisText'),
 ('pretend failed EGL identity report is complete','releaseEgl(d, s, c);\n        return env->NewStringUTF((std::string(','return env->NewStringUTF((std::string('),
 ('self-test ignores EGL error','selfTestExtensionsError == EGL_SUCCESS ? splitExt(selfTestDisplayExtensions, &selfTestDisplayExtComplete)','true ? splitExt(selfTestDisplayExtensions, &selfTestDisplayExtComplete)'),
]

def verify()->None:
    main=(ROOT/MAIN).read_text();native=(ROOT/NATIVE).read_text()
    notification_oracle(main);egl_oracle(native)
    count=0
    for label,anchor,bad in MAIN_MUTANTS+NATIVE_MUTANTS:
        source=main if (label,anchor,bad) in MAIN_MUTANTS else native
        require(anchor in source,'stale mutation anchor: '+label)
        mutated=source.replace(anchor,bad,1)
        try:
            (notification_oracle if source is main else egl_oracle)(mutated)
        except AssertionError: pass
        else:raise AssertionError('regression not detected: '+label)
        count+=1
    print('test_2_2_18_negative_mutations: PASS ('+str(count)+' deliberate notification/GL-EGL regressions detected)')
if __name__=='__main__':main_guard('test_2_2_18_negative_mutations',verify)
