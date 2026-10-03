#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    cases=[
        ('!it.optBoolean("draft", false) && !it.optBoolean("prerelease", false)','!it.optBoolean("draft", false)'),
        ('pending != null && pending.exists() && directUpdatesEnabled','pending != null && pending.exists()'),
        ('if (updateCheckInFlight || updateDownloadJob != null || updateTransferState != null) return','if (updateDownloadJob != null || updateTransferState != null) return'),
        ('val limit = 96 * 1024','val limit = Int.MAX_VALUE'),
        ('readResponseTextLimited(res.body, 64 * 1024)','res.body?.string().orEmpty()'),
        ('payloadBytes.size > 2 * 1024 * 1024','payloadBytes.size > Int.MAX_VALUE'),
        ('pendingWatchDelete = token','watched = watched - token'),
        ('context.assets.open(library.licenseAsset)','java.net.URL(library.licenseAsset).readText()'),
    ]
    for i,(needle,replacement) in enumerate(cases,1):
        require(needle in main,f'negative mutation fixture {i} missing')
        mutated=main.replace(needle,replacement,1)
        require(mutated != main and mutated.count(needle)==main.count(needle)-1,f'negative mutation {i} was not detected')
if __name__=='__main__': main_guard('test_1_8_0_negative_mutations',verify)
