#!/usr/bin/env python3
"""
diff_existing_suite.py - JDK-8 vs JDK-27 differential harness over the existing
behavioral API test suite (reuse, not hand-written probes).

Unlike diff_behavior.py (hand-written probes emitting key=value lines), this
harness reuses the suite's own black-box tests.  Each discovered test is a
`*Test.java` whose main() ends with `System.out.println("PASS (N assertions)")`.
The only guaranteed output is that line (plus, on failure, an AssertionError
stack trace on stderr), so the differential signal is:

  * the assertion count N differing between the two JDKs, or
  * one JDK passing while the other crashes (run rc differs), or
  * any extra stdout line differing.

For every discovered test class:
  1. compile the JDK-8 tree's source with each JDK's javac,
  2. run it with each JDK's java (headless, fixed Locale/TimeZone/Charset),
  3. diff the two normalized stdout streams + run exit codes.

Source selection: the JDK-8 tree's file is used as the canonical source, because
JDK-8 is the API subset most likely to compile under both JDKs.  (The JDK-27
tree often adds tests for JDK-19+ methods that would never compile on JDK-8, so
it is not a usable cross-version probe.)  Using the JDK-8 source therefore
captures *runtime* drift on the shared API surface and *removed* APIs, while
newer-only APIs are left to tools/enumerate_api.py.

Automatic categories:
  consistent        both compile+run, identical stdout (same N)
  divergent         both compile, but stdout/N differs OR one run passes while
                    the other exits non-zero (the drift we are hunting)
  spec_added        compiles only on JDK-27 (should not happen for JDK-8 source)
  spec_removed      compiles only on JDK-8  (source uses an API removed in 27)
  run_error         compiles on both, but both runs exit non-zero
  both_compile_fail broken on both sides
  skipped           excluded up front (@key headful/sound, @compile/fail,
                    @build/@library/@modules, native library tests)

Determinism contract (enforced here, not just by convention): runs are launched
with -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US
-Duser.timezone=GMT so default Locale/TimeZone/Charset are fixed regardless of
the machine environment.

Usage:
  python3 diff_existing_suite.py [--packages java/util,java/text] [--limit 20]
                                 [--classify classify.json] [--md OUT.md]
                                 [--json OUT.json]
"""

import argparse
import difflib
import json
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
REPO = ROOT.parent.parent  # .../JCK-testsuite
API8 = REPO / "JDK-8" / "API"
API27 = REPO / "JDK-27" / "API"

PASS_RE = re.compile(r"PASS \((\d+) assertions\)")

DEFAULT_PACKAGES = [
    "java/lang",
    "java/util",
    "java/text",
    "java/math",
    "java/time",
    "java/util/regex",
    "java/nio",
]

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

JAVA_OPTS = ["-Dfile.encoding=UTF-8", "-Duser.language=en",
             "-Duser.country=US", "-Duser.timezone=GMT"]

# A test whose run line needs a real native library (built from embedded C at
# run time) is not a valid single-javac/java probe on an arbitrary machine.
NATIVE_STEMS = {"SystemLoadNativeTest", "RuntimeLoadNativeTest"}

SKIP_KEY_TAGS = ("headful", "sound", "printer", "multimon", "intermittent")


def discover(packages, limit):
    """Return a list of (relpath, abspath) for *Test.java files that print PASS."""
    found = []
    for pkg in packages:
        pkg_dir = API8 / pkg
        if not pkg_dir.is_dir():
            print(f"warning: package dir missing: {pkg_dir}", file=sys.stderr)
            continue
        for path in sorted(pkg_dir.rglob("*Test.java")):
            try:
                text = path.read_text(encoding="utf-8", errors="replace")
            except OSError:
                continue
            if "PASS (" not in text:
                continue
            rel = path.relative_to(API8).as_posix()
            found.append((rel, path))
    if limit:
        found = found[:limit]
    return found


def skip_reason(rel, text):
    """Return a short reason to skip, or None if the test should run."""
    if "NativeBindings" in rel:
        return "native-jni"
    stem = rel.rsplit("/", 1)[-1][:-len(".java")]
    if stem in NATIVE_STEMS:
        return "native-load"
    if "@compile/fail" in text or "@compile/ref" in text:
        return "compile-fail"
    if "@build" in text:
        return "build"
    if "@library" in text:
        return "library"
    if "@modules" in text:
        return "modules"
    m = re.search(r"@key\s+([^\n*]+)", text)
    if m:
        tags = m.group(1).split()
        for t in tags:
            if t in SKIP_KEY_TAGS:
                return f"key:{t}"
    return None


def normalize(text):
    out = []
    for ln in text.splitlines():
        ln = ln.rstrip("\r").rstrip()
        if not ln:
            continue
        out.append(ln)
    return sorted(set(out))


