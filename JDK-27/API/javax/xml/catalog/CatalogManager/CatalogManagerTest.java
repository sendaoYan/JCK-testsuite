/*
 * @test
 * @summary javax.xml.catalog.CatalogManager conformance: white-box structure + black-box behavior.
 * @run main CatalogManagerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CatalogManagerTest {
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

        Class<?> c = Class.forName("javax.xml.catalog.CatalogManager");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "catalog"), "method catalog");
        check(hasMethod(c, "catalogResolver"), "method catalogResolver");

        javax.xml.catalog.CatalogResolver r = javax.xml.catalog.CatalogManager.catalogResolver(javax.xml.catalog.CatalogFeatures.defaults());
        check(r != null, "catalogResolver");
        javax.xml.catalog.Catalog cat = javax.xml.catalog.CatalogManager.catalog(javax.xml.catalog.CatalogFeatures.defaults());
        check(cat != null, "catalog");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.catalog.CatalogFeatures cf = javax.xml.catalog.CatalogFeatures.defaults();
        javax.xml.catalog.CatalogResolver cr = javax.xml.catalog.CatalogManager.catalogResolver(cf);
        check(cr != null, "catalogResolver(CatalogFeatures)");
        check(javax.xml.catalog.CatalogManager.catalogResolver(cf, new java.net.URI[0]) != null, "catalogResolver(CatalogFeatures, URIs)");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRewriteClosedLoop();
        testSystemPropertyRoute();
        testNullAndEmptyInputs();
        testMissingCatalogFile();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (catalog rewrite closed loop) ----
    static final String CATALOG_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n"
        + "  <system systemId=\"http://example.invalid/old.dtd\" uri=\"replaced/system.dtd\"/>\n"
        + "  <systemSuffix systemIdSuffix=\".xsd\" uri=\"replaced/suffix.xsd\"/>\n"
        + "  <public publicId=\"-//EXAMPLE//DTD SAMPLE//EN\" uri=\"replaced/public.dtd\"/>\n"
        + "  <uri name=\"http://example.invalid/doc.html\" uri=\"replaced/doc.html\"/>\n"
        + "  <rewriteSystem systemIdStartString=\"http://example.invalid/rew/\" rewritePrefix=\"rewritten/\"/>\n"
        + "</catalog>\n";

    static void testRewriteClosedLoop() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jcct-cm");
        java.nio.file.Path catFile = dir.resolve("catalog.xml");
        java.nio.file.Files.write(catFile, CATALOG_XML.getBytes("UTF-8"));
        try {
            javax.xml.catalog.CatalogFeatures cf = javax.xml.catalog.CatalogFeatures.builder()
                .with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, "continue").build();
            javax.xml.catalog.Catalog c = javax.xml.catalog.CatalogManager.catalog(cf, catFile.toUri());
            check(c != null, "catalog(CatalogFeatures, URI) non-null");

            // match results are absolute URIs resolved against the catalog file location
            String base = dir.toFile().toURI().toString();
            check((base + "replaced/system.dtd").equals(c.matchSystem("http://example.invalid/old.dtd")),
                  "matchSystem exact rewrite (system entry)");
            check((base + "rewritten/x.dtd").equals(c.matchSystem("http://example.invalid/rew/x.dtd")),
                  "matchSystem rewriteSystem prefix replacement");
            check((base + "replaced/suffix.xsd").equals(c.matchSystem("http://example.invalid/data.xsd")),
                  "matchSystem systemSuffix match");
            check((base + "replaced/doc.html").equals(c.matchURI("http://example.invalid/doc.html")),
                  "matchURI exact rewrite (uri entry)");
            check((base + "replaced/public.dtd").equals(c.matchPublic("-//EXAMPLE//DTD SAMPLE//EN")),
                  "matchPublic exact rewrite (public entry)");

            // no match returns null (match* do not throw)
            check(c.matchSystem("http://example.invalid/none.dtd") == null, "matchSystem no match -> null");
            check(c.matchURI("http://example.invalid/nope.html") == null, "matchURI no match -> null");
            check(c.matchPublic("-//NOPE//DTD//EN") == null, "matchPublic no match -> null");

            // null arguments: matchSystem rejects with NPE, public/URI answer null (recorded)
            boolean npeSystem = false;
            try { c.matchSystem(null); } catch (NullPointerException e) { npeSystem = true; }
            check(npeSystem, "matchSystem(null) -> NullPointerException");
            check(c.matchPublic(null) == null, "matchPublic(null) -> null");
            check(c.matchURI(null) == null, "matchURI(null) -> null");

            // flat catalog: no delegated/next catalogs in the stream
            check(c.catalogs().count() == 0, "flat catalog catalogs() count == 0");

            // resolver overloads on a loaded Catalog
            javax.xml.catalog.CatalogResolver cont =
                javax.xml.catalog.CatalogManager.catalogResolver(c, javax.xml.catalog.CatalogResolver.NotFoundAction.CONTINUE);
            check(cont != null, "catalogResolver(Catalog, NotFoundAction.CONTINUE)");
            org.xml.sax.InputSource is = cont.resolveEntity(null, "http://example.invalid/old.dtd");
            check(is != null && (base + "replaced/system.dtd").equals(is.getSystemId()),
                  "resolver(Catalog, CONTINUE) resolveEntity rewrites systemId");
            javax.xml.catalog.CatalogResolver strict =
                javax.xml.catalog.CatalogManager.catalogResolver(c, javax.xml.catalog.CatalogResolver.NotFoundAction.STRICT);
            boolean ce = false;
            try { strict.resolveEntity(null, "http://example.invalid/none.dtd"); }
            catch (javax.xml.catalog.CatalogException e) { ce = true; }
            check(ce, "resolver(Catalog, STRICT) no-match -> CatalogException");
        } finally {
            java.nio.file.Files.delete(catFile);
            java.nio.file.Files.delete(dir);
            check(!java.nio.file.Files.exists(catFile), "temp catalog deleted");
        }
    }

    static void testSystemPropertyRoute() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jcct-cm2");
        java.nio.file.Path catFile = dir.resolve("catalog.xml");
        java.nio.file.Files.write(catFile, CATALOG_XML.getBytes("UTF-8"));
        System.setProperty("javax.xml.catalog.files", catFile.toUri().toString());
        try {
            javax.xml.catalog.Catalog c = javax.xml.catalog.CatalogManager.catalog(javax.xml.catalog.CatalogFeatures.defaults());
            check(c != null, "catalog via javax.xml.catalog.files system property");
            String base = dir.toFile().toURI().toString();
            check((base + "replaced/system.dtd").equals(c.matchSystem("http://example.invalid/old.dtd")),
                  "system-property-configured catalog performs rewrite");
        } finally {
            System.clearProperty("javax.xml.catalog.files");
            java.nio.file.Files.delete(catFile);
            java.nio.file.Files.delete(dir);
        }
        check(System.getProperty("javax.xml.catalog.files") == null, "system property cleared");
    }

    static void testNullAndEmptyInputs() {
        boolean npe1 = false, npe2 = false;
        try { javax.xml.catalog.CatalogManager.catalog(javax.xml.catalog.CatalogFeatures.defaults(), (java.net.URI[]) null); }
        catch (NullPointerException e) { npe1 = true; }
        check(npe1, "catalog(features, null URIs array) -> NullPointerException");
        try { javax.xml.catalog.CatalogManager.catalogResolver((javax.xml.catalog.Catalog) null); }
        catch (NullPointerException e) { npe2 = true; }
        check(npe2, "catalogResolver(null Catalog) -> NullPointerException");
        javax.xml.catalog.Catalog empty =
            javax.xml.catalog.CatalogManager.catalog(javax.xml.catalog.CatalogFeatures.defaults(), new java.net.URI[0]);
        check(empty != null, "catalog(features, empty URIs) returns an empty catalog");
        check(empty.matchSystem("anything") == null, "empty catalog never matches");
        javax.xml.catalog.CatalogResolver r =
            javax.xml.catalog.CatalogManager.catalogResolver(javax.xml.catalog.CatalogFeatures.defaults(), new java.net.URI[0]);
        check(r != null, "catalogResolver(features, empty URIs)");
    }

    static void testMissingCatalogFile() throws Exception {
        // a nonexistent catalog path yields a lazily-loaded catalog; matching just finds nothing
        javax.xml.catalog.Catalog c = javax.xml.catalog.CatalogManager.catalog(
            javax.xml.catalog.CatalogFeatures.defaults(),
            java.net.URI.create("file:/tmp/jcct-definitely-missing-catalog.xml"));
        check(c != null, "missing catalog file still returns a Catalog (lazy load)");
        check(c.matchSystem("http://example.invalid/old.dtd") == null, "missing catalog matchSystem -> null");
    }
}
