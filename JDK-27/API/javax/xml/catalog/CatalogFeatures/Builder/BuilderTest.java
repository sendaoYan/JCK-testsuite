/*
 * @test
 * @summary javax.xml.catalog.CatalogFeatures.Builder conformance: white-box structure + black-box behavior.
 * @run main BuilderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class BuilderTest {
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

        Class<?> c = Class.forName("javax.xml.catalog.CatalogFeatures$Builder");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "build"), "method build");
        check(hasMethod(c, "with"), "method with");

        javax.xml.catalog.CatalogFeatures.Builder b = javax.xml.catalog.CatalogFeatures.builder();
        check(b.with(javax.xml.catalog.CatalogFeatures.Feature.DEFER, "true") == b, "with returns this");
        check(b.build() != null, "build");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.catalog.CatalogFeatures.Builder b = javax.xml.catalog.CatalogFeatures.builder();
        check(b != null, "builder()");
        check(b.with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, "continue") == b, "with returns this");
        javax.xml.catalog.CatalogFeatures cf = b.build();
        check("continue".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE)), "build applies with");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBuildSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepening (build semantics) ----
    static void testBuildSemantics() {
        // chaining: with() returns this, so a single expression sets everything
        javax.xml.catalog.CatalogFeatures cf = javax.xml.catalog.CatalogFeatures.builder()
            .with(javax.xml.catalog.CatalogFeatures.Feature.FILES, "file:///tmp/jcct-builder.xml")
            .with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "system")
            .with(javax.xml.catalog.CatalogFeatures.Feature.DEFER, "false")
            .with(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE, "continue")
            .build();
        check("file:///tmp/jcct-builder.xml".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.FILES)),
              "chained build FILES");
        check("system".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)), "chained build PREFER");
        check("false".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.DEFER)), "chained build DEFER");
        check("continue".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE)), "chained build RESOLVE");

        // the same builder can build repeatedly; later with() applies only to future builds
        javax.xml.catalog.CatalogFeatures.Builder b = javax.xml.catalog.CatalogFeatures.builder();
        javax.xml.catalog.CatalogFeatures first = b.build();
        b.with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "system");
        javax.xml.catalog.CatalogFeatures second = b.build();
        check("public".equals(first.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)),
              "first build captured pre-mutation state");
        check("system".equals(second.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)),
              "second build sees the mutation");

        // with() validates eagerly (throws before build())
        boolean iae = false;
        try {
            javax.xml.catalog.CatalogFeatures.Builder bad = javax.xml.catalog.CatalogFeatures.builder();
            bad.with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "bogus");
            bad.build();
        } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "invalid PREFER rejected during with() (before build)");

        // null feature -> NPE
        boolean npe = false;
        try { javax.xml.catalog.CatalogFeatures.builder().with(null, "x"); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "with(null, value) -> NullPointerException");

        // build() yields the documented defaults when nothing was set
        javax.xml.catalog.CatalogFeatures plain = javax.xml.catalog.CatalogFeatures.builder().build();
        check(plain.get(javax.xml.catalog.CatalogFeatures.Feature.FILES) == null, "plain build FILES null");
        check("public".equals(plain.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)), "plain build PREFER public");
        check("true".equals(plain.get(javax.xml.catalog.CatalogFeatures.Feature.DEFER)), "plain build DEFER true");
        check("strict".equals(plain.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE)), "plain build RESOLVE strict");
    }
}
