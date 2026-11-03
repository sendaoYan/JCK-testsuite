/*
 * @test
 * @summary javax.xml.namespace.QName conformance: white-box structure + black-box behavior.
 * @run main QNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class QNameTest {
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

        Class<?> c = Class.forName("javax.xml.namespace.QName");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "java.io.Serializable"), "interface java.io.Serializable");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getLocalPart"), "method getLocalPart");
        check(hasMethod(c, "getNamespaceURI"), "method getNamespaceURI");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "valueOf"), "method valueOf");

        javax.xml.namespace.QName q = new javax.xml.namespace.QName("http://ns", "local", "p");
        check("http://ns".equals(q.getNamespaceURI()), "ns");
        check("local".equals(q.getLocalPart()), "local");
        check("p".equals(q.getPrefix()), "prefix");
        check(q.equals(new javax.xml.namespace.QName("http://ns", "local")), "equals");
        check(q.hashCode() == new javax.xml.namespace.QName("http://ns", "local").hashCode(), "hashCode");
        check("{http://ns}local".equals(q.toString()), "toString");
        check(javax.xml.namespace.QName.valueOf("{http://ns}local").equals(new javax.xml.namespace.QName("http://ns", "local")), "valueOf");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.namespace.QName q = new javax.xml.namespace.QName("http://ns", "local", "p");
        check("http://ns".equals(q.getNamespaceURI()) && "local".equals(q.getLocalPart()) && "p".equals(q.getPrefix()), "3-arg ctor");
        javax.xml.namespace.QName q2 = new javax.xml.namespace.QName("http://ns", "local");
        check("http://ns".equals(q2.getNamespaceURI()) && q2.getPrefix().equals(javax.xml.XMLConstants.DEFAULT_NS_PREFIX), "2-arg ctor prefix empty");
        check(q.equals(q2), "equals ignores prefix");
        check(q.hashCode() == q2.hashCode(), "hashCode consistent");
        javax.xml.namespace.QName q3 = new javax.xml.namespace.QName("local");
        check(q3.getNamespaceURI().equals(javax.xml.XMLConstants.NULL_NS_URI), "1-arg ctor null ns");
        check("{http://ns}local".equals(q.toString()), "toString with ns");
        check("local".equals(q3.toString()), "toString without ns");
        javax.xml.namespace.QName v = javax.xml.namespace.QName.valueOf("{http://ns}local");
        check("http://ns".equals(v.getNamespaceURI()) && "local".equals(v.getLocalPart()), "valueOf");
        check(!q.equals(null) && !q.equals("str"), "equals non-QName false");
        boolean ok = false;
        try { new javax.xml.namespace.QName("http://ns", (String) null); }
        catch (IllegalArgumentException e) { ok = true; }
        check(ok, "null local part -> IllegalArgumentException");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
