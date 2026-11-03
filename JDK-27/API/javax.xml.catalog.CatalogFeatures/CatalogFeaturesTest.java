/*
 * @test
 * @summary javax.xml.catalog.CatalogFeatures conformance: white-box structure + black-box behavior.
 * @run main CatalogFeaturesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CatalogFeaturesTest {
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

        Class<?> c = Class.forName("javax.xml.catalog.CatalogFeatures");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "builder"), "method builder");
        check(hasMethod(c, "defaults"), "method defaults");
        check(hasMethod(c, "get"), "method get");

        javax.xml.catalog.CatalogFeatures f = javax.xml.catalog.CatalogFeatures.defaults();
        check(f != null, "defaults");
        javax.xml.catalog.CatalogFeatures b = javax.xml.catalog.CatalogFeatures.builder()
            .with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "system").build();
        check(b != null, "builder build");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.catalog.CatalogFeatures cf = javax.xml.catalog.CatalogFeatures.defaults();
        check(cf != null, "defaults");
        check("public".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)), "default PREFER");
        check("strict".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE)), "default RESOLVE");
        check("true".equals(cf.get(javax.xml.catalog.CatalogFeatures.Feature.DEFER)), "default DEFER");
        javax.xml.catalog.CatalogFeatures.Builder b = javax.xml.catalog.CatalogFeatures.builder();
        javax.xml.catalog.CatalogFeatures cf2 = b.with(javax.xml.catalog.CatalogFeatures.Feature.PREFER, "system").build();
        check("system".equals(cf2.get(javax.xml.catalog.CatalogFeatures.Feature.PREFER)), "builder with PREFER");
        check("strict".equals(cf2.get(javax.xml.catalog.CatalogFeatures.Feature.RESOLVE)), "builder inherits default RESOLVE");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
