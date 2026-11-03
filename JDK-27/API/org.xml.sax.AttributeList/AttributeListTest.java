/*
 * @test
 * @summary org.xml.sax.AttributeList conformance: white-box structure + black-box behavior.
 * @run main AttributeListTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class AttributeListTest {
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

        Class<?> c = Class.forName("org.xml.sax.AttributeList");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getValue"), "method getValue");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.AttributeList a = new org.xml.sax.AttributeList(){
            public int getLength() { return 3; }
            public java.lang.String getName(int p0) { return "s"; }
            public java.lang.String getType(int p0) { return "s"; }
            public java.lang.String getValue(int p0) { return "s"; }
            public java.lang.String getType(java.lang.String p0) { return "s"; }
            public java.lang.String getValue(java.lang.String p0) { return "s"; }
        };
        check(a != null, "anon impl of AttributeList");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check("s".equals(a.getName(3)), "getName() returns fixed default");
        check("s".equals(a.getType(3)), "getType() returns fixed default");
        check("s".equals(a.getValue(3)), "getValue() returns fixed default");
        check("s".equals(a.getType("s")), "getType() returns fixed default");
        check("s".equals(a.getValue("s")), "getValue() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