def compile_one(src, key, workdir):
    workdir.mkdir(parents=True, exist_ok=True)
    cmd = [JDKS[key]["javac"], "-encoding", "UTF-8", "-d", str(workdir), str(src)]
    p = subprocess.run(cmd, capture_output=True, text=True)
    return p.returncode, (p.stdout + "\n" + p.stderr).strip()


def run_one(class_name, key, workdir):
    cmd = [JDKS[key]["java"], *JAVA_OPTS, "-cp", str(workdir), class_name]
    try:
        p = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
        return p.returncode, p.stdout, p.stderr
    except subprocess.TimeoutExpired as e:
        # TimeoutExpired.stdout/stderr are bytes even under text=True.
        def dec(v):
            if v is None:
                return ""
            return v if isinstance(v, str) else v.decode(errors="replace")
        return 124, dec(e.stdout), dec(e.stderr)


def tail(s, n=6):
    lines = [x for x in s.strip().splitlines() if x.strip()]
    return lines[-n:]


def evaluate(rel, src):
    res = {"test": rel, "class": src.stem, "jdk8": {}, "jdk27": {}}
    workdirs = {}
    for key in ("jdk8", "jdk27"):
        wd = Path(tempfile.gettempdir()) / f"diff_existing_{src.stem}_{key}"
        wd.mkdir(parents=True, exist_ok=True)
        workdirs[key] = wd
        rc, msg = compile_one(src, key, wd)
        res[key]["compile_rc"] = rc
        res[key]["compile_msg"] = msg
        res[key]["run_rc"] = None
        res[key]["n"] = None
        res[key]["stdout"] = ""
        res[key]["stderr"] = ""
        if rc == 0:
            rrc, out, err = run_one(src.stem, key, wd)
            res[key]["run_rc"] = rrc
            res[key]["stdout"] = out
            res[key]["stderr"] = err
            m = PASS_RE.search(out)
            res[key]["n"] = int(m.group(1)) if m else None

    c8 = res["jdk8"]["compile_rc"]
    c27 = res["jdk27"]["compile_rc"]
    if c8 != 0 and c27 != 0:
        res["category"] = "both_compile_fail"
        res["detail"] = (res["jdk8"]["compile_msg"].splitlines() or ["?"])[-1]
        return res
    if c8 != 0 and c27 == 0:
        res["category"] = "spec_added"
        res["detail"] = "compiles on JDK-27 only"
        return res
    if c8 == 0 and c27 != 0:
        res["category"] = "spec_removed"
        res["detail"] = "compiles on JDK-8 only: "
        res["detail"] += (res["jdk27"]["compile_msg"].splitlines() or ["?"])[-1]
        return res

    r8 = res["jdk8"]["run_rc"]
    r27 = res["jdk27"]["run_rc"]
    n8 = res["jdk8"]["n"]
    n27 = res["jdk27"]["n"]

    if r8 != 0 and r27 != 0:
        res["category"] = "run_error"
        res["detail"] = f"run rc jdk8={r8} jdk27={r27}"
        res["stderr_jdk8_tail"] = tail(res["jdk8"]["stderr"])
        res["stderr_jdk27_tail"] = tail(res["jdk27"]["stderr"])
        return res

    if r8 != 0 or r27 != 0:
        res["category"] = "divergent"
        res["detail"] = f"run rc jdk8={r8} jdk27={r27} (one side crashes)"
        res["stderr_jdk8_tail"] = tail(res["jdk8"]["stderr"])
        res["stderr_jdk27_tail"] = tail(res["jdk27"]["stderr"])
        return res

    l8 = normalize(res["jdk8"]["stdout"])
    l27 = normalize(res["jdk27"]["stdout"])
    if l8 == l27:
        res["category"] = "consistent"
        res["detail"] = f"N={n8}"
        return res

    res["category"] = "divergent"
    res["only_jdk8"] = sorted(set(l8) - set(l27))
    res["only_jdk27"] = sorted(set(l27) - set(l8))
    res["detail"] = (f"N jdk8={n8} jdk27={n27}; "
                     f"{len(res['only_jdk8'])} lines only on JDK-8, "
                     f"{len(res['only_jdk27'])} lines only on JDK-27")
    return res


