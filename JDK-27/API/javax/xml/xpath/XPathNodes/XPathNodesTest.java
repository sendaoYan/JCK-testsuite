/*
 * @test
 * @summary javax.xml.xpath.XPathNodes conformance: white-box structure + black-box behavior.
 * @run main XPathNodesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathNodesTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathNodes");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "java.lang.Iterable"), "interface java.lang.Iterable");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "iterator"), "method iterator");
        check(hasMethod(c, "size"), "method size");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.xpath.XPathNodes a = new javax.xml.xpath.XPathNodes(){
            public java.util.Iterator<org.w3c.dom.Node> iterator() { return null; }
            public int size() { return 3; }
            public org.w3c.dom.Node get(int p0) { return null; }
        };
        check(a != null, "anon impl of XPathNodes");
        check(a.iterator() == null, "iterator() returns fixed default");
        check(a.size() == 3, "size() returns fixed default");
        check(a.get(3) == null, "get() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
