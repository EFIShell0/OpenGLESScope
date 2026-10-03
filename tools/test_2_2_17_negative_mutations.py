#!/usr/bin/env python3
"""Reject deliberate recurrence of the 2.2.17 TextUnit compile bug and label geometry drift."""
from __future__ import annotations
from gate_common import ROOT, require, main_guard
from verify_2_2_17_kotlin_compile_hotfix import compile_hotfix_oracle, MAIN

MUTANTS=[
 ('reintroduce TextUnit subtraction',
  'fontSize = if (item.page == Page.OpenGLES && !accessibilityScale) compactOpenGlesLabelFontSize else compactLabelFontSize',
  'fontSize = if (item.page == Page.OpenGLES && !accessibilityScale) compactLabelFontSize - 0.5.sp else compactLabelFontSize'),
 ('remove explicit landscape/portrait value',
  'val compactOpenGlesLabelFontSize = if (landscape) 7.5.sp else 8.5.sp',
  'val compactOpenGlesLabelFontSize = compactLabelFontSize'),
 ('wrong landscape value',
  'if (landscape) 7.5.sp else 8.5.sp',
  'if (landscape) 8.sp else 8.5.sp'),
 ('wrong portrait value',
  'if (landscape) 7.5.sp else 8.5.sp',
  'if (landscape) 7.5.sp else 9.sp'),
]

def verify() -> None:
    s=(ROOT/MAIN).read_text(encoding='utf-8')
    compile_hotfix_oracle(s)
    for label,old,new in MUTANTS:
        require(old in s,'stale negative fixture: '+label)
        changed=s.replace(old,new,1)
        try: compile_hotfix_oracle(changed)
        except AssertionError: pass
        else: raise AssertionError('undetected regression: '+label)
    print(f'test_2_2_17_negative_mutations: PASS ({len(MUTANTS)} deliberate compile/geometry regressions rejected)')

if __name__=='__main__': main_guard('test_2_2_17_negative_mutations',verify)
