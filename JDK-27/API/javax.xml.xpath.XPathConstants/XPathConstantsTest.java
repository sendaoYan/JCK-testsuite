/*
 * @test
 * @summary javax.xml.xpath.XPathConstants conformance: API type verification.
 * @run main XPathConstantsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathConstantsTest {
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
        Class<?> c = Class.forName("javax.xml.xpath.XPathConstants");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasField(c, "BOOLEAN"), "field BOOLEAN");
        check(hasField(c, "DOM_OBJECT_MODEL"), "field DOM_OBJECT_MODEL");
        check(hasField(c, "NODE"), "field NODE");
        check(hasField(c, "NODESET"), "field NODESET");
        check(hasField(c, "NUMBER"), "field NUMBER");
        check(hasField(c, "STRING"), "field STRING");

        check(javax.xml.xpath.XPathConstants.NUMBER != null, "NUMBER");
        check(javax.xml.xpath.XPathConstants.STRING != null, "STRING");
        check(javax.xml.xpath.XPathConstants.BOOLEAN != null, "BOOLEAN");
        check(javax.xml.xpath.XPathConstants.NODESET != null, "NODESET");
        check(javax.xml.xpath.XPathConstants.NODE != null, "NODE");
        check("http://java.sun.com/jaxp/xpath/dom".equals(javax.xml.xpath.XPathConstants.DOM_OBJECT_MODEL), "DOM_OBJECT_MODEL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
