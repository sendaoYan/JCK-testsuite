#!/usr/bin/env python3
"""Analyze which JDK-27 API methods are NOT covered by the test suite.

Inputs, all produced by tools/CovTools.java (ASM 9.11.1):
  methods: /tmp/jcov-tool/jdk27.methods.txt            (fqcn<TAB>name<TAB>desc<TAB>access)
  hierarchy: /tmp/jcov-tool/jdk27.methods.txt.hierarchy (fqcn<TAB>super<TAB>itf1,itf2<TAB>isInterface)
  preview:  /tmp/jcov-tool/jdk27.methods.txt.preview    (fqcn of preview API classes)
  calls:    /tmp/jcov-tool/calls.txt                    (src<TAB>KIND<TAB>...)
Plus the authoritative type universe tools/out/jdk27.types.txt and the internal
marker tools/out/jdk27.internal.txt (internal types -> module) to split exported
vs internal.

Coverage of a method (owner, name, desc) is any of:
  - DIRECT:     a test's bytecode has invoke* with that owner+name+desc;
  - REFLECTIVE: the method NAME appears as a string literal in its own class's
                Test.java (covers white-box getMethod/getDeclaredMethod/...);
  - CTOR:       a reflective constructor instantiation recovered either from
                bytecode (CovTools emits CTOR records via ASM Analyzer) or from
                source (Class.forName("X").getConstructor(..).newInstance(..)
                and Constructor-var .newInstance(..));
  - IFACE-DISPATCH: an invokeinterface on an interface also covers the concrete
                implementers' same name+desc methods, and a covered concrete
                method also covers the interface's same name+desc declaration.

Outputs under /tmp/jcov-tool/coverage-out/:
  summary.txt, uncovered_methods.txt, uncovered_classes.txt, per_class.tsv,
  behavioral_uncovered_methods.txt, preview_uncovered_methods.txt
"""
import os
import re
import sys
import collections

REPO = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
TYPES = os.path.join(REPO, "tools", "out", "jdk27.types.txt")
INTERNAL = os.path.join(REPO, "tools", "out", "jdk27.internal.txt")
METHODS = "/tmp/jcov-tool/jdk27.methods.txt"
HIERARCHY = METHODS + ".hierarchy"
PREVIEW = METHODS + ".preview"
CALLS = "/tmp/jcov-tool/calls.txt"
APIROOT = os.path.join(REPO, "JDK-27", "API")
OUT = "/tmp/jcov-tool/coverage-out"

PRIM = {'int': 'I', 'long': 'J', 'boolean': 'Z', 'char': 'C', 'byte': 'B',
        'short': 'S', 'float': 'F', 'double': 'D', 'void': 'V'}
JAVA_LANG = {'Object', 'String', 'Integer', 'Long', 'Boolean', 'Character',
             'Byte', 'Short', 'Float', 'Double', 'Void', 'Class', 'System',
             'Thread', 'Runnable', 'Exception', 'Throwable', 'Error',
             'RuntimeException', 'IllegalArgumentException', 'IllegalStateException',
             'NullPointerException', 'UnsupportedOperationException',
             'ClassCastException', 'IndexOutOfBoundsException',
             'ArithmeticException', 'NumberFormatException', 'Override',
             'SuppressWarnings', 'Deprecated', 'FunctionalInterface', 'SafeVarargs',
             'Enum', 'Iterable', 'AutoCloseable', 'CharSequence', 'Comparable',
             'Math', 'StrictMath', 'Process', 'ProcessBuilder', 'Runtime',
             'StringBuilder', 'StringBuffer', 'ThreadLocal', 'ThreadGroup',
             'StackTraceElement', 'Record'}


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
            internal.add(line.split("\t")[0])
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
            m[parts[0]].append((parts[1], parts[2], int(parts[3])))
    return m


def load_hierarchy():
    """Return (parents, transitive supertypes, is_interface)."""
    parents = collections.defaultdict(set)
    is_interface = {}
    with open(HIERARCHY) as f:
        for line in f:
            line = line.rstrip("\n")
            if not line:
                continue
            parts = line.split("\t")
            fqcn = parts[0]
            sup = parts[1] if len(parts) > 1 else ""
            itfs = parts[2] if len(parts) > 2 else ""
            flag = parts[3] if len(parts) > 3 else "0"
            is_interface[fqcn] = (flag == "1")
            for it in itfs.split(","):
                if it:
                    parents[fqcn].add(it)
            if sup and sup != "java.lang.Object":
                parents[fqcn].add(sup)
    supertypes = {}
    for t in parents:
        seen = set()
        stack = list(parents[t])
        while stack:
            s = stack.pop()
            if s in seen:
                continue
            seen.add(s)
            stack.extend(parents.get(s, ()))
        supertypes[t] = seen
    return parents, supertypes, is_interface


