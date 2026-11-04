#!/usr/bin/env python3
"""
JDK-8 vs JDK-27 differential behavior harness.

For every probe in probes/*.java:
  1. compile it with each JDK's javac,
  2. run it with each JDK's java (headless, fixed default charset),
  3. collect stdout lines, normalize (sort + dedupe, so hash/iteration order is
     irrelevant), and diff the two normalized outputs.

Categorization:
  consistent        both compile+run and emit identical normalized output
  divergent         both compile+run but outputs differ
  spec_added        JDK-8 compile fails, JDK-27 compiles+runs  (newer-only API)
  spec_removed      JDK-27 compile fails, JDK-8 compiles+runs  (older-only API)
  both_compile_fail probe is broken on both sides (not a JDK difference)

Determinism contract for probes (enforced by convention, not by the harness):
  - no wall-clock, no random, fixed Locale/TimeZone/Charset,
  - emit one `key=value` line per case; on exception emit `key=exception=<type>`,
  - never let an exception escape main.

Usage:
  python3 diff_behavior.py              # run all probes, write results.json + console report
  python3 diff_behavior.py --probe IntegerParseProbe
  python3 diff_behavior.py --md docs/diff-jdk8-jdk27.md   # also emit a markdown skeleton
"""

import argparse
import difflib
import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PROBES_DIR = ROOT / "probes"
WORK_DIR = ROOT / "work"
RESULTS_JSON = ROOT / "results.json"

JDKS = {
    "jdk8": {
        "java": "/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/java",
        "javac": "/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javac",
        "label": "JDK-8 (1.8.0_372)",
    },
    "jdk27": {
        "java": "/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1/bin/java",
        "javac": "/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1/bin/javac",
        "label": "JDK-27 (27.0.1)",
    },
}


def normalize(text: str) -> list[str]:
    """Return a canonical, order-insensitive list of output lines."""
    out = []
    for ln in text.splitlines():
        ln = ln.rstrip("\r").rstrip()
        if not ln:
            continue
        out.append(ln)
    # Sort + dedupe: the probes' keys are unique, so this only collapses
    # duplicate lines and neutralizes any map/iteration ordering noise.
    return sorted(set(out))


def compile_probe(probe: Path, key: str):
    workdir = WORK_DIR / f"{probe.stem}_{key}"
    workdir.mkdir(parents=True, exist_ok=True)
    cmd = [JDKS[key]["javac"], "-encoding", "UTF-8", "-d", str(workdir), str(probe)]
    p = subprocess.run(cmd, capture_output=True, text=True)
    return workdir, p.returncode, (p.stdout + "\n" + p.stderr).strip()


def run_probe(probe: Path, key: str, workdir: Path):
    cmd = [
        JDKS[key]["java"],
        "-Dfile.encoding=UTF-8",
        "-cp",
        str(workdir),
        probe.stem,
    ]
    try:
        p = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
        return p.returncode, p.stdout, p.stderr
    except subprocess.TimeoutExpired as e:
        return 124, (e.stdout or b"").decode(errors="replace"), (e.stderr or b"").decode(errors="replace")


