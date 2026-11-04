#!/usr/bin/env python3
"""Analyze which JDK-27 API methods are NOT covered by the test suite.

Two inputs, both produced by CovTools.java (ASM 9.11.1):
  methods: /tmp/jcov-tool/jdk27.methods.txt   (fqcn<TAB>name<TAB>desc<TAB>access)
  calls:   /tmp/jcov-tool/calls.txt           (srcFqcn<TAB>INVOKE<TAB>owner<TAB>name<TAB>desc
                                               or srcFqcn<TAB>LDC<TAB>string)
Plus the authoritative type universe tools/out/jdk27.types.txt and the internal
marker tools/out/jdk27.internal.txt (internal types -> module) to split exported vs internal.

Coverage definition for a method (owner, name, desc):
  - DIRECT:  any test's bytecode has invoke* with that owner+name+desc; or
  - REFLECTIVE: the method NAME appears as a string literal in its own class's
    Test.java (covers white-box getMethod/getDeclaredMethod/getConstructor/...).

Outputs under /tmp/jcov-tool/coverage-out/:
  summary.txt, uncovered_methods.txt, uncovered_classes.txt, per_class.tsv
"""
import os
import re
import sys
import collections

REPO = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
TYPES = os.path.join(REPO, "tools", "out", "jdk27.types.txt")
INTERNAL = os.path.join(REPO, "tools", "out", "jdk27.internal.txt")
METHODS = "/tmp/jcov-tool/jdk27.methods.txt"
CALLS = "/tmp/jcov-tool/calls.txt"
APIROOT = os.path.join(REPO, "JDK-27", "API")
OUT = "/tmp/jcov-tool/coverage-out"


def load_types():
    universe = set()
    with open(TYPES) as f:
        for line in f:
            line = line.strip()
            if line:
                universe.add(line)
    internal = set()
    with open(INTERNAL) as f:
        for line in f:
            line = line.rstrip("\n")
            if not line:
                continue
            parts = line.split("\t")
            internal.add(parts[0])
    return universe, internal


def load_methods():
    """fqcn -> list[(name, desc, access)]"""
    m = collections.defaultdict(list)
    with open(METHODS) as f:
        for line in f:
            line = line.rstrip("\n")
            if not line:
                continue
            parts = line.split("\t")
            if len(parts) < 4:
                continue
            fqcn, name, desc, acc = parts[0], parts[1], parts[2], parts[3]
            m[fqcn].append((name, desc, int(acc)))
    return m


def load_calls():
    """returns (direct: owner -> set[(name,desc)], reflective_names_by_fqcn: fqcn -> set[name])"""
    direct = collections.defaultdict(set)
    reflective = collections.defaultdict(set)
    if not os.path.exists(CALLS):
        return direct, reflective
    with open(CALLS) as f:
        for line in f:
            line = line.rstrip("\n")
            if not line:
                continue
            parts = line.split("\t")
            if len(parts) < 2:
                continue
            src = parts[0]
            kind = parts[1]
            if kind == "INVOKE" and len(parts) >= 5:
                owner = parts[2].replace("/", ".")
                name = parts[3]
                desc = parts[4]
                direct[owner].add((name, desc))
            elif kind == "LDC" and len(parts) >= 3:
                s = parts[2]
                # method-name-looking literals (identifiers) are captured reflectively
                if re.fullmatch(r"[A-Za-z_$][A-Za-z0-9_$]*", s):
                    reflective[src].add(s)
    return direct, reflective


