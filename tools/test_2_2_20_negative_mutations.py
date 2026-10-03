#!/usr/bin/env python3
"""Independent mutations for EGL string provenance and complete bounded enumeration."""
from gate_common import ROOT,require,main_guard
from verify_2_2_20_device_dma_integrity import source_oracle,NATIVE
MUTATIONS=[
('remove 64-bit modifier display','static std::string hexModifier(EGLuint64KHR value)','static std::string missingModifier(EGLuint64KHR value)'),
('accept partial count','written == expected','written <= expected'),
('ignore EGL device error','if (error != EGL_SUCCESS) return {nullptr, eglErrorDisplay(error)};','if (false) return {nullptr, eglErrorDisplay(error)};'),
('accept invalid device string','if (!runtimeStringValid(raw)) return','if (false) return'),
('skip device extension provenance','const auto deviceExtText = queryDeviceText(device, EGL_EXTENSIONS);','const auto deviceExtText = std::pair<const char*,std::string>{"", ""};'),
('skip renderer provenance','const auto renderer = queryDeviceText(device, EGL_RENDERER_EXT_VALUE);','const auto renderer = std::pair<const char*,std::string>{nullptr,""};'),
('skip driver name provenance','const auto driverName = queryDeviceText(device, EGL_DRIVER_NAME_EXT_VALUE);','const auto driverName = std::pair<const char*,std::string>{nullptr,""};'),
('trust display-device query without EGL error','deviceRead == EGL_TRUE && deviceReadError == EGL_SUCCESS && rawDevice != 0','deviceRead == EGL_TRUE && rawDevice != 0'),
('trust unstable device enumeration','readOk && stableCount(count, written)','readOk'),
('trust unstable DRM format count','formatsRead && stableCount(formatCount, writtenFormats)','formatsRead'),
('trust unstable modifier count','modifiersRead && stableCount(modifierCount, writtenModifiers)','modifiersRead'),
('drop numerical raw modifier value','modifierDetail += hexModifier(modifiers[index]);','modifierDetail += "Modifier unknown";'),
('fabricate external-only false','modifierDetail += externalOnly[index] == EGL_TRUE ? "[externalOnly=true]" : "[externalOnly=false]";','modifierDetail += "[externalOnly=false]";'),
('accept malformed external-only flag','if (externalOnly[index] != EGL_TRUE && externalOnly[index] != EGL_FALSE)','if (false)'),
('allow invalid modifier status','modifierFlagsValid ? "Available" : "Unavailable"','modifiersRead ? "Available" : "Unavailable"'),
('accept unverified MESA driver text','driverNameError == EGL_SUCCESS && runtimeStringValid(driverName)','runtimeStringValid(driverName)'),
('permit unbounded DMA totals','kMaxDmaBufModifierCountTotal = 4096','kMaxDmaBufModifierCountTotal = 65536'),
('omit compression rate count gate','const bool ok = readOk && stableCount(count, written);','const bool ok = readOk;'),
]
def verify():
 s=(ROOT/NATIVE).read_text();source_oracle(s)
 for label,needle,replacement in MUTATIONS:
  require(needle in s,'stale negative test: '+label)
  broken=s.replace(needle,replacement,1)
  try: source_oracle(broken)
  except AssertionError: continue
  raise AssertionError('regression escaped: '+label)
 print('test_2_2_20_negative_mutations: PASS ('+str(len(MUTATIONS))+' defects rejected)')
if __name__=='__main__':main_guard('test_2_2_20_negative_mutations',verify)
