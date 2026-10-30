#!/usr/bin/env python3
"""Generate PDFs for javadoc class pages via weasyprint.

Usage:
    python3 gen_pdfs.py                    # generate PDFs for all mirrored classes
    python3 gen_pdfs.py --limit N          # only first N classes per version
    python3 gen_pdfs.py --class java.lang.Integer   # only a specific class

The HTML mirrors live under spec-docs/{java8-api,java27-api}/ and are
gitignored. PDFs are written to spec-docs/pdf/{java8,java27}/<fqcn>.pdf
and are committed to the repo.
"""
import argparse
import os
import subprocess
import sys

WEASY = ("/data/yansendao/python/binary/hpython/build/pgo-default-with-jit/"
         "install/bin/weasyprint")

ROOT = os.path.dirname(os.path.abspath(__file__))

# (version, api-root-rel, package-to-dir mapping)
MIRRORS = {
    "java8": {
        "api": os.path.join(ROOT, "java8-api", "docs.oracle.com",
                            "javase", "8", "docs", "api"),
        # package pkg -> relative dir of its class pages
        "pkgdir": lambda pkg: pkg.replace(".", "/"),
    },
    "java27": {
        "api": os.path.join(ROOT, "java27-api", "docs.oracle.com",
                            "en", "java", "javase", "27", "docs", "api"),
        # Java 27 javadoc puts modules before the package path; the common
        # case is that non-java.* classes live under their module dir.
        # The class-list below resolves explicitly, so this is a fallback.
        "pkgdir": lambda pkg: pkg.replace(".", "/"),
    },
}


def html_path(version, fqcn):
    """Return the mirror HTML path for a fully-qualified class name."""
    cfg = MIRRORS[version]
    pkg, _, simple = fqcn.rpartition(".")
    if version == "java8":
        rel = os.path.join(cfg["pkgdir"](pkg), simple + ".html")
    else:
        # Java 27: find the module-qualified path by scanning for the file.
        api = cfg["api"]
        rel = os.path.join(pkg.replace(".", "/"), simple + ".html")
        # try common module roots
        for mod in ("java.base", "java.sql", "java.desktop", "java.xml",
                    "java.logging", "java.naming", "java.management",
                    "java.compiler", "java.instrument", "java.prefs",
                    "java.rmi", "java.net.http", "java.scripting",
                    "java.security.jgss", "java.smartcardio",
                    "java.transaction.xa", "java.datatransfer",
                    "java.se"):
            cand = os.path.join(api, mod, rel)
            if os.path.exists(cand):
                return cand
        # last resort: unqualified search
        for root, _dirs, files in os.walk(api):
            if simple + ".html" in files and pkg.replace(".", "/") in root:
                return os.path.join(root, simple + ".html")
        raise FileNotFoundError(f"{version} {fqcn}")
    return os.path.join(cfg["api"], rel)


def all_classes(version):
    """Yield fqcn for every class HTML in the mirror."""
    cfg = MIRRORS[version]
    api = cfg["api"]
    for root, _dirs, files in os.walk(api):
        for f in files:
            if not f.endswith(".html"):
                continue
            if f in ("index.html", "overview-tree.html", "help-doc.html",
                     "allclasses-noframe.html", "allclasses-index.html",
                     "allpackages-index.html", "allclasses.html",
                     "constant-values.html", "deprecated-list.html",
                     "serialized-form.html", "index-all.html",
                     "package-summary.html", "package-tree.html",
                     "package-use.html", "module-summary.html",
                     "overview-summary.html", "system-properties.html",
                     "tree.html"):
                continue
            # root-relative package path -> fqcn
            rel = os.path.relpath(root, api)
            pkg = rel.replace(os.sep, ".")
            if version == "java27" and not pkg.startswith("java"):
                # strip module prefix: java.base/java/lang -> java.lang
                parts = pkg.split(".")
                for i, p in enumerate(parts):
                    if p == "java":
                        pkg = ".".join(parts[i:])
                        break
            fqcn = pkg + "." + f[:-5] if pkg else f[:-5]
            yield fqcn


def to_pdf(version, fqcn, out):
    src = html_path(version, fqcn)
    r = subprocess.run([WEASY, src, out],
                       capture_output=True, text=True)
    if r.returncode != 0 or not os.path.exists(out):
        # weasyprint logs CSS/image errors to stderr but still succeeds;
        # only treat a missing PDF as failure
        return False
    return True


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--limit", type=int, default=0)
    ap.add_argument("--class", dest="cls", default=None)
    args = ap.parse_args()

    if args.cls:
        jobs = [(v, args.cls) for v in MIRRORS]
    else:
        jobs = []
        for v in MIRRORS:
            for fqcn in all_classes(v):
                jobs.append((v, fqcn))
                if args.limit and len([j for j in jobs if j[0] == v]) >= args.limit:
                    break

    out_root = os.path.join(ROOT, "pdf")
    done = 0
    fail = 0
    for version, fqcn in jobs:
        out_dir = os.path.join(out_root, version)
        os.makedirs(out_dir, exist_ok=True)
        out = os.path.join(out_dir, fqcn + ".pdf")
        if os.path.exists(out):
            continue
        try:
            if to_pdf(version, fqcn, out):
                done += 1
            else:
                fail += 1
                print(f"FAIL {version} {fqcn}", file=sys.stderr)
        except FileNotFoundError as e:
            fail += 1
            print(f"SKIP {e}", file=sys.stderr)
    print(f"generated={done} failed={fail}")


if __name__ == "__main__":
    main()
