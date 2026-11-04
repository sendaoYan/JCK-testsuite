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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
