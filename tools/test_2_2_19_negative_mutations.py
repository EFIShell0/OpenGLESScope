#!/usr/bin/env python3
"""Deliberately break each new 2.2.19 safety/performance guarantee."""
from gate_common import ROOT,require,main_guard
from verify_2_2_19_integrity_security_performance import source_oracle,NATIVE
MUTATIONS=[
('accept mismatched versions','major != parsed.first || minor != parsed.second','major < 3'),
('erase inconsistency diagnostic','diagnostic("Runtime GL version consistency", GL_INVALID_VALUE);',''),
('fabricate a usable version','return {0, -1};','return parsed;'),
('omit self-test version gate','Self-test runtime GL version identity was inconsistent','Self-test maybe fine'),
('ignore shader log reported count','glGetShaderInfoLog(shader, length, &written, buffer.data());','glGetShaderInfoLog(shader, length, nullptr, buffer.data());'),
('ignore program log reported count','glGetProgramInfoLog(program, length, &written, buffer.data());','glGetProgramInfoLog(program, length, nullptr, buffer.data());'),
('unsafe string over-read','std::string(buffer.data(), static_cast<size_t>(written))','std::string(text.data())'),
('allow overflowing GL reported length','written < 0 || written >= length','written < 0'),
('accept truncated config census','totalConfigs != configCapacity','totalConfigs > configCapacity'),
('restore quadratic mapping','auto attr = [&](size_t index) -> EglAttrResult { return attrs[index].result; };','auto attr = [&](const char* name) -> EglAttrResult { for (const auto& item : attrs) if (std::string(item.key) == name) return item.result; return {0, false, EGL_BAD_ATTRIBUTE}; };'),
('corrupt config red key','{"red", configAttr(EGL_RED_SIZE, 100)','{"r", configAttr(EGL_RED_SIZE, 100)'),
('swap config column','attr(0)','attr(1)'),
]
def verify():
 s=(ROOT/NATIVE).read_text();source_oracle(s)
 for label,needle,replacement in MUTATIONS:
  require(needle in s,'stale negative mutation: '+label)
  bad=s.replace(needle,replacement,1)
  try:source_oracle(bad)
  except AssertionError:continue
  raise AssertionError('undetected new regression: '+label)
 print('test_2_2_19_negative_mutations: PASS ('+str(len(MUTATIONS))+' deliberate defects detected)')
if __name__=='__main__':main_guard('test_2_2_19_negative_mutations',verify)