def main():
    ap = argparse.ArgumentParser(description="JDK-8 vs JDK-27 diff over existing suite")
    ap.add_argument("--packages", default=",".join(DEFAULT_PACKAGES),
                    help="comma-separated package paths relative to JDK-8/API")
    ap.add_argument("--limit", type=int, default=0, help="only the first N discovered tests")
    ap.add_argument("--json", default=str(ROOT / "results_existing.json"))
    ap.add_argument("--md", default=str(ROOT / "DIFF_EXISTING.md"))
    ap.add_argument("--classify", default=None,
                    help="optional JSON mapping test relpath -> {class, note} for the 3-way md")
    ap.add_argument("--resume", default=None,
                    help="path to a prior results JSON; tests already present are skipped")
    args = ap.parse_args()

    packages = [p.strip() for p in args.packages.split(",") if p.strip()]
    tests = discover(packages, args.limit)

    results = []
    done = set()
    if args.resume and Path(args.resume).exists():
        for r in json.loads(Path(args.resume).read_text(encoding="utf-8")):
            results.append(r)
            done.add(r["test"])

    pending = [(rel, src) for rel, src in tests if rel not in done]
    print(f"discovered {len(tests)} tests ({len(done)} already done, {len(pending)} pending)")

    out_json = Path(args.json)
    for i, (rel, src) in enumerate(pending, 1):
        text = src.read_text(encoding="utf-8", errors="replace")
        why = skip_reason(rel, text)
        if why:
            results.append({"test": rel, "class": src.stem, "category": "skipped",
                            "skip_reason": why, "detail": why})
            print(f"[{i}/{len(pending)}] {rel}: skipped ({why})", flush=True)
        else:
            r = evaluate(rel, src)
            results.append(r)
            print(f"[{i}/{len(pending)}] {rel}: {r['category']}  {r['detail']}", flush=True)
        # incremental write so a crash never loses completed work
        out_json.write_text(json.dumps(results, indent=2) + "\n", encoding="utf-8")

    by_cat = {}
    for r in results:
        by_cat.setdefault(r["category"], []).append(r["test"])

    print(f"\ntests: {len(results)}")
    for cat in ("consistent", "divergent", "spec_added", "spec_removed",
                "run_error", "both_compile_fail", "skipped"):
        items = by_cat.get(cat, [])
        print(f"  {cat}: {len(items)}")
        for name in items:
            r = next(x for x in results if x["test"] == name)
            print(f"    - {name}  {r['detail']}")

    out_json = Path(args.json)
    out_json.write_text(json.dumps(results, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {out_json}")

    classify = None
    if args.classify and Path(args.classify).exists():
        classify = json.loads(Path(args.classify).read_text(encoding="utf-8"))
    write_markdown(results, Path(args.md), classify)
    print(f"wrote {args.md}")


def write_markdown(results, path, classify):
    classify = classify or {}
    lines = ["# JDK-8 vs JDK-27 现有行为化用例差分清单", "",
             "> 由 `tools/diff_jdk8_27/diff_existing_suite.py` 生成。",
             f"> 共 {len(results)} 个测试类。",
             "> 三分类：`expected`（规范允许的进化，测试已用公共父类/contains 处理）、",
             "> `candidate`（真实未覆盖分支 → 新测试场景）、`env`（环境噪声，忽略）。",
             ""]

    buckets = {"expected": [], "candidate": [], "env": []}
    unclassified = []
    for r in results:
        cls = classify.get(r["test"], {}).get("class")
        if cls in buckets:
            buckets[cls].append(r)
        elif r["category"] in ("divergent", "spec_added", "spec_removed"):
            unclassified.append(r)

    for cls, title, note in (
        ("candidate", "candidate — 真实未覆盖分支", "需补新测试场景"),
        ("expected", "expected — 规范允许的进化", "测试已用公共父类/contains 处理"),
        ("env", "env — 环境噪声", "忽略"),
    ):
        items = buckets[cls]
        lines.append(f"## {title} ({len(items)})")
        lines.append("")
        if not items:
            lines.append("（无）")
            lines.append("")
            continue
        for r in items:
            entry = classify.get(r["test"], {})
            lines.append(f"### {r['test']}")
            lines.append(f"- 类别：`{r['category']}`")
            lines.append(f"- 差异：{r.get('detail','')}")
            if entry.get("note"):
                lines.append(f"- 说明：{entry['note']}")
            if r.get("only_jdk8"):
                lines.append("- 仅 JDK-8 输出：")
                lines.extend(f"  - `{x}`" for x in r["only_jdk8"][:20])
            if r.get("only_jdk27"):
                lines.append("- 仅 JDK-27 输出：")
                lines.extend(f"  - `{x}`" for x in r["only_jdk27"][:20])
            if r.get("stderr_jdk8_tail"):
                lines.append("- JDK-8 stderr 尾：")
                lines.extend(f"  - `{x}`" for x in r["stderr_jdk8_tail"])
            if r.get("stderr_jdk27_tail"):
                lines.append("- JDK-27 stderr 尾：")
                lines.extend(f"  - `{x}`" for x in r["stderr_jdk27_tail"])
            lines.append("")

    if unclassified:
        lines.append(f"## 待分类分歧 ({len(unclassified)})")
        lines.append("")
        for r in unclassified:
            lines.append(f"- `{r['test']}` — `{r['category']}`: {r.get('detail','')}")
        lines.append("")

    # consistent / other summary
    by_cat = {}
    for r in results:
        by_cat.setdefault(r["category"], []).append(r["test"])
    lines.append("## 分类统计")
    lines.append("")
    for cat in ("consistent", "divergent", "spec_added", "spec_removed",
                "run_error", "both_compile_fail", "skipped"):
        n = len(by_cat.get(cat, []))
        lines.append(f"- `{cat}`: {n}")
    lines.append("")

    path.write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    main()
