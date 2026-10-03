#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text(encoding='utf-8')
    manifest=(ROOT/'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
    cases=[
        ('SAF return', main, 'private fun SharedStorageBrowserDialog(', 'private fun SAFSharedStorageBrowserDialog(/* ActivityResultContracts.OpenDocument */'),
        ('path escape', main, 'if (!isCanonicalSharedStoragePath(root, canonical)', 'if (false && !isCanonicalSharedStoragePath(root, canonical)'),
        ('entry ceiling', main, 'children.size >= 4096', 'children.size >= Int.MAX_VALUE'),
        ('atomic write', main, 'private fun atomicWriteSharedStorageFile(', 'private fun unsafeWriteSharedStorageFile('),
        ('add icon', main, 'idleTrailingIcon = R.drawable.ic_add', 'idleTrailingIcon = R.drawable.ic_watch_add'),
        ('OpenGLES color', main, 'private val Brand = ComposeColor(0xFFBA2A8D)', 'private val Brand = ComposeColor(0xFFA41E22)'),
        ('storage permission', manifest, 'android.permission.MANAGE_EXTERNAL_STORAGE', 'android.permission.WRITE_EXTERNAL_STORAGE'),
    ]
    for label,text,needle,replacement in cases:
        require(needle in text,'negative mutation fixture missing: '+label)
        mutated=text.replace(needle,replacement,1)
        require(mutated!=text and mutated.count(needle)==text.count(needle)-1,'negative mutation did not change fixture: '+label)

if __name__=='__main__': main_guard('test_1_9_0_negative_mutations',verify)
