/*
 * @test
 * @summary javax.xml.transform.sax.SAXSource conformance: API type verification.
 * @run main SAXSourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class SAXSourceTest {
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
        Class<?> c = Class.forName("javax.xml.transform.sax.SAXSource");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.transform.Source"), "interface javax.xml.transform.Source");
        check(hasMethod(c, "getInputSource"), "method getInputSource");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getXMLReader"), "method getXMLReader");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setInputSource"), "method setInputSource");
        check(hasMethod(c, "setSystemId"), "method setSystemId");
        check(hasMethod(c, "setXMLReader"), "method setXMLReader");
        check(hasMethod(c, "sourceToInputSource"), "method sourceToInputSource");
        check(hasField(c, "FEATURE"), "field FEATURE");

        javax.xml.transform.sax.SAXSource s = new javax.xml.transform.sax.SAXSource();
        check(s.getInputSource() == null, "inputSource null");
        check(s.isEmpty(), "isEmpty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
