/*
 * @test
 * @summary javax.xml.namespace.NamespaceContext conformance: white-box structure + black-box behavior.
 * @run main NamespaceContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class NamespaceContextTest {
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

        Class<?> c = Class.forName("javax.xml.namespace.NamespaceContext");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getNamespaceURI"), "method getNamespaceURI");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "getPrefixes"), "method getPrefixes");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.namespace.NamespaceContext a = new javax.xml.namespace.NamespaceContext(){
            public java.lang.String getNamespaceURI(java.lang.String p0) { return "s"; }
            public java.lang.String getPrefix(java.lang.String p0) { return "s"; }
            public java.util.Iterator<java.lang.String> getPrefixes(java.lang.String p0) { return null; }
        };
        check(a != null, "anon impl of NamespaceContext");
        check("s".equals(a.getNamespaceURI("s")), "getNamespaceURI() returns fixed default");
        check("s".equals(a.getPrefix("s")), "getPrefix() returns fixed default");
        check(a.getPrefixes("s") == null, "getPrefixes() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
