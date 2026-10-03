#!/usr/bin/env python3
"""Independent in-memory UI/branding regressions must fail the exact 2.2.21 source oracle."""
from gate_common import ROOT,require,main_guard
from verify_2_2_21_gpu_navigation import MAIN,GRADLE,source_oracle

def verify():
    s=(ROOT/MAIN).read_text();g=(ROOT/GRADLE).read_text();source_oracle(s,g)
    variants=[
       ('drop Display primary','NavigationItem(Page.Display, "Display", R.drawable.ic_display)','NavigationItem(Page.EGL, "EGL™", R.drawable.ic_egl_official)'),
       ('wrong nested EGL selection','Page.EGL -> Page.OpenGLES','Page.EGL -> Page.Display'),
       ('wrong transition','Page.Display -> 2','Page.Display -> 4'),
       ('wide nav drift','if (landscape) 340.dp else PrimaryNavigationMaxWidth','if (landscape) 384.dp else PrimaryNavigationMaxWidth'),
       ('asymmetric safe insets','16.dp + endSystemInset','0.dp + endSystemInset'),
       ('remove equal hit width','.weight(1f)\n                            .fillMaxHeight()','.width(52.dp)\n                            .fillMaxHeight()'),
       ('remove neutral hero','Surface(color = ComposeColor(0xFF181516), shape = MaterialTheme.shapes.extraLargeIncreased','Surface(color = SurfaceRaised, shape = MaterialTheme.shapes.extraLargeIncreased'),
       ('incorrect driver header','Text("System Driver"','Text("System OpenGL ES"'),
       ('fabricated vendor ID','CapabilityKeyValue("EGL_VENDOR", report.egl.vendor.ifBlank { "Unavailable" })','CapabilityKeyValue("Vendor ID", "0x0000")'),
       ('remove brand evidence boundary','no vendor/device ID or physical GPU model is inferred','vendor/device ID is inferred'),
       ('lose context variation','title.equals("Current EGL binding and context", true) -> EglSectionArtwork(EglArtworkUse.CONTEXT)','title.equals("Current EGL binding and context", true) -> EglSectionArtwork(EglArtworkUse.IDENTITY)'),
       ('lose pbuffer variation','title.equals("Collector pbuffer", true) -> EglSectionArtwork(EglArtworkUse.PBUFFER)','title.equals("Collector pbuffer", true) -> EglSectionArtwork(EglArtworkUse.IDENTITY)'),
       ('discard original EGL art','painterResource(R.drawable.ic_egl_official)','painterResource(R.drawable.ic_info)'),
       ('fuzzy renderer matching','declared = vendor.trim().lowercase(java.util.Locale.ROOT)','declared = renderer.trim().lowercase(java.util.Locale.ROOT)'),
       ('remove neutral software layer','"mesa", "freedreno"','"freedreno"'),
       ('divergent database art','painter = painterResource(vendorArtworkResource(vendor, renderer))','painter = painterResource(R.drawable.gpu_vendor_unknown)'),
       ('unanchored vendor match','^qualcomm\\b','qualcomm'),
       ('fabricated VeriSilicon art','    Regex("""^vivante\\b""") to R.drawable.gpu_vendor_vivante','    Regex("""^verisilicon\\b""") to R.drawable.gpu_vendor_vsi'),
       ('accessibility art desync','contentDescription = artworkDescription','contentDescription = "Qualcomm"'),
    ]
    detected=0
    for label,old,new in variants:
       require(old in s,'mutation fixture stale: '+label)
       damaged=s.replace(old,new,1)
       try: source_oracle(damaged,g)
       except AssertionError: detected+=1
       else: raise AssertionError('mutation escaped: '+label)
    try: source_oracle(s,g.replace('releaseVersionCode = 2221','releaseVersionCode = 2220'))
    except AssertionError:detected+=1
    else:raise AssertionError('version identity mutation escaped')
    require(detected==20,'mutation census mismatch')
    print('test_2_2_21_negative_mutations: PASS (20 independent deliberate violations rejected)')
if __name__=='__main__': main_guard('test_2_2_21_negative_mutations',verify)
