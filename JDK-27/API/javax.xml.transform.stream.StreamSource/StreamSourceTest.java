/*
 * @test
 * @summary javax.xml.transform.stream.StreamSource conformance: API type verification.
 * @run main StreamSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StreamSourceTest {
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
        Class<?> c = Class.forName("javax.xml.transform.stream.StreamSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Source"), "interface javax.xml.transform.Source");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "getPublicId"), "method getPublicId");
        check(hasMethod(c, "getReader"), "method getReader");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setInputStream"), "method setInputStream");
        check(hasMethod(c, "setPublicId"), "method setPublicId");
        check(hasMethod(c, "setReader"), "method setReader");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.transform.stream.StreamSource s = new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<a/>"));
        check(s.getReader() != null, "getReader");
        check(!s.isEmpty(), "not empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
