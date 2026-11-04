/*
 * @test
 * @summary org.xml.sax.DTDHandler conformance: white-box structure + black-box behavior.
 * @run main DTDHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DTDHandlerTest {
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

        Class<?> c = Class.forName("org.xml.sax.DTDHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "notationDecl"), "method notationDecl");
        check(hasMethod(c, "unparsedEntityDecl"), "method unparsedEntityDecl");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.DTDHandler a = new org.xml.sax.DTDHandler(){
            public void notationDecl(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void unparsedEntityDecl(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3) { }
        };
        check(a != null, "anon impl of DTDHandler");
        a.notationDecl("s", "s", "s");
        a.unparsedEntityDecl("s", "s", "s", "s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
