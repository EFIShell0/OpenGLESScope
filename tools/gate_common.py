from __future__ import annotations
import os
from pathlib import Path

ROOT = Path(os.environ.get('OPENGLESSCOPE_ROOT', Path(__file__).resolve().parents[1])).resolve()

def text(rel: str) -> str:
    return (ROOT / rel).read_text(encoding='utf-8')

def require(cond: bool, message: str) -> None:
    if not cond:
        raise AssertionError(message)

def main_guard(name: str, fn) -> None:
    try:
        fn()
    except Exception as exc:
        print(f'{name}: FAIL')
        print(f' - {exc}')
        raise SystemExit(1)
    print(f'{name}: PASS')
