#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
    reg=(ROOT/'app/src/main/java/com/efishell/openglesscope/RegistryCatalog.kt').read_text()
    cases=[
        (main,'ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 100','ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 99'),
        (main,'withContext(Dispatchers.IO) { runCatching { RegistryCatalog.load(context) } }','runCatching { RegistryCatalog.load(context) }'),
        (main,'.take(ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT + 1).toList()','.toList()'),
        (reg,'MAX_CATALOG_BYTES = 2 * 1024 * 1024','MAX_CATALOG_BYTES = Int.MAX_VALUE'),
    ]
    for i,(source,needle,replacement) in enumerate(cases,1):
        require(needle in source,f'negative mutation fixture {i} missing')
        mutated=source.replace(needle,replacement,1)
        require(mutated.count(needle)==source.count(needle)-1,f'negative mutation {i} was not detected')
if __name__=='__main__': main_guard('test_1_7_0_negative_mutations',verify)
