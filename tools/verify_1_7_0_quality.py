#!/usr/bin/env python3
from gate_common import ROOT, require, main_guard

def verify():
 main=(ROOT/'app/src/main/java/com/efishell/openglesscope/MainActivity.kt').read_text()
 reg=(ROOT/'app/src/main/java/com/efishell/openglesscope/RegistryCatalog.kt').read_text()
 gradle=(ROOT/'app/build.gradle.kts').read_text()
 require('releaseVersionName = "1.7.0"' in gradle and 'releaseVersionCode = 1700' in gradle,'1.7.0 identity missing')
 for token in ['AnimatedNavigationIcon','OpenGLESScopeLazyPage','ExpressiveScrollHints','SettingsSectionCards','ExpressiveDestinationCard','slideInHorizontally(animationSpec = spring())','ShortNavigationBar','CompactNavigationRail']:
  require(token in main,'shared VulkanScope UI parity missing: '+token)
 for token in ['INFO("Info"','REPORTS_DATABASE("Reports & Database"','UPDATE_PREFERENCES("Update preferences"']:
  require(token in main,'Settings destination parity missing: '+token)
 for token in ['ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT = 100','ENCYCLOPEDIA_MAX_QUERY_LENGTH = 160','largeFamilyNeedsQuery','Dispatchers.IO','take(ENCYCLOPEDIA_VISIBLE_RESULT_LIMIT + 1)','catalogError']:
  require(token in main,'Encyclopedia stability guard missing: '+token)
 for token in ['MAX_CATALOG_BYTES = 2 * 1024 * 1024','MAX_CATALOG_ENTRIES = 6_000','readBoundedCatalog','Duplicate registry entry','seen.add']:
  require(token in reg,'catalog bound/integrity guard missing: '+token)
 require((ROOT/'rules/1.7.0_VULKANSCOPE_VISUAL_INTERACTION_PARITY_AND_ENCYCLOPEDIA_STABILITY_AUDIT.md').is_file(),'1.7.0 audit missing')
 require('Vulkan-only' in (ROOT/'rules/PROJECT_RULES.md').read_text(),'API boundary missing')
if __name__=='__main__': main_guard('verify_1_7_0_quality',verify)
