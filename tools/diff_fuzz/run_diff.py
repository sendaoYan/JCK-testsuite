#!/usr/bin/env python3
"""Differential fuzzing driver: JDK-8 vs JDK-27.

Builds DiffFuzz.java once (with JDK-8 javac), runs it under both JDKs with a
fixed seed, and diffs the normalized per-case stdout (skipping META lines).

Determinism: DiffFuzz generates all inputs from a fixed Random seed, so both
runs emit the identical line sequence; only genuinely different behaviour can
make a line differ.

Usage:
    python3 run_diff.py [--jdk8 /path/to/jdk8/bin/java] \
                        [--jdk27 /path/to/jdk27/bin/java]

Outputs (under tools/diff_fuzz/out/):
    jdk8.txt, jdk27.txt   raw per-JDK traces
    diff.txt              human-readable divergences
    diff.json             machine-readable divergences + per-category counts
"""

import argparse
import json
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "out")

DEFAULT_JDK8 = "/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/java"
DEFAULT_JDK27 = "/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1/bin/java"


def run(cmd):
    proc = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if proc.returncode != 0:
        sys.stderr.write("command failed: %s\n" % " ".join(cmd))
        sys.stderr.write(proc.stderr.decode("utf-8", "replace"))
        sys.exit(proc.returncode)
    return proc.stdout.decode("utf-8", "replace")


def parse_line(line):
    # category, index, input, result
    parts = line.split("\t", 3)
    if len(parts) != 4:
        return None
    return parts[0], parts[1], parts[2], parts[3]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jdk8", default=DEFAULT_JDK8)
    ap.add_argument("--jdk27", default=DEFAULT_JDK27)
    args = ap.parse_args()

    os.makedirs(OUT, exist_ok=True)
    javac8 = os.path.join(os.path.dirname(args.jdk8), "javac")
    src = os.path.join(HERE, "DiffFuzz.java")

    # Build once with JDK-8 so the class runs on both JDKs.
    run([javac8, "-encoding", "UTF-8", "-d", OUT, src])

    out8 = run([args.jdk8, "-Dfile.encoding=UTF-8", "-cp", OUT, "DiffFuzz"])
    out27 = run([args.jdk27, "-Dfile.encoding=UTF-8", "-cp", OUT, "DiffFuzz"])

    with open(os.path.join(OUT, "jdk8.txt"), "w") as f:
        f.write(out8)
    with open(os.path.join(OUT, "jdk27.txt"), "w") as f:
        f.write(out27)

    lines8 = [l for l in out8.splitlines() if not l.startswith("META\t")]
    lines27 = [l for l in out27.splitlines() if not l.startswith("META\t")]

    meta8 = [l for l in out8.splitlines() if l.startswith("META\t")]
    meta27 = [l for l in out27.splitlines() if l.startswith("META\t")]

    n = max(len(lines8), len(lines27))
    divergences = []
    cat_total = {}
    cat_div = {}

    for i in range(n):
        if i >= len(lines8) or i >= len(lines27):
            # length mismatch (should not happen with deterministic gen)
            a = lines8[i] if i < len(lines8) else "<missing>"
            b = lines27[i] if i < len(lines27) else "<missing>"
            divergences.append({"line": i, "jdk8": a, "jdk27": b,
                                "cat": "LEN-MISMATCH", "idx": "", "input": "", "kind": "length"})
            continue
        a = parse_line(lines8[i])
        b = parse_line(lines27[i])
        if a is None or b is None:
            divergences.append({"line": i, "jdk8": lines8[i], "jdk27": lines27[i],
                                "cat": "PARSE", "idx": "", "input": "", "kind": "parse"})
            continue
        cat = a[0]
        cat_total[cat] = cat_total.get(cat, 0) + 1
        # input + index must match (deterministic generation); only result may differ
        if a[1] != b[1] or a[2] != b[2]:
            divergences.append({"line": i, "cat": cat, "idx": a[1], "input": a[2],
                                "jdk8": lines8[i], "jdk27": lines27[i],
                                "kind": "input-drift"})
            continue
        if a[3] != b[3]:
            cat_div[cat] = cat_div.get(cat, 0) + 1
            divergences.append({
                "line": i, "cat": cat, "idx": a[1], "input": a[2],
                "jdk8": a[3], "jdk27": b[3], "kind": classify(a[3], b[3]),
            })

    report = {
        "jdk8_meta": meta8,
        "jdk27_meta": meta27,
        "total_cases": n,
        "divergence_count": len(divergences),
        "per_category_total": dict(sorted(cat_total.items())),
        "per_category_divergences": dict(sorted(cat_div.items())),
        "divergences": divergences,
    }

    with open(os.path.join(OUT, "diff.json"), "w") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)

    with open(os.path.join(OUT, "diff.txt"), "w") as f:
        f.write("JDK-8  : %s\n" % " ".join(meta8))
        f.write("JDK-27 : %s\n" % " ".join(meta27))
        f.write("total cases: %d  divergences: %d\n\n" % (n, len(divergences)))
        f.write("per-category divergence counts:\n")
        for c, d in sorted(cat_div.items()):
            f.write("  %-28s %d / %d\n" % (c, d, cat_total.get(c, 0)))
        f.write("\n")
        for d in divergences:
            f.write("---- line %s  [%s/%s]  kind=%s\n" % (d["line"], d.get("cat"), d.get("idx"), d.get("kind")))
            f.write("  input : %s\n" % d.get("input", ""))
            f.write("  jdk8  : %s\n" % d["jdk8"])
            f.write("  jdk27 : %s\n" % d["jdk27"])

    print("JDK-8  :", " ".join(meta8))
    print("JDK-27 :", " ".join(meta27))
    print("total cases:", n, " divergences:", len(divergences))
    print("per-category divergence counts:")
    for c, d in sorted(cat_div.items()):
        print("  %-28s %d / %d" % (c, d, cat_total.get(c, 0)))
    print("\nartifacts under", OUT)


def classify(a, b):
    def exc(s):
        return s if s.startswith("EXC=") else ("ok" if s.startswith("ok=") else s)
    ea, eb = exc(a), exc(b)
    if ea != eb:
        if ea.startswith("EXC=") and eb.startswith("EXC="):
            return "exception-type"
        if ea.startswith("EXC=") or eb.startswith("EXC="):
            return "throw-vs-value"
        return "value"
    return "value"


if __name__ == "__main__":
    main()
