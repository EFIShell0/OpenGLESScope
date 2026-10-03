#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
    c=(ROOT/'app/src/main/java/com/efishell/openglesscope/OpenGLESProbeContract.kt').read_text(encoding='utf-8')
    native=(ROOT/'app/src/main/cpp/openglesscope.cpp').read_text(encoding='utf-8')
    require('kMaxEglCapabilityCount = 256' in native,'native EGL capability bound missing')
    require('item.name == name' in native and 'Duplicate EGL capability identity returned by the collector' in native,'native EGL capability duplicate rejection missing')
    require('EGL capability evidence exceeded the 256-record safety bound' in native,'native EGL capability overflow must fail closed')
    required={
      'MAX_LIMITS = 8192':'limit bound','MAX_ENUMERATION_ITEMS = 16384':'enumeration bound','MAX_EGL_CAPABILITIES = 256':'EGL capability bound',
      'MAX_INTERNAL_FORMATS = 256':'internal-format bound','MAX_PRECISION = 64':'precision bound','MAX_DIAGNOSTICS = 16384':'diagnostic bound',
      'MAX_EGL_CONFIGS = 4096':'EGL config bound','MAX_EGL_FAILURES = 64':'nested EGL failure bound',
      'terminalStates = setOf("Available", "Unavailable", "Not applicable", "Unknown")':'canonical evidence states',
      'uniqueObjectArray(value, MAX_EGL_CAPABILITIES':'EGL capability uniqueness','uniqueObjectArray(value, MAX_LIMITS':'limit uniqueness',
      'uniqueObjectArray(value, MAX_INTERNAL_FORMATS':'internal-format uniqueness','uniqueObjectArray(value, MAX_PRECISION':'precision uniqueness',
      'uniqueObjectArray(value, MAX_DIAGNOSTICS':'diagnostic uniqueness','uniqueObjectArray(value, MAX_EGL_CONFIGS':'EGL config uniqueness',
      'isUniqueStringArray(root.opt("extensions"), MAX_ENUMERATION_ITEMS)':'extension uniqueness/bound',
    }
    for token,desc in required.items(): require(token in c,f'producer-side strict report contract missing {desc}')
    require('sampleCounts.length() > 64' in c and '!sampleSeen.add(sample.toInt())' in c,'internal-format sample count bound/uniqueness missing')
    require('sampleSeen.contains(nv.optInt("samples"))' in c,'NV sample evidence must reference a reported sample count')
    require('item.optString("status") == "Available" || (sampleCounts.length() == 0' in c,'failed internal-format evidence must not carry fabricated samples')

if __name__=='__main__': main_guard('verify_report_contract_alignment_1_4_0',verify)
