#!/usr/bin/env python3
"""Build the consolidated per-JDK API reference PDF.

Pipeline
--------
1. Discover the target type universe from the conformance test dirs
   (JDK-{8,27}/API/<fqcn>/<Simple>Test.java).
2. Generate any missing per-class javadoc PDFs in parallel via weasyprint.
3. Merge each version's per-class PDFs into one PDF containing:
   - a title page
   - a rendered Table of Contents (packages -> classes, with page numbers)
   - a hierarchical PDF outline (bookmarks): title -> TOC -> package -> class

Outputs
-------
spec-docs/pdf/java8-api.pdf   (from spec-docs/pdf/java8/*.pdf)
spec-docs/pdf/java27-api.pdf  (from spec-docs/pdf/java27/*.pdf)

Usage
-----
    python3 build_api_pdf.py generate   # only fill missing per-class PDFs
    python3 build_api_pdf.py merge      # only (re)build the merged PDFs
    python3 build_api_pdf.py all        # both
"""
import argparse
import concurrent.futures
import os
import subprocess
import sys

from pypdf import PdfReader, PdfWriter

WEASY = ("/data/yansendao/python/binary/hpython/build/pgo-default-with-jit/"
         "install/bin/weasyprint")

ROOT = os.path.dirname(os.path.abspath(__file__))
PDF_ROOT = os.path.join(ROOT, "pdf")

MIRRORS = {
    "java8": os.path.join(ROOT, "java8-api", "docs.oracle.com",
                          "javase", "8", "docs", "api"),
    "java27": os.path.join(ROOT, "java27-api", "docs.oracle.com",
                           "en", "java", "javase", "27", "docs", "api"),
}

# module-qualified (java27) mirror root -> package = path segments after module
JDK27 = "java27"


def build_index(version):
    """Map fqcn -> absolute HTML path for every class page in the mirror."""
    api = MIRRORS[version]
    idx = {}
    for root, dirs, files in os.walk(api):
        # skip javadoc cross-reference dirs (they mirror class names but are
        # not class definition pages)
        dirs[:] = [d for d in dirs if d not in ("class-use", "package-use")]
        rel = os.path.relpath(root, api)
        if rel == ".":
            rel_parts = []
        else:
            rel_parts = rel.split(os.sep)
        if version == JDK27:
            # api/<module>/<pkg>/<Class>.html -> drop the module segment
            pkg_parts = rel_parts[1:]
        else:
            pkg_parts = rel_parts
        pkg = ".".join(pkg_parts)
        for f in files:
            if not f.endswith(".html"):
                continue
            stem = f[:-5]
            # class pages are capitalized; index/summary pages are lowercase
            if not stem or not stem[0].isupper():
                continue
            fqcn = pkg + "." + stem if pkg else stem
            idx[fqcn] = os.path.join(root, f)
    return idx


def target_fqcns(jdk):
    """The conformance-test universe: one fqcn per test directory."""
    base = os.path.join(ROOT, "..", jdk, "API")
    out = set()
    for root, _dirs, files in os.walk(base):
        for f in files:
            if f.endswith("Test.java"):
                out.add(os.path.relpath(root, base))
    return out


def pdf_path(version, fqcn):
    return os.path.join(PDF_ROOT, version, fqcn + ".pdf")


def gen_one(args):
    version, fqcn, html, out = args
    if os.path.exists(out):
        return (fqcn, "skip")
    r = subprocess.run([WEASY, html, out], capture_output=True, text=True)
    if os.path.exists(out) and os.path.getsize(out) > 0:
        return (fqcn, "ok")
    # weasyprint may still succeed after CSS warnings; a missing/empty PDF is a
    # real failure
    return (fqcn, "fail:" + r.stderr[-200:] if r.stderr else "fail")


def generate(version, jdk, workers):
    idx = build_index(version)
    target = target_fqcns(jdk)
    os.makedirs(os.path.join(PDF_ROOT, version), exist_ok=True)
    jobs = []
    missing_html = []
    for fqcn in sorted(target):
        if fqcn not in idx:
            missing_html.append(fqcn)
            continue
        jobs.append((version, fqcn, idx[fqcn], pdf_path(version, fqcn)))
    print(f"[{version}] target={len(target)} have_html={len(jobs)} "
          f"no_html={len(missing_html)}")
    if missing_html:
        for m in missing_html:
            print(f"  MISSING-HTML {m}", file=sys.stderr)

    ok = fail = 0
    with concurrent.futures.ProcessPoolExecutor(max_workers=workers) as ex:
        for fqcn, status in ex.map(gen_one, jobs):
            if status == "ok":
                ok += 1
            elif status == "skip":
                pass
            else:
                fail += 1
                print(f"  {version} {fqcn}: {status}", file=sys.stderr)
    print(f"[{version}] generated={ok} failed={fail}")


# --------------------------------------------------------------------------
# merge helpers
# --------------------------------------------------------------------------

def type_display(fqcn, pkg):
    """Simple type name for the TOC (nested types keep their dot chain)."""
    return fqcn[len(pkg) + 1:] if pkg else fqcn


CSS = """
@page { size: A4; margin: 2.2cm 2cm; @bottom-center { content: counter(page); } }
body { font-family: 'DejaVu Serif', serif; font-size: 10pt; color: #111; }
h1.title { text-align: center; font-size: 22pt; margin: 4cm 0 0.4em 0; }
p.subtitle { text-align: center; font-size: 12pt; color: #444; margin: 0 0 4cm 0; }
h1.toc { font-size: 16pt; margin-top: 0; }
h2.pkg { font-size: 12pt; margin: 1.4em 0 0.4em 0; border-bottom: 1px solid #888; }
ul { list-style: none; margin: 0 0 0 0.6em; padding: 0; }
li { display: flex; justify-content: space-between; border-bottom: 1px dotted #bbb; padding: 0.1em 0; }
li .pg { color: #333; }
"""