def load_calls():
    """direct: owner -> set[(name,desc)]; iface: owner -> set[(name,desc)];
    ctor: owner -> set[desc]; reflective: src-fqcn -> set[name]."""
    direct = collections.defaultdict(set)
    iface = collections.defaultdict(set)
    ctor = collections.defaultdict(set)
    reflective = collections.defaultdict(set)
    if not os.path.exists(CALLS):
        return direct, iface, ctor, reflective
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
                direct[owner].add((parts[3], parts[4]))
            elif kind == "INVOKEIF" and len(parts) >= 5:
                owner = parts[2].replace("/", ".")
                iface[owner].add((parts[3], parts[4]))
            elif kind == "CTOR" and len(parts) >= 4:
                ctor[parts[2]].add(parts[3])
            elif kind == "LDC" and len(parts) >= 3:
                s = parts[2]
                if re.fullmatch(r"[A-Za-z_$][A-Za-z0-9_$]*", s):
                    reflective[src].add(s)
    return direct, iface, ctor, reflective


def build_propagation_indexes(methods, supertypes, is_interface):
    """implementers[I] = classes C (in methods) with I in supertypes[C];
    subinterfaces[I] = subinterfaces SubI (in methods) with I in supertypes[SubI]."""
    implementers = collections.defaultdict(set)
    subinterfaces = collections.defaultdict(set)
    for t in methods.keys():
        if is_interface.get(t):
            kind_map = subinterfaces
        else:
            kind_map = implementers
        for s in supertypes.get(t, ()):
            if is_interface.get(s):
                kind_map[s].add(t)
    return implementers, subinterfaces


def propagate(covered, method_set, supertypes, is_interface, implementers, subinterfaces):
    """Bidirectional interface<->implementer propagation over same name+desc.
    - a covered concrete method covers the interface's same name+desc declaration;
    - a covered interface method (interface dispatch) covers its implementers' and
      subinterfaces' same name+desc method."""
    work = []
    for t, ss in covered.items():
        for nd in ss:
            work.append((t, nd[0], nd[1]))
    seen = set()
    while work:
        t, n, d = work.pop()
        key = (t, n, d)
        if key in seen:
            continue
        seen.add(key)
        # up: any covered method also covers its (transitive) super-interfaces
        for s in supertypes.get(t, ()):
            if is_interface.get(s) and (n, d) in method_set.get(s, ()):
                if (n, d) not in covered[s]:
                    covered[s].add((n, d))
                    work.append((s, n, d))
        # down: an interface method covers implementers + subinterfaces
        if is_interface.get(t):
            for c in implementers.get(t, set()) | subinterfaces.get(t, set()):
                if (n, d) in method_set.get(c, ()) and (n, d) not in covered[c]:
                    covered[c].add((n, d))
                    work.append((c, n, d))


# ---------------------------------------------------------------------------
# source-based reflective constructor recovery (supplements bytecode CTOR records)
# ---------------------------------------------------------------------------
def dir_for(fqcn):
    return os.path.join(APIROOT, fqcn.replace(".", os.sep))


def parse_imports(src):
    imports = {}
    for m in re.finditer(r'^\s*import\s+(?:static\s+)?([\w.]+)\s*;', src, re.M):
        full = m.group(1)
        imports[full.split('.')[-1]] = full
    return imports


def resolve_class_name(name, imports, fqcn):
    if not name:
        return None
    if name.startswith('['):
        return None
    if '.' in name:
        return name
    if name in imports:
        return imports[name]
    if name in JAVA_LANG:
        return 'java.lang.' + name
    if name == fqcn.split('.')[-1]:
        return fqcn
    return None


def java_type_to_desc(tok):
    t = tok.strip()
    if not t.endswith('.class'):
        return None
    t = t[:-len('.class')].strip()
    dims = 0
    while t.endswith('[]'):
        dims += 1
        t = t[:-2].strip()
    if t in PRIM:
        base = PRIM[t]
    else:
        base = 'L' + t.replace('.', '/') + ';'
    return '[' * dims + base