def main():
    universe, internal = load_types()
    methods = load_methods()
    direct, reflective = load_calls()

    os.makedirs(OUT, exist_ok=True)

    exported = universe - internal
    # only analyze classes that exist in the method dump (i.e. real classes with methods)
    target = sorted(exported & set(methods.keys()))

    total_methods = 0
    covered_methods = 0
    uncovered_methods = 0
    uncovered_lines = []
    per_class = []
    uncovered_classes = []
    bh_total = bh_covered = bh_uncovered = 0
    bh_uncovered_lines = []
    bh_classes = 0

    for fqcn in target:
        mlist = methods[fqcn]
        # methods (name,desc) directly invoked on this owner (any test)
        dset = direct.get(fqcn, set())
        # reflective names found in this class's own test source
        rnames = reflective.get(fqcn, set())
        # also gather reflective names from the Test.java source (belt-and-braces)
        rnames = set(rnames)
        rnames |= reflective_names_from_source(fqcn)

        bb = has_blackbox(fqcn)
        uncovered = []
        for name, desc, acc in mlist:
            total_methods += 1
            if (name, desc) in dset or name in rnames:
                covered_methods += 1
                if bb:
                    bh_total += 1
                    bh_covered += 1
            else:
                uncovered_methods += 1
                uncovered.append((name, desc))
                if bb:
                    bh_total += 1
                    bh_uncovered += 1
                    bh_uncovered_lines.append(f"{fqcn}\t{name}{desc}")
        per_class.append((fqcn, len(mlist), len(mlist) - len(uncovered), len(uncovered), 1 if bb else 0))
        for name, desc in uncovered:
            uncovered_lines.append(f"{fqcn}\t{name}{desc}")
        if len(uncovered) == len(mlist) and len(mlist) > 0:
            uncovered_classes.append(fqcn)
        if bb:
            bh_classes += 1

    with open(os.path.join(OUT, "summary.txt"), "w") as f:
        f.write("JDK-27 API method coverage analysis (static: bytecode invoke + reflective name)\n")
        f.write("=" * 70 + "\n")
        f.write(f"exported API classes analyzed : {len(target)}\n")
        f.write(f"  behavioral (testBlackBox)   : {bh_classes}\n")
        f.write(f"  structural-only             : {len(target) - bh_classes}\n")
        f.write(f"total non-private methods      : {total_methods}\n")
        f.write(f"covered methods (direct+refl)  : {covered_methods} ({100.0*covered_methods/max(1,total_methods):.2f}%)\n")
        f.write(f"uncovered methods              : {uncovered_methods} ({100.0*uncovered_methods/max(1,total_methods):.2f}%)\n")
        f.write(f"classes with 0 methods covered : {len(uncovered_classes)}\n")
        f.write("\n--- behavioral classes only ---\n")
        f.write(f"behavioral methods             : {bh_total}\n")
        f.write(f"  covered                      : {bh_covered} ({100.0*bh_covered/max(1,bh_total):.2f}%)\n")
        f.write(f"  uncovered                    : {bh_uncovered} ({100.0*bh_uncovered/max(1,bh_total):.2f}%)\n")

    with open(os.path.join(OUT, "uncovered_methods.txt"), "w") as f:
        f.write("\n".join(sorted(uncovered_lines)) + "\n")

    with open(os.path.join(OUT, "behavioral_uncovered_methods.txt"), "w") as f:
        f.write("\n".join(sorted(bh_uncovered_lines)) + "\n")

    with open(os.path.join(OUT, "uncovered_classes.txt"), "w") as f:
        f.write("\n".join(sorted(uncovered_classes)) + "\n")

    per_class.sort(key=lambda x: -x[3])
    with open(os.path.join(OUT, "per_class.tsv"), "w") as f:
        f.write("fqcn\ttotal\tcovered\tuncovered\tbehavioral\n")
        for fqcn, t, c, u, bb in per_class:
            f.write(f"{fqcn}\t{t}\t{c}\t{u}\t{bb}\n")

    print(f"analyzed {len(target)} exported classes, {total_methods} methods")
    print(f"covered {covered_methods} ({100.0*covered_methods/max(1,total_methods):.2f}%), "
          f"uncovered {uncovered_methods} ({100.0*uncovered_methods/max(1,total_methods):.2f}%)")
    print(f"classes with 0 covered methods: {len(uncovered_classes)}")
    print(f"behavioral classes: {bh_classes}, uncovered {bh_uncovered}/{bh_total} "
          f"({100.0*bh_uncovered/max(1,bh_total):.2f}%)")
    print(f"outputs in {OUT}/")


def dir_for(fqcn):
    """Map a dotted fqcn to its nested test directory under APIROOT."""
    return os.path.join(APIROOT, fqcn.replace(".", os.sep))


def has_blackbox(fqcn):
    """True if the class's Test.java contains a testBlackBox method (behavioral)."""
    d = dir_for(fqcn)
    if not os.path.isdir(d):
        return False
    for fn in os.listdir(d):
        if not fn.endswith("Test.java"):
            continue
        p = os.path.join(d, fn)
        try:
            with open(p, encoding="utf-8", errors="replace") as f:
                if "testBlackBox" in f.read():
                    return True
        except OSError:
            pass
    return False


def reflective_names_from_source(fqcn):
    """Extract identifier-looking string literals from a class's Test.java (white-box reflection)."""
    names = set()
    d = dir_for(fqcn)
    if not os.path.isdir(d):
        return names
    for fn in os.listdir(d):
        if not fn.endswith("Test.java"):
            continue
        p = os.path.join(d, fn)
        try:
            with open(p, encoding="utf-8", errors="replace") as f:
                src = f.read()
        except OSError:
            continue
        # string literals
        for s in re.findall(r'"([A-Za-z_$][A-Za-z0-9_$]*)"', src):
            names.add(s)
        # method-name references in getMethod/getDeclaredMethod/getConstructor/...
        for s in re.findall(r'get(?:Declared)?(?:Method|Constructor|Field)\s*\(\s*"([A-Za-z_$][A-Za-z0-9_$]*)"', src):
            names.add(s)
        # Class.forName / direct .methodName( calls
        for s in re.findall(r'\.([A-Za-z_$][A-Za-z0-9_$]*)\s*\(', src):
            names.add(s)
    return names


if __name__ == "__main__":
    main()