def evaluate(probe: Path):
    result = {"probe": probe.stem, "jdk8": {}, "jdk27": {}}
    for key in ("jdk8", "jdk27"):
        workdir, rc, msg = compile_probe(probe, key)
        result[key]["compile_rc"] = rc
        result[key]["compile_msg"] = msg
        if rc != 0:
            result[key]["run_rc"] = None
            result[key]["lines"] = []
            result[key]["stderr"] = ""
            continue
        run_rc, stdout, stderr = run_probe(probe, key, workdir)
        result[key]["run_rc"] = run_rc
        result[key]["lines"] = normalize(stdout)
        result[key]["stderr"] = stderr.strip()

    c8, c27 = result["jdk8"]["compile_rc"], result["jdk27"]["compile_rc"]
    if c8 != 0 and c27 != 0:
        result["category"] = "both_compile_fail"
        result["detail"] = result["jdk8"]["compile_msg"].splitlines()[-1] if result["jdk8"]["compile_msg"] else "?"
        return result
    if c8 != 0 and c27 == 0:
        result["category"] = "spec_added"
        result["detail"] = "compiles on JDK-27 only"
        return result
    if c8 == 0 and c27 != 0:
        result["category"] = "spec_removed"
        result["detail"] = "compiles on JDK-8 only"
        return result

    # Both compile. Run must have succeeded for a meaningful comparison.
    r8, r27 = result["jdk8"]["run_rc"], result["jdk27"]["run_rc"]
    if r8 != 0 or r27 != 0:
        result["category"] = "run_error"
        result["detail"] = f"run rc jdk8={r8} jdk27={r27}"
        return result

    l8, l27 = result["jdk8"]["lines"], result["jdk27"]["lines"]
    if l8 == l27:
        result["category"] = "consistent"
        result["detail"] = ""
    else:
        result["category"] = "divergent"
        result["only_jdk8"] = sorted(set(l8) - set(l27))
        result["only_jdk27"] = sorted(set(l27) - set(l8))
        result["detail"] = f"{len(result['only_jdk8'])} lines only on JDK-8, {len(result['only_jdk27'])} lines only on JDK-27"
    return result


def main():
    ap = argparse.ArgumentParser(description="JDK-8 vs JDK-27 differential harness")
    ap.add_argument("--probe", help="run only this probe (stem without .java)")
    ap.add_argument("--md", help="also write a markdown skeleton to this path")
    args = ap.parse_args()

    probes = sorted(PROBES_DIR.glob("*.java"))
    if args.probe:
        probes = [p for p in probes if p.stem == args.probe]
        if not probes:
            sys.exit(f"probe not found: {args.probe}")

    results = []
    for p in probes:
        r = evaluate(p)
        results.append(r)
        print(f"[{len(results)}/{len(probes)}] {p.stem}: {r['category']}  {r['detail']}", flush=True)

    # Console summary
    by_cat = {}
    for r in results:
        by_cat.setdefault(r["category"], []).append(r["probe"])
    print(f"probes: {len(results)}")
    for cat in ("consistent", "divergent", "spec_added", "spec_removed", "run_error", "both_compile_fail"):
        items = by_cat.get(cat, [])
        print(f"  {cat}: {len(items)}")
        for name in items:
            r = next(x for x in results if x["probe"] == name)
            print(f"    - {name}  {r['detail']}")

    RESULTS_JSON.write_text(json.dumps(results, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {RESULTS_JSON}")

    if args.md:
        write_markdown(results, Path(args.md))
        print(f"wrote {args.md}")


def write_markdown(results, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    lines = ["# JDK-8 vs JDK-27 行为差分清单", "",
             f"> 由 `tools/diff_jdk8_27/diff_behavior.py` 生成骨架；判定列需人工复核。",
             f"> 探针数：{len(results)}", ""]
    by_cat = {}
    for r in results:
        by_cat.setdefault(r["category"], []).append(r)
    for cat, title in (
        ("divergent", "分歧"),
        ("spec_added", "规范新增"),
        ("spec_removed", "规范移除"),
        ("run_error", "运行错误"),
        ("both_compile_fail", "双方编译失败"),
    ):
        items = by_cat.get(cat, [])
        if not items:
            continue
        lines.append(f"## {title} ({len(items)})")
        lines.append("")
        for r in items:
            lines.append(f"### {r['probe']}")
            lines.append(f"- 类别：`{r['category']}`")
            lines.append(f"- 判定：**待填**（规范允许进化 / 候选回归）")
            if r.get("only_jdk8"):
                lines.append("- 仅 JDK-8 输出：")
                lines.extend(f"  - `{x}`" for x in r["only_jdk8"][:20])
            if r.get("only_jdk27"):
                lines.append("- 仅 JDK-27 输出：")
                lines.extend(f"  - `{x}`" for x in r["only_jdk27"][:20])
            lines.append("")
    lines.append("## 一致（省略详情）")
    cons = [r["probe"] for r in results if r["category"] == "consistent"]
    lines.append("")
    lines.append(", ".join(f"`{c}`" for c in cons))
    lines.append("")
    path.write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    main()
