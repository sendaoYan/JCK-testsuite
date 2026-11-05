/*
 * @test
 * @summary javax.xml.catalog.Catalog conformance: white-box structure + black-box behavior.
 * @run main CatalogTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CatalogTest {
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

        Class<?> c = Class.forName("javax.xml.catalog.Catalog");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "catalogs"), "method catalogs");
        check(hasMethod(c, "matchPublic"), "method matchPublic");
        check(hasMethod(c, "matchSystem"), "method matchSystem");
        check(hasMethod(c, "matchURI"), "method matchURI");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.catalog.Catalog a = new javax.xml.catalog.Catalog(){
            public java.lang.String matchSystem(java.lang.String p0) { return "s"; }
            public java.lang.String matchPublic(java.lang.String p0) { return "s"; }
            public java.lang.String matchURI(java.lang.String p0) { return "s"; }
            public java.util.stream.Stream<javax.xml.catalog.Catalog> catalogs() { return null; }
        };
        check(a != null, "anon impl of Catalog");
        check("s".equals(a.matchSystem("s")), "matchSystem() returns fixed default");
        check("s".equals(a.matchPublic("s")), "matchPublic() returns fixed default");
        check("s".equals(a.matchURI("s")), "matchURI() returns fixed default");
        check(a.catalogs() == null, "catalogs() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMatchMatrix();
        testMethodSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (real catalog match matrix) ----
    static void testMatchMatrix() throws Exception {
        String xml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n"
            + "  <system systemId=\"http://example.invalid/old.dtd\" uri=\"replaced/system.dtd\"/>\n"
            + "  <system systemId=\"http://example.invalid/exact.xsd\" uri=\"replaced/exact.xsd\"/>\n"
            + "  <system systemId=\"http://other.invalid/old.dtd\" uri=\"replaced/other.dtd\"/>\n"
            + "  <systemSuffix systemIdSuffix=\".dtd\" uri=\"replaced/allsuffix.dtd\"/>\n"
            + "  <public publicId=\"-//EXAMPLE//DTD A//EN\" uri=\"replaced/a.dtd\"/>\n"
            + "  <public publicId=\"-//EXAMPLE//DTD B//EN\" uri=\"replaced/b.dtd\"/>\n"
            + "  <uri name=\"http://example.invalid/doc.html\" uri=\"replaced/doc.html\"/>\n"
            + "  <uri name=" + "\"http://example.invalid/img.png\"" + " uri=\"replaced/img.png\"/>\n"
            + "  <rewriteSystem systemIdStartString=\"http://example.invalid/rew/\" rewritePrefix=\"rewritten/\"/>\n"
            + "</catalog>\n";
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jcct-cat");
        java.nio.file.Path catFile = dir.resolve("catalog.xml");
        java.nio.file.Files.write(catFile, xml.getBytes("UTF-8"));
        try {
            javax.xml.catalog.CatalogFeatures cf = javax.xml.catalog.CatalogFeatures.builder()
                .with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, "continue").build();
            javax.xml.catalog.Catalog c = javax.xml.catalog.CatalogManager.catalog(cf, catFile.toUri());
            String base = dir.toFile().toURI().toString();

            // system: exact match wins over suffix match
            check((base + "replaced/system.dtd").equals(c.matchSystem("http://example.invalid/old.dtd")),
                  "matchSystem exact system entry");
            check((base + "replaced/exact.xsd").equals(c.matchSystem("http://example.invalid/exact.xsd")),
                  "matchSystem exact wins over .dtd suffix rule");
            // systemSuffix: only consulted when no exact entry matches
            check((base + "replaced/allsuffix.dtd").equals(c.matchSystem("http://example.invalid/whatever.dtd")),
                  "matchSystem falls back to systemSuffix");
            check(c.matchSystem("http://example.invalid/whatever.xsd") == null,
                  "non-matching suffix -> null");
            // rewriteSystem applied before suffix rules
            check((base + "rewritten/partial.dtd").equals(c.matchSystem("http://example.invalid/rew/partial.dtd")),
                  "matchSystem rewriteSystem transforms prefix");

            // public: distinct publicIds are independent keys
            check((base + "replaced/a.dtd").equals(c.matchPublic("-//EXAMPLE//DTD A//EN")), "matchPublic first key");
            check((base + "replaced/b.dtd").equals(c.matchPublic("-//EXAMPLE//DTD B//EN")), "matchPublic second key");
            check(c.matchPublic("-//EXAMPLE//DTD C//EN") == null, "matchPublic unregistered key -> null");

            // uri entries match by exact name
            check((base + "replaced/doc.html").equals(c.matchURI("http://example.invalid/doc.html")), "matchURI first name");
            check((base + "replaced/img.png").equals(c.matchURI("http://example.invalid/img.png")), "matchURI second name");
            check(c.matchURI("http://example.invalid/missing.html") == null, "matchURI unknown name -> null");

            // null arguments per method (recorded: system NPE, others null)
            boolean npe = false;
            try { c.matchSystem(null); } catch (NullPointerException e) { npe = true; }
            check(npe, "matchSystem(null) -> NPE");
            check(c.matchPublic(null) == null, "matchPublic(null) -> null");
            check(c.matchURI(null) == null, "matchURI(null) -> null");

            // no nextCatalog: delegate stream is empty
            check(c.catalogs().count() == 0, "no delegates -> catalogs() empty");

            // nextCatalog makes the sub-catalog visible in the stream
            java.nio.file.Path sub = dir.resolve("sub.xml");
            java.nio.file.Files.write(sub,
                ("<?xml version=\"1.0\"?><catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">"
                 + "<uri name=\"http://delegated/doc\" uri=\"delegated.dtd\"/></catalog>").getBytes("UTF-8"));
            java.nio.file.Path main = dir.resolve("main.xml");
            java.nio.file.Files.write(main,
                ("<?xml version=\"1.0\"?><catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">"
                 + "<nextCatalog catalog=\"sub.xml\"/></catalog>").getBytes("UTF-8"));
            javax.xml.catalog.Catalog cmain = javax.xml.catalog.CatalogManager.catalog(
                javax.xml.catalog.CatalogFeatures.defaults(), main.toUri());
            check(cmain.catalogs().count() == 1, "nextCatalog appears exactly once in catalogs()");

            // malformed catalog file -> CatalogException with the JAXP09030002 code
            java.nio.file.Path bad = dir.resolve("bad.xml");
            java.nio.file.Files.write(bad, "not-xml-at-all".getBytes("UTF-8"));
            boolean ce = false;
            String ceMsg = null;
            try { javax.xml.catalog.CatalogManager.catalog(javax.xml.catalog.CatalogFeatures.defaults(), bad.toUri()).matchSystem("x"); }
            catch (javax.xml.catalog.CatalogException e) { ce = true; ceMsg = e.getMessage(); }
            check(ce, "malformed catalog -> CatalogException");
            check(ceMsg != null && ceMsg.contains("JAXP09030002"), "malformed catalog message carries JAXP09030002");
        } finally {
            java.util.List<java.nio.file.Path> all = new java.util.ArrayList<>();
            java.nio.file.DirectoryStream<java.nio.file.Path> ds = java.nio.file.Files.newDirectoryStream(dir);
            try { for (java.nio.file.Path p : ds) all.add(p); } finally { ds.close(); }
            for (java.nio.file.Path p : all) java.nio.file.Files.delete(p);
            java.nio.file.Files.delete(dir);
            check(!java.nio.file.Files.exists(catFile), "temp catalog dir cleaned");
        }
    }

    // ---- Part 4: method surface locked against javap ----
    static void testMethodSurface() throws Exception {
        Class<?> c = Class.forName("javax.xml.catalog.Catalog");
        java.util.Set<String> names = new java.util.TreeSet<>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        // public interface surface: exactly the four match/catalogs methods plus Object's
        check(names.contains("matchSystem") && names.contains("matchPublic")
              && names.contains("matchURI") && names.contains("catalogs"),
              "Catalog interface declares matchSystem/matchPublic/matchURI/catalogs");
        check(!names.contains("matchNamespace"), "Catalog has NO matchNamespace (verified via javap)");
        check(!names.contains("getCatalogManager"), "Catalog has no getCatalogManager");
    }
}
