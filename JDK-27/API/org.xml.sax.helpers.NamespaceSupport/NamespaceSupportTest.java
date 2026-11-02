/*
 * @test
 * @summary org.xml.sax.helpers.NamespaceSupport conformance: API type verification.
 * @run main NamespaceSupportTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class NamespaceSupportTest {
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
        Class<?> c = Class.forName("org.xml.sax.helpers.NamespaceSupport");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "declarePrefix"), "method declarePrefix");
        check(hasMethod(c, "getDeclaredPrefixes"), "method getDeclaredPrefixes");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "getPrefixes"), "method getPrefixes");
        check(hasMethod(c, "getURI"), "method getURI");
        check(hasMethod(c, "isNamespaceDeclUris"), "method isNamespaceDeclUris");
        check(hasMethod(c, "popContext"), "method popContext");
        check(hasMethod(c, "processName"), "method processName");
        check(hasMethod(c, "pushContext"), "method pushContext");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setNamespaceDeclUris"), "method setNamespaceDeclUris");
        check(hasField(c, "NSDECL"), "field NSDECL");
        check(hasField(c, "XMLNS"), "field XMLNS");

        org.xml.sax.helpers.NamespaceSupport ns = new org.xml.sax.helpers.NamespaceSupport();
        ns.pushContext();
        check(ns.declarePrefix("p", "http://ns"), "declarePrefix");
        check("http://ns".equals(ns.getURI("p")), "getURI");
        check("p".equals(ns.getPrefix("http://ns")), "getPrefix");
        ns.popContext();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
