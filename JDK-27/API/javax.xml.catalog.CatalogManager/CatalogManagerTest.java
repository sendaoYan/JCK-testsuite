/*
 * @test
 * @summary javax.xml.catalog.CatalogManager conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
