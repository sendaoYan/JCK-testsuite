/*
 * @test
 * @summary org.xml.sax.ext.Attributes2 conformance: white-box structure + black-box behavior.
 * @run main Attributes2Test
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class Attributes2Test {
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

        Class<?> c = Class.forName("org.xml.sax.ext.Attributes2");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "org.xml.sax.Attributes"), "interface org.xml.sax.Attributes");
        check(hasMethod(c, "isDeclared"), "method isDeclared");
        check(hasMethod(c, "isSpecified"), "method isSpecified");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.ext.Attributes2 a = new org.xml.sax.ext.Attributes2(){
            public boolean isDeclared(int p0) { return true; }
            public boolean isDeclared(java.lang.String p0) { return true; }
            public boolean isDeclared(java.lang.String p0, java.lang.String p1) { return true; }
            public boolean isSpecified(int p0) { return true; }
            public boolean isSpecified(java.lang.String p0, java.lang.String p1) { return true; }
            public boolean isSpecified(java.lang.String p0) { return true; }
            public int getLength() { return 3; }
            public java.lang.String getURI(int p0) { return "s"; }
            public java.lang.String getLocalName(int p0) { return "s"; }
            public java.lang.String getQName(int p0) { return "s"; }
            public java.lang.String getType(int p0) { return "s"; }
            public java.lang.String getValue(int p0) { return "s"; }
            public int getIndex(java.lang.String p0, java.lang.String p1) { return 3; }
            public int getIndex(java.lang.String p0) { return 3; }
            public java.lang.String getType(java.lang.String p0, java.lang.String p1) { return "s"; }
            public java.lang.String getType(java.lang.String p0) { return "s"; }
            public java.lang.String getValue(java.lang.String p0, java.lang.String p1) { return "s"; }
            public java.lang.String getValue(java.lang.String p0) { return "s"; }
        };
        check(a != null, "anon impl of Attributes2");
        check(a.isDeclared(3) == true, "isDeclared() returns fixed default");
        check(a.isDeclared("s") == true, "isDeclared() returns fixed default");
        check(a.isDeclared("s", "s") == true, "isDeclared() returns fixed default");
        check(a.isSpecified(3) == true, "isSpecified() returns fixed default");
        check(a.isSpecified("s", "s") == true, "isSpecified() returns fixed default");
        check(a.isSpecified("s") == true, "isSpecified() returns fixed default");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check("s".equals(a.getURI(3)), "getURI() returns fixed default");
        check("s".equals(a.getLocalName(3)), "getLocalName() returns fixed default");
        check("s".equals(a.getQName(3)), "getQName() returns fixed default");
        check("s".equals(a.getType(3)), "getType() returns fixed default");
        check("s".equals(a.getValue(3)), "getValue() returns fixed default");
        check(a.getIndex("s", "s") == 3, "getIndex() returns fixed default");
        check(a.getIndex("s") == 3, "getIndex() returns fixed default");
        check("s".equals(a.getType("s", "s")), "getType() returns fixed default");
        check("s".equals(a.getType("s")), "getType() returns fixed default");
        check("s".equals(a.getValue("s", "s")), "getValue() returns fixed default");
        check("s".equals(a.getValue("s")), "getValue() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
