#!/usr/bin/env python3
"""Enumerate every non-private (public/protected) type in a JDK.

This is the *authoritative* universe for API-conformance coverage: it reads the
compiled class files of the actual JDK image (not the javadoc, which covers only
the documented subset) and extracts each type's access flags, so it includes
jdk.*, com.sun.*, sun.* and org.* implementation types too.

For a module image (JDK 9+) it also maps each type to its owning module and
says whether the package is *unqualified-exported* (reachable from a test in the
unnamed module without extra flags) or *internal* (needs --add-exports /
--add-opens, expressed in jtreg as `@modules <mod>/<pkg> +open`).

For a pre-module JDK 8 layout it reads rt.jar and the standard jars; every
public/protected type there is reachable with no flags.

Outputs (per JDK, written next to this script under tools/out/):
  <jdk>.types.txt     one fqcn per line, non-private types, sorted
  <jdk>.gap.txt       fqcn without a matching JDK-{8,27}/API/<pkg>/.../<Simple>/ dir
  <jdk>.internal.txt  (module image only) internal types -> module/pkg

Usage:
  python3 tools/enumerate_api.py jdk27
  python3 tools/enumerate_api.py jdk8
  python3 tools/enumerate_api.py all
"""
import argparse
import os
import re
import struct
import subprocess
import sys
import zipfile

JDK27 = "/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1"
JDK8 = "/data/yansendao/JDK/binary/temurin/jdk8u372-b07"
REPO = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                     ".."))
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "out")

ACC_PUBLIC = 0x0001
ACC_PRIVATE = 0x0002
ACC_PROTECTED = 0x0004
ACC_SYNTHETIC = 0x1000

ANON = re.compile(r"\$\d+")
PKG_INFO = re.compile(r"(^|/)package-info$|(^|/)module-info$")


def access_flags(data):
    """Return the class-file access_flags, or None if not parseable/class."""
    if len(data) < 10 or data[:4] != b"\xca\xfe\xba\xbe":
        return None
    off = 8
    cp_count = struct.unpack(">H", data[off:off + 2])[0]
    off += 2
    i = 1
    while i < cp_count:
        if off >= len(data):
            return None
        tag = data[off]
        off += 1
        if tag == 1:  # CONSTANT_Utf8
            ln = struct.unpack(">H", data[off:off + 2])[0]
            off += 2 + ln
        elif tag in (3, 4):  # Integer / Float
            off += 4
        elif tag in (5, 6):  # Long / Double (2 slots)
            off += 8
            i += 1
        elif tag in (7, 8, 16, 19, 20):  # Class/String/MethodType/Module/Pkg
            off += 2
        elif tag in (9, 10, 11, 12, 17, 18):  # refs / NameAndType / Indy/Dyn
            off += 4
        elif tag == 15:  # MethodHandle
            off += 3
        else:
            return None
        i += 1
    if off + 2 > len(data):
        return None
    return struct.unpack(">H", data[off:off + 2])[0]


def is_api_type(entry):
    """entry: 'java/util/Map$Entry.class' -> True if a non-private API type."""
    if not entry.endswith(".class") or PKG_INFO.search(entry):
        return False
    name = entry[:-6]  # drop .class
    if ANON.search(name):  # anonymous / local classes ($1, $2, ...)
        return False
    return True


def entry_to_fqcn(entry):
    return entry[:-6].replace("/", ".").replace("$", ".")


def class_to_fqcn(entry, data):
    """entry + bytes -> fqcn, or None if not a non-private API type."""
    if not is_api_type(entry):
        return None
    flags = access_flags(data)
    if flags is None:
        return None
    if flags & ACC_SYNTHETIC:
        return None
    if not (flags & (ACC_PUBLIC | ACC_PROTECTED)):
        return None
    return entry_to_fqcn(entry)


def coverage(jdk):
    base = os.path.join(REPO, jdk, "API")
    out = set()
    if not os.path.isdir(base):
        return out
    for root, _dirs, files in os.walk(base):
        for f in files:
            if f.endswith("Test.java"):
                out.add(os.path.relpath(root, base).replace(os.sep, "."))
    return out


# ---------------------------------------------------------------------------
# JDK 9+ module image
# ---------------------------------------------------------------------------

