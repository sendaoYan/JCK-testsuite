/*
 * @test
 * @summary javax.xml.catalog.CatalogResolver conformance: white-box structure + black-box behavior.
 * @run main CatalogResolverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CatalogResolverTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.catalog.CatalogResolver");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "interface org.xml.sax.EntityResolver");
        check(hasInterface(c, "javax.xml.stream.XMLResolver"), "interface javax.xml.stream.XMLResolver");
        check(hasInterface(c, "javax.xml.transform.URIResolver"), "interface javax.xml.transform.URIResolver");
        check(hasInterface(c, "org.w3c.dom.ls.LSResourceResolver"), "interface org.w3c.dom.ls.LSResourceResolver");
        check(hasMethod(c, "resolve"), "method resolve");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
        check(hasMethod(c, "resolveResource"), "method resolveResource");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.catalog.CatalogFeatures cf = javax.xml.catalog.CatalogFeatures.builder()
            .with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, "continue").build();
        javax.xml.catalog.CatalogResolver cr = javax.xml.catalog.CatalogManager.catalogResolver(cf);
        check(cr != null, "resolver obtained");
        check(cr instanceof org.xml.sax.EntityResolver, "EntityResolver");
        check(cr instanceof javax.xml.stream.XMLResolver, "XMLResolver");
        check(cr instanceof javax.xml.transform.URIResolver, "URIResolver");
        check(cr instanceof org.w3c.dom.ls.LSResourceResolver, "LSResourceResolver");
        check(cr.resolveEntity(null, "http://example.invalid/dtd") == null, "resolveEntity unknown null");
        check(cr.resolve("http://example.invalid/uri", null) != null, "resolve returns Source for resolvable URI");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testResolveMatrix();
        testPreferSemantics();
        testNotFoundActions();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (resolve closed loop) ----
    static javax.xml.catalog.CatalogFeatures features(String resolve) {
        return javax.xml.catalog.CatalogFeatures.builder()
            .with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, resolve).build();
    }

    static void testResolveMatrix() throws Exception {
        String xml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n"
            + "  <system systemId=\"http://example.invalid/old.dtd\" uri=\"replaced/system.dtd\"/>\n"
            + "  <public publicId=\"-//EXAMPLE//DTD SAMPLE//EN\" uri=\"replaced/public.dtd\"/>\n"
            + "  <uri name=\"http://example.invalid/doc.html\" uri=\"replaced/doc.html\"/>\n"
            + "</catalog>\n";
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jcct-cr");
        java.nio.file.Path catFile = dir.resolve("catalog.xml");
        java.nio.file.Files.write(catFile, xml.getBytes("UTF-8"));
        try {
            String base = dir.toFile().toURI().toString();
            javax.xml.catalog.CatalogResolver cr =
                javax.xml.catalog.CatalogManager.catalogResolver(features("continue"), catFile.toUri());

            // EntityResolver form: matched systemId is rewritten to the catalog entry
            org.xml.sax.InputSource sys = cr.resolveEntity("-//EXAMPLE//DTD SAMPLE//EN", "http://example.invalid/old.dtd");
            check(sys != null && (base + "replaced/system.dtd").equals(sys.getSystemId()),
                  "resolveEntity rewrites systemId to catalog replacement");

            // no match under RESOLVE=continue: SAX contract returns null to fall through
            check(cr.resolveEntity(null, "http://example.invalid/none.dtd") == null,
                  "resolveEntity no match (continue) -> null");

            // URIResolver form
            javax.xml.transform.Source src = cr.resolve("http://example.invalid/doc.html", null);
            check(src != null && (base + "replaced/doc.html").equals(src.getSystemId()),
                  "resolve(href, base) rewrites to catalog replacement");
            javax.xml.transform.Source miss = cr.resolve("http://example.invalid/nope.html", null);
            check(miss != null && "http://example.invalid/nope.html".equals(miss.getSystemId()),
                  "resolve no match (continue) -> Source with original href");

            // StAX XMLResolver form: 4-arg resolveEntity returns null when nothing matches
            check(cr.resolveEntity("pid", "sid", "http://example.invalid/old.dtd", "base") == null,
                  "4-arg resolveEntity publicId/systemId form -> null when pair unmatched");

            // LSResourceResolver form
            org.w3c.dom.ls.LSInput ls = cr.resolveResource(
                null, null, null, "http://example.invalid/old.dtd", null);
            check(ls != null && (base + "replaced/system.dtd").equals(ls.getSystemId()),
                  "resolveResource resolves matched systemId");

            // strict mode: unmatched request throws CatalogException with the JAXP09040001 code
            javax.xml.catalog.CatalogResolver strict =
                javax.xml.catalog.CatalogManager.catalogResolver(features("strict"), catFile.toUri());
            boolean ce = false;
            String ceMsg = null;
            try { strict.resolveEntity(null, "http://example.invalid/none.dtd"); }
            catch (javax.xml.catalog.CatalogException e) { ce = true; ceMsg = e.getMessage(); }
            check(ce, "resolveEntity no match (strict) -> CatalogException");
            check(ceMsg != null && ceMsg.contains("JAXP09040001")
                  && ceMsg.contains("http://example.invalid/none.dtd"),
                  "strict CatalogException message carries JAXP09040001 and the systemId");
            javax.xml.catalog.CatalogException ceTyped = null;
            try { strict.resolve("http://example.invalid/nope.html", null); }
            catch (javax.xml.catalog.CatalogException e) { ceTyped = e; }
            check(ceTyped != null && ceTyped instanceof RuntimeException,
                  "resolve no match (strict) -> CatalogException (RuntimeException)");

            // ignore mode: unmatched request yields a non-null empty InputSource (no passthrough)
            javax.xml.catalog.CatalogResolver ignore =
                javax.xml.catalog.CatalogManager.catalogResolver(features("ignore"), catFile.toUri());
            org.xml.sax.InputSource ignored = ignore.resolveEntity(null, "http://example.invalid/none.dtd");
            check(ignored != null, "resolveEntity no match (ignore) -> non-null empty InputSource");
        } finally {
            java.nio.file.Files.delete(catFile);
            java.nio.file.Files.delete(dir);
        }
    }

    static void testPreferSemantics() throws Exception {
        // only a public entry is registered; PREFER decides whether publicId lookup is honored
        String xml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n"
            + "  <public publicId=\"-//EX//DTD P//EN\" uri=\"pub.dtd\"/>\n"
            + "</catalog>\n";
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jcct-cr2");
        java.nio.file.Path catFile = dir.resolve("catalog.xml");
        java.nio.file.Files.write(catFile, xml.getBytes("UTF-8"));
        try {
            String base = dir.toFile().toURI().toString();
            javax.xml.catalog.CatalogFeatures pub = javax.xml.catalog.CatalogFeatures.builder()
                .with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "public")
                .with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, "continue").build();
            javax.xml.catalog.CatalogResolver pubRes =
                javax.xml.catalog.CatalogManager.catalogResolver(pub, catFile.toUri());
            org.xml.sax.InputSource byPublic = pubRes.resolveEntity("-//EX//DTD P//EN", "http://unknown/system.dtd");
            check(byPublic != null && (base + "pub.dtd").equals(byPublic.getSystemId()),
                  "PREFER=public falls back to publicId when systemId unmatched");

            javax.xml.catalog.CatalogFeatures sys = javax.xml.catalog.CatalogFeatures.builder()
                .with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "system")
                .with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, "continue").build();
            javax.xml.catalog.CatalogResolver sysRes =
                javax.xml.catalog.CatalogManager.catalogResolver(sys, catFile.toUri());
            check(sysRes.resolveEntity("-//EX//DTD P//EN", "http://unknown/system.dtd") == null,
                  "PREFER=system ignores publicId-only match");

            // when BOTH entries match, the system entry wins regardless of PREFER
            java.nio.file.Path cat2 = dir.resolve("catalog2.xml");
            String xml2 =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n"
                + "  <system systemId=\"http://ex/sys.dtd\" uri=\"sysmatch.dtd\"/>\n"
                + "  <public publicId=\"-//EX//DTD P//EN\" uri=\"pubmatch.dtd\"/>\n"
                + "</catalog>\n";
            java.nio.file.Files.write(cat2, xml2.getBytes("UTF-8"));
            for (String prefer : new String[] {"public", "system"}) {
                javax.xml.catalog.CatalogFeatures f = javax.xml.catalog.CatalogFeatures.builder()
                    .with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, prefer)
                    .with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, "continue").build();
                javax.xml.catalog.CatalogResolver r =
                    javax.xml.catalog.CatalogManager.catalogResolver(f, cat2.toUri());
                org.xml.sax.InputSource is = r.resolveEntity("-//EX//DTD P//EN", "http://ex/sys.dtd");
                check(is != null && (base + "sysmatch.dtd").equals(is.getSystemId()),
                      "both match + PREFER=" + prefer + " -> system entry wins");
            }
            java.nio.file.Files.delete(cat2);
        } finally {
            java.nio.file.Files.delete(catFile);
            java.nio.file.Files.delete(dir);
        }
    }

    static void testNotFoundActions() {
        // NotFoundAction constants cover the three RESOLVE behaviors
        javax.xml.catalog.CatalogResolver.NotFoundAction[] vals =
            javax.xml.catalog.CatalogResolver.NotFoundAction.values();
        check(vals.length == 3, "three NotFoundAction constants");
        java.util.Set<String> names = new java.util.TreeSet<>();
        for (javax.xml.catalog.CatalogResolver.NotFoundAction v : vals) names.add(v.name());
        check(names.contains("CONTINUE") && names.contains("IGNORE") && names.contains("STRICT"),
              "constant names CONTINUE/IGNORE/STRICT");
        // getType maps the lowercase RESOLVE feature value to the action
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.getType("continue")
              == javax.xml.catalog.CatalogResolver.NotFoundAction.CONTINUE, "getType(continue)");
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.getType("ignore")
              == javax.xml.catalog.CatalogResolver.NotFoundAction.IGNORE, "getType(ignore)");
        check(javax.xml.catalog.CatalogResolver.NotFoundAction.getType("strict")
              == javax.xml.catalog.CatalogResolver.NotFoundAction.STRICT, "getType(strict)");
        boolean iae = false;
        try { javax.xml.catalog.CatalogResolver.NotFoundAction.getType("STRICT"); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "getType is case sensitive -> IAE for 'STRICT'");
        boolean iaeNull = false;
        try { javax.xml.catalog.CatalogResolver.NotFoundAction.getType(null); }
        catch (IllegalArgumentException e) { iaeNull = true; }
        check(iaeNull, "getType(null) -> IllegalArgumentException");
        // resolver honoring an explicit action
        javax.xml.catalog.Catalog empty =
            javax.xml.catalog.CatalogManager.catalog(javax.xml.catalog.CatalogFeatures.defaults(), new java.net.URI[0]);
        check(javax.xml.catalog.CatalogManager.catalogResolver(
                  empty, javax.xml.catalog.CatalogResolver.NotFoundAction.CONTINUE) != null,
              "catalogResolver(Catalog, NotFoundAction) factory");
    }
}
