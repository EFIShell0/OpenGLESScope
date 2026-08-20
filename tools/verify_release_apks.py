from pathlib import Path
from zipfile import ZipFile
import re
import shutil

root = Path(__file__).resolve().parents[1]
outputs = root / "app" / "build" / "outputs"
canonical = root / "app" / "build" / "outputs" / "releaseArtifacts"
expected = {
    "universal": {"arm64-v8a", "armeabi-v7a", "x86_64"},
    "arm64-v8a": {"arm64-v8a"},
    "armeabi-v7a": {"armeabi-v7a"},
    "x86_64": {"x86_64"},
}

def apk_abis(apk: Path) -> set[str]:
    with ZipFile(apk) as archive:
        return {
            match.group(1)
            for name in archive.namelist()
            if (match := re.match(r"lib/([^/]+)/[^/]+\.so$", name))
        }

apks = sorted(apk for apk in outputs.rglob("*.apk") if canonical not in apk.parents)
if not apks:
    raise SystemExit(f"No release APKs found under {outputs}")
classified = {apk: apk_abis(apk) for apk in apks}
if any("x86" in abis for abis in classified.values()):
    bad = [apk.name for apk, abis in classified.items() if "x86" in abis]
    raise SystemExit(f"x86 must not be present in release APKs: {bad}")
selected = {}
for key, required in expected.items():
    matches = [apk for apk, abis in classified.items() if abis == required]
    if len(matches) != 1:
        details = {apk.name: sorted(abis) for apk, abis in classified.items()}
        raise SystemExit(f"Expected exactly one {key} APK with ABIs {sorted(required)}, found {[x.name for x in matches]}; outputs={details}")
    selected[key] = matches[0]
canonical.mkdir(parents=True, exist_ok=True)
for old in canonical.glob("*.apk"):
    old.unlink()
for key, source in selected.items():
    target = canonical / f"OpenGLESScope-0.1.20-{key}-release.apk"
    shutil.copy2(source, target)
    if apk_abis(target) != expected[key]:
        raise SystemExit(f"Canonical artifact failed ABI verification: {target.name}")
    print(f"{key}: {source.name} -> {target.name}")
