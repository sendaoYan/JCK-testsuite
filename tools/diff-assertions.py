#!/usr/bin/env python3
"""diff-assertions.py - cross-run differential harness.

Compares two jtreg work directories (trees of .jtr files) and reports, per
test, differences in status and in the "PASS (N assertions)" count printed by
this suite's tests. Purpose: surface behavioral drift between environments
(x86 vs riscv64/qemu, c1/c2 vs -Xcomp, jdk8 vs jdk27) that pass/fail counts
alone cannot see - two runs can both be green yet assert different amounts.

Usage: diff-assertions.py <workdirA> <workdirB> [--verbose]
Exit code 0 when no drift, 1 otherwise.
"""
import os, re, sys

PASS_RE = re.compile(r"PASS \((\d+) assertions\)")
# .jtr header carries the overall verdict as "execStatus=Passed." or
# "execStatus=Failed. Execution failed: ..." (jtreg >= 5 / JavaTest result v2)
EXEC_RE = re.compile(r"^execStatus=(Passed|Failed|Error)\b", re.M)
# per-section verdict lines: "result: Passed. Build successful" etc.
SECTION_RE = re.compile(r"^result: (Passed|Failed|Error)\.", re.M)


def scan(root):
    out = {}
    for dirpath, _, files in os.walk(root):
        for f in files:
            if not f.endswith(".jtr"):
                continue
            p = os.path.join(dirpath, f)
            rel = os.path.relpath(p, root)
            try:
                text = open(p, errors="replace").read()
            except OSError:
                continue
            n = PASS_RE.search(text)
            m = EXEC_RE.search(text)
            if m:
                status = m.group(1)
            else:
                sections = SECTION_RE.findall(text)
                # a .jtr carries several sections (build/compile/main);
                # overall pass == no Failed/Error section
                status = "Failed" if ("Failed" in sections or "Error" in sections) \
                    else ("Passed" if "Passed" in sections else "NoVerdict")
            out[rel] = (status, int(n.group(1)) if n else None)
    return out


def main():
    a_root, b_root = sys.argv[1], sys.argv[2]
    verbose = "--verbose" in sys.argv
    A, B = scan(a_root), scan(b_root)
    only_a = sorted(set(A) - set(B))
    only_b = sorted(set(B) - set(A))
    drift = []
    for k in sorted(set(A) & set(B)):
        sa, na = A[k]
        sb, nb = B[k]
        if sa != sb or na != nb:
            drift.append((k, sa, na, sb, nb))

    print(f"A: {a_root}  {len(A)} tests")
    print(f"B: {b_root}  {len(B)} tests")
    print(f"only in A: {len(only_a)}   only in B: {len(only_b)}")
    print(f"status/assertion drift: {len(drift)}")
    for k, sa, na, sb, nb in drift:
        print(f"  DRIFT {k}: {sa}/{na} vs {sb}/{nb}")
    for k in only_a[:40]:
        print(f"  ONLY-A {k}: {A[k]}")
    for k in only_b[:40]:
        print(f"  ONLY-B {k}: {B[k]}")
    if verbose:
        for k in only_a[40:]:
            print(f"  ONLY-A {k}: {A[k]}")
        for k in only_b[40:]:
            print(f"  ONLY-B {k}: {B[k]}")
    return 1 if (drift or only_a or only_b) else 0


if __name__ == "__main__":
    sys.exit(main())
