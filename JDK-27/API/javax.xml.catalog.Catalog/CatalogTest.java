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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
