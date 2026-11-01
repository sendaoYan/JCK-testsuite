# spec-docs — Java API reference PDFs

Per-class javadoc PDFs and consolidated per-JDK API PDFs generated from
Oracle's javadoc HTML. These are the reference documents for the API
conformance tests under `JDK-8/API/` and `JDK-27/API/` — one PDF per covered
type.

## Layout

- `java8-api/`, `java27-api/` — local HTML mirrors of the Oracle javadoc
  (gitignored; re-downloadable, see Step 1 below).
- `pdf/java8/`, `pdf/java27/` — one `<fqcn>.pdf` per covered type.
- `pdf/java8-api.pdf`, `pdf/java27-api.pdf` — the consolidated PDF, one per
  JDK version, containing a title page, a rendered Table of Contents
  (packages → classes, with page numbers), and a hierarchical PDF outline
  (title → Table of Contents → package → class).
- `build_api_pdf.py` — generator + merger (recommended; handles nested
  types and the Java 27 module-qualified layout).
- `gen_pdfs.py` — older, simpler per-class generator (top-level classes only).

## Prerequisites

- Python 3 with `pypdf` (`pip install pypdf`).
- [WeasyPrint](https://doc.courtbouillon.org/weasyprint/stable/) — the
  HTML → PDF renderer. The scripts default to the binary at
  `/data/yansendao/python/binary/hpython/build/pgo-default-with-jit/install/bin/weasyprint`;
  edit `WEASY` in either script to point elsewhere.

## Step 1 — download the javadoc HTML mirror

The mirrors are plain `wget --mirror` snapshots of the Oracle javadoc site,
rooted so that the hostname is preserved on disk (that is exactly the path
`build_api_pdf.py` expects).

```sh
cd spec-docs

# Java SE 8 API
wget --mirror --convert-links --page-requisites --no-parent \
     --directory-prefix=java8-api \
     https://docs.oracle.com/javase/8/docs/api/

# Java SE 27 API
wget --mirror --convert-links --page-requisites --no-parent \
     --directory-prefix=java27-api \
     https://docs.oracle.com/en/java/javase/27/docs/api/
```

`--page-requisites` fetches the stylesheet (and images) the renderer needs;
without it WeasyPrint renders the pages unstyled. The mirrors are large
(thousands of HTML files) and are deliberately **not** committed — only the
generated PDFs are.

## Step 2 — generate the per-class PDFs

`build_api_pdf.py` discovers the type universe from the conformance-test
directories (`JDK-{8,27}/API/<fqcn>/<Simple>Test.java`) and generates any
missing `<fqcn>.pdf`:

```sh
cd spec-docs
python3 build_api_pdf.py generate --workers 48
```

The render is CPU-bound; parallelise with `--workers` (default 48). The step
is resumable — existing PDFs are skipped.

## Step 3 — build the consolidated PDF

```sh
cd spec-docs
python3 build_api_pdf.py merge
```

This merges each version's per-class PDFs into `pdf/java8-api.pdf` /
`pdf/java27-api.pdf`, prepends a title page and a rendered Table of Contents
(page numbers are computed in two passes so they stay correct), and adds the
PDF outline. `python3 build_api_pdf.py all` runs Steps 2 and 3 together.

The merged PDFs exceed GitHub's 100 MB per-file limit (pypdf duplicates the
embedded fonts across all per-class files). Re-compress them before pushing:

```sh
gs -q -dBATCH -dNOPAUSE -sDEVICE=pdfwrite -dPDFSETTINGS=/default \
   -o pdf/java8-api.pdf pdf/java8-api.pdf
gs -q -dBATCH -dNOPAUSE -sDEVICE=pdfwrite -dPDFSETTINGS=/default \
   -o pdf/java27-api.pdf pdf/java27-api.pdf
```

This deduplicates/subset fonts (~17 % smaller) and preserves the PDF outline.

## Notes

- **Nested types** — `java.util.Map.Entry` lives at `java/util/Map.Entry.html`,
  *not* `java/util/Map/Entry.html`. `build_api_pdf.py` resolves this by walking
  the mirror and deriving the fqcn from the filename; `gen_pdfs.py` does not
  and silently skips nested types.
- **Java 27 modules** — the Java 27 javadoc puts a module segment before the
  package path (`…/api/java.base/java/lang/String.html`); the generator drops
  the first path segment when deriving the package.
- **Removed types** — types present in Java 8 but removed later
  (`java.lang.Compiler`, `java.util.jar.Pack200*`) only appear in the
  `java8-api.pdf` document.
