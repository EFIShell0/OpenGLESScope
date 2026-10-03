#!/usr/bin/env python3
from __future__ import annotations
import re, xml.etree.ElementTree as ET
from pathlib import Path
from gate_common import ROOT, require, main_guard

TOKEN_LITERAL_RE = re.compile(r'"((?:GL|EGL)_[A-Za-z0-9_]+)"')


def registry_index(path: Path, prefix: str):
    root = ET.parse(path).getroot()
    names: set[str] = set()
    values: dict[str, set[int]] = {}
    for group in root.findall('enums'):
        for enum in group.findall('enum'):
            name = enum.get('name')
            if not name or not name.startswith(prefix):
                continue
            names.add(name)
            value = enum.get('value')
            if value is None:
                continue
            try:
                iv = int(value, 0)
            except (TypeError, ValueError):
                continue
            values.setdefault(name, set()).add(iv)
    extensions = {
        ext.get('name') for ext in root.findall('./extensions/extension')
        if ext.get('name') and ext.get('name').startswith(prefix)
    }
    return root, names, values, extensions


def verify():
    _, gl_names, gl_values, gl_extensions = registry_index(ROOT/'registry/gl.xml', 'GL_')
    _, egl_names, egl_values, egl_extensions = registry_index(ROOT/'registry/egl.xml', 'EGL_')

    source_paths = [ROOT/'app/src/main/cpp/openglesscope.cpp'] + sorted((ROOT/'app/src/main/java').rglob('*.kt'))
    sources = {p: p.read_text(encoding='utf-8') for p in source_paths}
    native = sources[ROOT/'app/src/main/cpp/openglesscope.cpp']

    allowed_non_enums = {
        # glGetString/glGetStringi symbolic query names are canonical API tokens in the registry,
        # but retain this explicit set as a fail-readable guard for older registry snapshots.
        'GL_VENDOR','GL_RENDERER','GL_VERSION','GL_SHADING_LANGUAGE_VERSION','GL_EXTENSIONS','GL_MAX_',
    }

    bad_literals: list[str] = []
    for path, text in sources.items():
        for token in sorted(set(TOKEN_LITERAL_RE.findall(text))):
            if token.startswith('GL_'):
                if token not in gl_names and token not in gl_extensions and token not in allowed_non_enums:
                    bad_literals.append(f'{path.relative_to(ROOT)}:{token}')
            elif token.startswith('EGL_'):
                if token not in egl_names and token not in egl_extensions:
                    bad_literals.append(f'{path.relative_to(ROOT)}:{token}')
    require(not bad_literals, 'fabricated/unregistered GL_/EGL_ literals presented as canonical names: '+', '.join(bad_literals[:40]))

    # Numeric display pairs are especially dangerous: a plausible token can be paired with a stale
    # or copied numeric value. Validate every hard-coded GL/EGL numeric pair against the locked XML.
    bad_pairs: list[str] = []
    for prefix, values, extensions in [('GL_', gl_values, gl_extensions), ('EGL_', egl_values, egl_extensions)]:
        for m in re.finditer(r'"('+re.escape(prefix)+r'[A-Z0-9_]+)"\s*,\s*(0x[0-9A-Fa-f]+)', native):
            name = m.group(1); value = int(m.group(2), 0)
            if name in values and value not in values[name]:
                bad_pairs.append(f'{name}={m.group(2)} expected '+"/".join(hex(v) for v in sorted(values[name])))
            elif name not in values and name not in extensions:
                bad_pairs.append(f'{name}={m.group(2)} is not a registered enum with a simple numeric value')
    require(not bad_pairs, 'hard-coded GL/EGL enum/value mismatch: '+', '.join(bad_pairs))

    # Known historical/fabricated aliases must never return as user-visible canonical-looking labels.
    forbidden = {
      'GL_COMPRESSED_LUMINANCE_LATC1_NV','GL_COMPRESSED_SIGNED_LUMINANCE_LATC1_NV',
      'GL_COMPRESSED_LUMINANCE_ALPHA_LATC2_NV','GL_COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_NV',
      'GL_NVIDIA_PLATFORM_BINARY_NV','GL_TIME_ELAPSED_EXT_QUERY_COUNTER_BITS','GL_TIMESTAMP_EXT_QUERY_COUNTER_BITS',
      'EGL_ERROR',
    }
    all_source = '\n'.join(sources.values())
    require(not any(('"'+x+'"') in all_source for x in forbidden), 'known fabricated/historical aliases remain in native/report labels')

    displayed = set(re.findall(r'"(GL_[A-Z0-9_]+) \("', native))
    required = {
      'GL_COMPRESSED_LUMINANCE_LATC1_EXT','GL_COMPRESSED_SIGNED_LUMINANCE_LATC1_EXT',
      'GL_COMPRESSED_LUMINANCE_ALPHA_LATC2_EXT','GL_COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_EXT'
    }
    require(required <= displayed, 'canonical LATC EXT names not restored')
    require('Query counter bits: GL_TIME_ELAPSED_EXT' in native and 'Query counter bits: GL_TIMESTAMP_EXT' in native,
            'timer query counter-bit evidence labels must be descriptive rather than fabricated GL tokens')
    require('default: return "Unknown EGL error";' in native,
            'unknown EGL errors must use descriptive text rather than fabricated canonical-looking EGL_ERROR')

if __name__=='__main__':
    main_guard('verify_registry_canonical_names', verify)
