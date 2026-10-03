#!/usr/bin/env python3
"""Reject deliberate network, collection, disabled-submit, and icon geometry regressions."""
from __future__ import annotations
from gate_common import ROOT,require,main_guard
from verify_2_2_16_status_navigation import ui_oracle,MAIN
MUTANTS=[
 ('disabled submit becomes clickable','enabled = !submissionInFlight && completeReportReady && networkAvailable','enabled = true'),
 ('stale click bypass','if (submissionInFlight || !completeReportReady || !networkAvailable) false else','if (submissionInFlight) false else'),
 ('active-looking offline arrow','idleTrailingIcon = R.drawable.ic_upload','idleTrailingIcon = R.drawable.ic_upload,\n                    readableDisabledTrailing = true'),
 ('disabled state ignores priority','!enabled -> accent','!enabled -> trailingTint ?: accent'),
 ('dual lock missing','if (!completeReportReady && !networkAvailable) {','if (false) {'),
 ('offline line removed','Database upload and public report browsing are disabled until Android reports a validated internet connection.','Internet disabled.'),
 ('network recovery missing','When internet returns during collection, the network lock clears immediately;','Network ready.'),
 ('wrong pop-up icon','R.drawable.ic_info','R.drawable.ic_network_disconnected'),
 ('collection/network sequencing','ConnectivityStatusHost(collectionStatus, networkStateKnown, networkAvailable, networkBannerState)','ConnectivityStatusHost(CollectionStatus.IDLE, networkStateKnown, networkAvailable, networkBannerState)'),
 ('offline collecting evidence lost','Internet-dependent actions remain locked by network state, while report-dependent actions also remain locked until collection completes.','Internet unavailable.'),
 ('no vendor report acknowledgement','if (result.success) submissionSuccessId = result.reportId else submissionFailureLog = result.log','submissionSuccessId = result.reportId'),
 ('nav unequal icon slots','modifier = Modifier.size(artworkSlot)','modifier = Modifier.size(compactIconSize - 5.dp)'),
 ('egl icon no longer centred','Page.EGL -> EglBrandArtwork(','Page.EGL -> SemanticArtwork('),
 ('egl loses white tint','tint = ComposeColor.White','tint = BrandSoft'),
 ('gles icon not normalized','Page.OpenGLES -> Image(','Page.OpenGLES -> SemanticArtwork('),
 ('narrow nav bar','compactMaxWidth = if (landscape) 384.dp else 348.dp','compactMaxWidth = if (landscape) 310.dp else 280.dp'),
 ('RTL/equal-weight regression','.weight(1f)','.weight(0.5f)'),
 ('no large font wrapping','maxLines = if (accessibilityScale && !landscape) 2 else 1','maxLines = 1'),
]
def verify():
 s=(ROOT/MAIN).read_text();ui_oracle(s)
 for label,old,new in MUTANTS:
  require(old in s,'stale negative fixture: '+label)
  db_start=s.index('CapabilitySectionCard("OpenGLESScope Database")')
  db_end=s.index('\n@Composable\nprivate fun CapabilityListPage(',db_start)
  host_start=s.index('private fun TransientStatusOverlayHost(')
  host_end=s.index('\n@Composable\nprivate fun UpdateAvailableIcon(',host_start)
  nav_start=s.index('private fun CompactBottomNavigationBar(')
  nav_end=s.index('\n@Composable\nprivate fun HeaderActionButton(',nav_start)
  button_start=s.index('private fun ExpressiveActionButton(')
  button_end=s.index('\n@Composable\nprivate fun ExpressiveIdentityBlock(',button_start)
  if label in ('disabled state ignores priority',): start,end=button_start,button_end
  elif label in ('wrong pop-up icon','collection/network sequencing','offline collecting evidence lost'): start,end=host_start,host_end
  elif label in ('nav unequal icon slots','egl icon no longer centred','egl loses white tint','gles icon not normalized','narrow nav bar','RTL/equal-weight regression','no large font wrapping'): start,end=nav_start,nav_end
  else:start,end=db_start,db_end
  section=s[start:end];require(old in section,'stale targeted fixture: '+label)
  changed=s[:start]+section.replace(old,new,1)+s[end:]
  try:ui_oracle(changed)
  except (AssertionError,ValueError): pass
  else:raise AssertionError('undetected regression: '+label)
 print(f'test_2_2_16_negative_mutations: PASS ({len(MUTANTS)} deliberate bugs rejected)')
if __name__=='__main__':main_guard('test_2_2_16_negative_mutations',verify)
