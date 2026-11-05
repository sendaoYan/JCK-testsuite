/*
 * @test
 * @summary javax.xml.catalog.CatalogException conformance: white-box structure + black-box behavior.
 * @run main CatalogExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CatalogExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.catalog.CatalogException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass java.lang.RuntimeException");

        javax.xml.catalog.CatalogException e = new javax.xml.catalog.CatalogException("m");
        check("m".equals(e.getMessage()), "ctor msg");
        check(new javax.xml.catalog.CatalogException("m", new RuntimeException("c")).getCause() != null, "cause");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.catalog.CatalogException e = new javax.xml.catalog.CatalogException("m");
        check("m".equals(e.getMessage()), "message ctor");
        javax.xml.catalog.CatalogException e2 = new javax.xml.catalog.CatalogException("m", new RuntimeException("c"));
        check("m".equals(e2.getMessage()) && e2.getCause() != null, "message + throwable");
        check(e instanceof java.lang.RuntimeException, "is RuntimeException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorMatrix();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (constructor matrix + real trigger) ----
    static void testConstructorMatrix() throws Exception {
        // exactly two constructors (verified against javap: (String) and (String,Throwable))
        java.lang.reflect.Constructor<?>[] ctors =
            Class.forName("javax.xml.catalog.CatalogException").getDeclaredConstructors();
        check(ctors.length == 2, "exactly two constructors");
        int stringOnly = 0, stringThrowable = 0;
        for (java.lang.reflect.Constructor<?> ct : ctors) {
            Class<?>[] ps = ct.getParameterTypes();
            if (ps.length == 1 && ps[0] == String.class) stringOnly++;
            else if (ps.length == 2 && ps[0] == String.class && Throwable.class.isAssignableFrom(ps[1])) stringThrowable++;
        }
        check(stringOnly == 1, "one (String) ctor");
        check(stringThrowable == 1, "one (String,Throwable) ctor");

        javax.xml.catalog.CatalogException e1 = new javax.xml.catalog.CatalogException("JAXP09040001: m");
        check("JAXP09040001: m".equals(e1.getMessage()), "(String) message verbatim");
        check(e1.getCause() == null, "(String) cause null");
        RuntimeException root = new RuntimeException("root");
        javax.xml.catalog.CatalogException e2 = new javax.xml.catalog.CatalogException("m2", root);
        check("m2".equals(e2.getMessage()) && e2.getCause() == root, "(String,Throwable) message and cause identity");
        // unchecked: no throws clause anywhere
        check(javax.xml.catalog.CatalogException.class != null
              && java.lang.reflect.Modifier.isPublic(javax.xml.catalog.CatalogException.class.getModifiers()),
              "public unchecked exception");
    }

    static void testRealTrigger() throws Exception {
        // RESOLVE=strict + unmatched request is the documented trigger path
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jcct-ce");
        java.nio.file.Path catFile = dir.resolve("catalog.xml");
        java.nio.file.Files.write(catFile,
            ("<?xml version=\"1.0\"?><catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">"
             + "<system systemId=\"http://example.invalid/known.dtd\" uri=\"known.dtd\"/></catalog>")
                .getBytes("UTF-8"));
        try {
            javax.xml.catalog.CatalogFeatures strict = javax.xml.catalog.CatalogFeatures.builder()
                .with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, "strict").build();
            javax.xml.catalog.CatalogResolver cr = javax.xml.catalog.CatalogManager.catalogResolver(strict, catFile.toUri());
            boolean caught = false;
            String msg = null;
            try { cr.resolveEntity(null, "http://example.invalid/unknown.dtd"); }
            catch (javax.xml.catalog.CatalogException e) { caught = true; msg = e.getMessage(); }
            check(caught, "strict resolver raises CatalogException on unmatched systemId");
            check(msg != null && msg.contains("JAXP09040001")
                  && msg.contains("http://example.invalid/unknown.dtd"),
                  "trigger message carries JAXP09040001 and the offending systemId");
        } finally {
            java.nio.file.Files.delete(catFile);
            java.nio.file.Files.delete(dir);
        }
    }
}
