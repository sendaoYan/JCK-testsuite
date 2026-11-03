/*
 * @test
 * @summary org.xml.sax.ext.EntityResolver2 conformance: white-box structure + black-box behavior.
 * @run main EntityResolver2Test
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EntityResolver2Test {
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

        Class<?> c = Class.forName("org.xml.sax.ext.EntityResolver2");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "interface org.xml.sax.EntityResolver");
        check(hasMethod(c, "getExternalSubset"), "method getExternalSubset");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.ext.EntityResolver2 a = new org.xml.sax.ext.EntityResolver2(){
            public org.xml.sax.InputSource getExternalSubset(java.lang.String p0, java.lang.String p1) { return null; }
            public org.xml.sax.InputSource resolveEntity(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) { return null; }
            public org.xml.sax.InputSource resolveEntity(java.lang.String p0, java.lang.String p1) { return null; }
        };
        check(a != null, "anon impl of EntityResolver2");
        check(a.getExternalSubset("s", "s") == null, "getExternalSubset() returns fixed default");
        check(a.resolveEntity("s", "s", "s", "s") == null, "resolveEntity() returns fixed default");
        check(a.resolveEntity("s", "s") == null, "resolveEntity() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
