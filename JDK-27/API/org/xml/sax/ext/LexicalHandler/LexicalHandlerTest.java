/*
 * @test
 * @summary org.xml.sax.ext.LexicalHandler conformance: white-box structure + black-box behavior.
 * @run main LexicalHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class LexicalHandlerTest {
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

        Class<?> c = Class.forName("org.xml.sax.ext.LexicalHandler");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "comment"), "method comment");
        check(hasMethod(c, "endCDATA"), "method endCDATA");
        check(hasMethod(c, "endDTD"), "method endDTD");
        check(hasMethod(c, "endEntity"), "method endEntity");
        check(hasMethod(c, "startCDATA"), "method startCDATA");
        check(hasMethod(c, "startDTD"), "method startDTD");
        check(hasMethod(c, "startEntity"), "method startEntity");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.xml.sax.ext.LexicalHandler a = new org.xml.sax.ext.LexicalHandler(){
            public void startDTD(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void endDTD() { }
            public void startEntity(java.lang.String p0) { }
            public void endEntity(java.lang.String p0) { }
            public void startCDATA() { }
            public void endCDATA() { }
            public void comment(char[] p0, int p1, int p2) { }
        };
        check(a != null, "anon impl of LexicalHandler");
        a.startDTD("s", "s", "s");
        a.endDTD();
        a.startEntity("s");
        a.endEntity("s");
        a.startCDATA();
        a.endCDATA();
        a.comment((char[]) null, 3, 3);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