def ctor_desc_from_args(argstr):
    if argstr.strip() == '':
        return '()V'
    toks = []
    depth = 0
    cur = ''
    for ch in argstr:
        if ch == '<':
            depth += 1
        elif ch == '>':
            depth -= 1
        if ch == ',' and depth == 0:
            toks.append(cur)
            cur = ''
        else:
            cur += ch
    toks.append(cur)
    parts = []
    for tok in toks:
        d = java_type_to_desc(tok)
        if d is None:
            return None
        parts.append(d)
    return '(' + ''.join(parts) + ')V'


def reflective_ctors_from_source(fqcn):
    """Return set of (target_fqcn, desc) recovered from source."""
    result = set()
    d = dir_for(fqcn)
    if not os.path.isdir(d):
        return result
    for fn in os.listdir(d):
        if not fn.endswith("Test.java"):
            continue
        p = os.path.join(d, fn)
        try:
            with open(p, encoding="utf-8", errors="replace") as f:
                src = f.read()
        except OSError:
            continue
        imports = parse_imports(src)
        classvars = {}
        for m in re.finditer(r'Class(?:<[^>]*>)?\s+(\w+)\s*=\s*Class\.forName\s*\(\s*"([^"]+)"\s*\)\s*;', src):
            classvars[m.group(1)] = m.group(2)
        for m in re.finditer(r'Class(?:<[^>]*>)?\s+(\w+)\s*=\s*([A-Za-z_$][\w.$]*?)\.class\s*;', src):
            t = resolve_class_name(m.group(2), imports, fqcn)
            if t:
                classvars[m.group(1)] = t

        # Constructor<...> NAME = <receiver>.get(Declared)Constructor(ARGS);
        ctorvars = {}
        ctor_pat = re.compile(
            r'Constructor\s*<[^>]*>\s+(\w+)\s*=\s*'
            r'(?:Class\.forName\s*\(\s*"([^"]+)"\s*\)|([A-Za-z_$][\w.$]*?)\.class|(\w+))'
            r'\s*\.\s*(?:getDeclaredConstructor|getConstructor)\s*\(([^)]*)\)\s*;'
        )
        for m in ctor_pat.finditer(src):
            name = m.group(1)
            target = None
            if m.group(2):
                target = m.group(2)
            elif m.group(3):
                target = resolve_class_name(m.group(3), imports, fqcn)
            elif m.group(4):
                target = classvars.get(m.group(4))
            if target:
                desc = ctor_desc_from_args(m.group(5))
                if desc:
                    ctorvars[name] = (target, desc)

        for name, (target, desc) in ctorvars.items():
            if re.search(r'\b' + re.escape(name) + r'\s*\.\s*newInstance\s*\(', src):
                result.add((target, desc))

        # inline: Class.forName("X").get(Declared)Constructor(ARGS).newInstance
        for m in re.finditer(r'Class\.forName\s*\(\s*"([^"]+)"\s*\)\s*\.\s*(?:getDeclaredConstructor|getConstructor)\s*\(([^)]*)\)\s*\.\s*newInstance', src):
            desc = ctor_desc_from_args(m.group(2))
            if desc:
                result.add((m.group(1), desc))
        # inline: X.class.get(Declared)Constructor(ARGS).newInstance
        for m in re.finditer(r'([A-Za-z_$][\w.$]*?)\.class\s*\.\s*(?:getDeclaredConstructor|getConstructor)\s*\(([^)]*)\)\s*\.\s*newInstance', src):
            t = resolve_class_name(m.group(1), imports, fqcn)
            desc = ctor_desc_from_args(m.group(2))
            if t and desc:
                result.add((t, desc))
    return result


# ---------------------------------------------------------------------------
# source-based reflective name detection (white-box reflection) — unchanged
# ---------------------------------------------------------------------------
def reflective_names_from_source(fqcn):
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
        for s in re.findall(r'"([A-Za-z_$][A-Za-z0-9_$]*)"', src):
            names.add(s)
        for s in re.findall(r'get(?:Declared)?(?:Method|Constructor|Field)\s*\(\s*"([A-Za-z_$][A-Za-z0-9_$]*)"', src):
            names.add(s)
        for s in re.findall(r'\.([A-Za-z_$][A-Za-z0-9_$]*)\s*\(', src):
            names.add(s)
    return names


def has_blackbox(fqcn):
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