def group_by_package(fqcns):
    """Group fqcns by their true package (segments before the first type)."""
    pkgs = {}
    for fqcn in fqcns:
        segs = fqcn.split(".")
        k = 0
        for i, s in enumerate(segs):
            if s[0].isupper():
                k = i
                break
        pkgs.setdefault(".".join(segs[:k]), []).append(fqcn)
    return pkgs


def title_page_html(title, subtitle):
    return ("<html><head><meta charset='utf-8'><style>" + CSS +
            "</style></head><body>"
            f"<h1 class='title'>{title}</h1>"
            f"<p class='subtitle'>{subtitle}</p>"
            "</body></html>")


def toc_html(pkgs, page_of):
    """pkgs: {package: [fqcn...]} ; page_of: fqcn -> 1-indexed page number."""
    lines = ["<html><head><meta charset='utf-8'><style>" + CSS +
             "</style></head><body>", "<h1 class='toc'>Table of Contents</h1>"]
    for pkg in sorted(pkgs):
        lines.append(f"<h2 class='pkg'>{pkg}</h2>")
        lines.append("<ul>")
        for fqcn in sorted(pkgs[pkg]):
            disp = type_display(fqcn, pkg)
            pg = page_of.get(fqcn, 0)
            lines.append(f"<li><span class='nm'>{disp}</span>"
                         f"<span class='pg'>{pg}</span></li>")
        lines.append("</ul>")
    lines.append("</body></html>")
    return "\n".join(lines)


def render_html(html, out_pdf):
    r = subprocess.run([WEASY, "-", out_pdf], input=html, capture_output=True,
                       text=True)
    if not os.path.exists(out_pdf) or os.path.getsize(out_pdf) == 0:
        raise RuntimeError(f"weasyprint failed: {r.stderr[-500:]}")
    return out_pdf


def merge_version(version, jdk, title, subtitle):
    fqcns = sorted(target_fqcns(jdk))
    # only merge classes we actually have a PDF for
    present = [f for f in fqcns if os.path.exists(pdf_path(version, f))]
    missing = [f for f in fqcns if f not in set(present)]
    if missing:
        print(f"[{version}] WARNING: {len(missing)} types have no PDF; "
              f"skipping {missing[:10]}...", file=sys.stderr)

    # 1) merge class PDFs, recording each class's 0-indexed start page
    merged = PdfWriter()
    start_page = {}  # fqcn -> 0-indexed page in the class-only merge
    for fqcn in sorted(present):
        r = PdfReader(pdf_path(version, fqcn))
        start_page[fqcn] = len(merged.pages)
        for p in r.pages:
            merged.add_page(p)

    pkgs = group_by_package(present)

    # 2) title page
    title_pdf = os.path.join("/tmp", f"title_{version}.pdf")
    render_html(title_page_html(title, subtitle), title_pdf)
    title_pages = len(PdfReader(title_pdf).pages)

    # 3) render TOC (two passes so page numbers include title+TOC offset)
    offset = title_pages
    toc_pdf = os.path.join("/tmp", f"toc_{version}.pdf")
    for _ in range(5):
        page_of = {f: offset + start_page[f] + 1 for f in start_page}
        render_html(toc_html(pkgs, page_of), toc_pdf)
        n = len(PdfReader(toc_pdf).pages)
        if offset == title_pages + n:
            break
        offset = title_pages + n
    toc_pages = offset - title_pages

    # 4) final assembly: title page + TOC pages + class pages
    final = PdfWriter()
    for p in PdfReader(title_pdf).pages:
        final.add_page(p)
    for p in PdfReader(toc_pdf).pages:
        final.add_page(p)
    for p in merged.pages:
        final.add_page(p)

    # 5) bookmarks: title -> TOC -> package -> class
    top = final.add_outline_item(title, 0, None)
    final.add_outline_item("Table of Contents", title_pages, top)

    def final_page(f):
        return title_pages + toc_pages + start_page[f]

    for pkg in sorted(pkgs):
        pkg_item = final.add_outline_item(pkg, final_page(sorted(pkgs[pkg])[0]),
                                          top)
        for fqcn in sorted(pkgs[pkg]):
            final.add_outline_item(type_display(fqcn, pkg), final_page(fqcn),
                                   pkg_item)

    out = os.path.join(PDF_ROOT, f"{version}-api.pdf")
    with open(out, "wb") as fh:
        final.write(fh)
    print(f"[{version}] wrote {out}: {len(final.pages)} pages, "
          f"{len(present)} types ({toc_pages} TOC pages)")
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("cmd", choices=["generate", "merge", "all"])
    ap.add_argument("--workers", type=int, default=48)
    args = ap.parse_args()

    jobs = [
        ("java8", "JDK-8", "Java SE 8 API Specification",
         "Consolidated javadoc reference — JCK-testsuite conformance coverage"),
        ("java27", "JDK-27", "Java SE 27 API Specification",
         "Consolidated javadoc reference — JCK-testsuite conformance coverage"),
    ]
    for version, jdk, title, subtitle in jobs:
        if args.cmd in ("generate", "all"):
            generate(version, jdk, args.workers)
        if args.cmd in ("merge", "all"):
            merge_version(version, jdk, title, subtitle)


if __name__ == "__main__":
    main()
