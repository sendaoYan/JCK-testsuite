/*
 * @test
 * @summary javax.xml.crypto.NodeSetData conformance: white-box structure + black-box behavior.
 * @run main NodeSetDataTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class NodeSetDataTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.NodeSetData");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.Data"), "interface javax.xml.crypto.Data");
        check(hasInterface(c, "java.lang.Iterable"), "interface java.lang.Iterable");
        check(hasMethod(c, "iterator"), "method iterator");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.crypto.NodeSetData<java.lang.String> a = new javax.xml.crypto.NodeSetData<java.lang.String>() {
            public java.util.Iterator<java.lang.String> iterator() {
                return java.util.Arrays.asList("s").iterator();
            }
        };
        check(a != null, "anon impl of NodeSetData");
        java.util.Iterator<java.lang.String> it = a.iterator();
        check(it != null && it.hasNext(), "iterator hasNext");
        check("s".equals(it.next()) && !it.hasNext(), "iterator next exhausts");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