def main():
    universe, internal = load_types()
    methods = load_methods()
    parents, supertypes, is_interface = load_hierarchy()
    direct, iface, ctor, reflective = load_calls()

    # base coverage: direct invokes + bytecode-recovered reflective ctors (gap a)
    covered = collections.defaultdict(set)
    for owner, ss in direct.items():
        covered[owner] |= ss
    for owner, descs in ctor.items():
        for desc in descs:
            covered[owner].add(("<init>", desc))

    os.makedirs(OUT, exist_ok=True)

    exported = universe - internal
    target = sorted(exported & set(methods.keys()))

    preview_classes = set()
    if os.path.exists(PREVIEW):
        with open(PREVIEW) as f:
            for line in f:
                line = line.strip()
                if line:
                    preview_classes.add(line)

    # gap (a) supplement: source-recovered reflective ctors
    for fqcn in target:
        for t, desc in reflective_ctors_from_source(fqcn):
            covered[t].add(("<init>", desc))

    # gap (b): interface <-> implementer propagation
    method_set = {fqcn: {(n, d) for n, d, _ in ml} for fqcn, ml in methods.items()}
    implementers, subinterfaces = build_propagation_indexes(methods, supertypes, is_interface)
    propagate(covered, method_set, supertypes, is_interface, implementers, subinterfaces)

    total_methods = 0
    covered_methods = 0
    uncovered_methods = 0
    uncovered_lines = []
    per_class = []
    uncovered_classes = []
    preview_uncovered_lines = []
    bh_total = bh_covered = bh_uncovered = 0
    bh_uncovered_lines = []
    bh_classes = 0
    preview_analyzed = 0

    for fqcn in target:
        mlist = methods[fqcn]
        dset = covered.get(fqcn, set())
        rnames = reflective.get(fqcn, set()) | reflective_names_from_source(fqcn)
        bb = has_blackbox(fqcn)
        is_preview = fqcn in preview_classes
        if is_preview:
            preview_analyzed += 1
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
                if is_preview:
                    preview_uncovered_lines.append(f"{fqcn}\t{name}{desc}\tpreview")
                if bb:
                    bh_total += 1
                    bh_uncovered += 1
                    bh_uncovered_lines.append(f"{fqcn}\t{name}{desc}")
        per_class.append((fqcn, len(mlist), len(mlist) - len(uncovered), len(uncovered), 1 if bb else 0, 1 if is_preview else 0))
        for name, desc in uncovered:
            uncovered_lines.append(f"{fqcn}\t{name}{desc}")
        if len(uncovered) == len(mlist) and len(mlist) > 0:
            uncovered_classes.append(fqcn)
        if bb:
            bh_classes += 1

    with open(os.path.join(OUT, "summary.txt"), "w") as f:
        f.write("JDK-27 API method coverage analysis (static: invoke + reflective + ctor + iface-dispatch)\n")
        f.write("=" * 70 + "\n")
        f.write(f"exported API classes analyzed : {len(target)}\n")
        f.write(f"  behavioral (testBlackBox)   : {bh_classes}\n")
        f.write(f"  structural-only             : {len(target) - bh_classes}\n")
        f.write(f"  preview API classes         : {preview_analyzed}\n")
        f.write(f"total non-private methods      : {total_methods}\n")
        f.write(f"covered methods                : {covered_methods} ({100.0*covered_methods/max(1,total_methods):.2f}%)\n")
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

    with open(os.path.join(OUT, "preview_uncovered_methods.txt"), "w") as f:
        f.write("\n".join(sorted(preview_uncovered_lines)) + "\n")

    with open(os.path.join(OUT, "uncovered_classes.txt"), "w") as f:
        f.write("\n".join(sorted(uncovered_classes)) + "\n")

    per_class.sort(key=lambda x: -x[3])
    with open(os.path.join(OUT, "per_class.tsv"), "w") as f:
        f.write("fqcn\ttotal\tcovered\tuncovered\tbehavioral\tpreview\n")
        for fqcn, t, c, u, bb, pv in per_class:
            f.write(f"{fqcn}\t{t}\t{c}\t{u}\t{bb}\t{pv}\n")

    print(f"analyzed {len(target)} exported classes, {total_methods} methods "
          f"({preview_analyzed} preview)")
    print(f"covered {covered_methods} ({100.0*covered_methods/max(1,total_methods):.2f}%), "
          f"uncovered {uncovered_methods} ({100.0*uncovered_methods/max(1,total_methods):.2f}%)")
    print(f"classes with 0 covered methods: {len(uncovered_classes)}")
    print(f"behavioral classes: {bh_classes}, uncovered {bh_uncovered}/{bh_total} "
          f"({100.0*bh_uncovered/max(1,bh_total):.2f}%)")
    print(f"outputs in {OUT}/")


if __name__ == "__main__":
    main()