def module_packages(jdk_home):
    """Return (exported:set[pkg], internal:{pkg: module})."""
    java = os.path.join(jdk_home, "bin", "java")
    mods = subprocess.run([java, "--list-modules"], capture_output=True,
                          text=True, check=True).stdout.split()
    exported, internal = set(), {}
    for m in mods:
        m = m.split("@")[0]
        desc = subprocess.run([java, "--describe-module", m],
                              capture_output=True, text=True,
                              check=True).stdout.splitlines()
        for line in desc:
            line = line.strip()
            if line.startswith("exports "):
                # "exports pkg" or "exports pkg to a,b,c"
                rest = line[len("exports "):].strip()
                pkg = rest.split()[0]
                if " to " not in line:
                    exported.add(pkg)
            elif line.startswith("contains "):
                pkg = line[len("contains "):].strip()
                internal.setdefault(pkg, m)
            elif line.startswith("opens "):
                pass
    return exported, internal


def enumerate_module(jdk_home):
    """Extract the jimage and return (fqcns, internal {fqcn: module})."""
    lib = os.path.join(jdk_home, "lib", "modules")
    jimage = os.path.join(jdk_home, "bin", "jimage")
    tmp = os.path.join("/tmp", "jimage-extract")
    subprocess.run(["rm", "-rf", tmp], check=False)
    subprocess.run([jimage, "extract", "--dir", tmp, lib], check=True)
    exported, internal_pkg = module_packages(jdk_home)
    fqcns, internal = set(), {}
    for root, _dirs, files in os.walk(tmp):
        for f in files:
            if not f.endswith(".class"):
                continue
            # jimage extract nests everything under <module>/; drop that segment
            rel_parts = os.path.relpath(os.path.join(root, f), tmp).split(os.sep)
            if len(rel_parts) < 2:
                continue
            rel = os.sep.join(rel_parts[1:])
            with open(os.path.join(root, f), "rb") as fh:
                data = fh.read()
            fqcn = class_to_fqcn(rel, data)
            if fqcn is None:
                continue
            fqcns.add(fqcn)
            pkg = fqcn[:fqcn.rfind(".")] if "." in fqcn else ""
            # package is everything before the first upper-case segment
            segs = fqcn.split(".")
            k = len(segs)
            for i, s in enumerate(segs):
                if s and s[0].isupper():
                    k = i
                    break
            pkg = ".".join(segs[:k])
            if pkg not in exported:
                internal[fqcn] = internal_pkg.get(pkg, "?")
    return fqcns, internal


# ---------------------------------------------------------------------------
# JDK 8 rt.jar + standard jars
# ---------------------------------------------------------------------------

JDK8_JARS = ["jre/lib/rt.jar", "jre/lib/jce.jar", "jre/lib/jsse.jar",
             "jre/lib/jfr.jar", "jre/lib/charsets.jar"]


def enumerate_jdk8(jdk_home):
    fqcns = set()
    for rel in JDK8_JARS:
        path = os.path.join(jdk_home, rel)
        if not os.path.exists(path):
            continue
        with zipfile.ZipFile(path) as z:
            for name in z.namelist():
                fqcn = class_to_fqcn(name, z.read(name))
                if fqcn:
                    fqcns.add(fqcn)
    return fqcns, {}


def write(name, items):
    path = os.path.join(OUT, name)
    with open(path, "w") as fh:
        for it in sorted(items):
            fh.write(it + "\n")
    return path


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("target", choices=["jdk27", "jdk8", "all"], nargs="?",
                    default="all")
    args = ap.parse_args()
    os.makedirs(OUT, exist_ok=True)

    if args.target in ("jdk27", "all"):
        print("[jdk27] enumerating module image...", file=sys.stderr)
        fqcns, internal = enumerate_module(JDK27)
        cov = coverage("JDK-27")
        gap = fqcns - cov
        write("jdk27.types.txt", fqcns)
        write("jdk27.gap.txt", gap)
        write("jdk27.internal.txt",
              [f"{f}\t{internal[f]}" for f in sorted(internal)])
        print(f"[jdk27] types={len(fqcns)} covered={len(cov)} gap={len(gap)} "
              f"internal={len(internal)}", file=sys.stderr)

    if args.target in ("jdk8", "all"):
        print("[jdk8] enumerating rt.jar...", file=sys.stderr)
        fqcns, _ = enumerate_jdk8(JDK8)
        cov = coverage("JDK-8")
        gap = fqcns - cov
        write("jdk8.types.txt", fqcns)
        write("jdk8.gap.txt", gap)
        print(f"[jdk8] types={len(fqcns)} covered={len(cov)} gap={len(gap)}",
              file=sys.stderr)


if __name__ == "__main__":
    main()
