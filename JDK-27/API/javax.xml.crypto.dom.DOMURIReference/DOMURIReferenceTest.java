/*
 * @test
 * @summary javax.xml.crypto.dom.DOMURIReference conformance: white-box structure + black-box behavior.
 * @run main DOMURIReferenceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMURIReferenceTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dom.DOMURIReference");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.URIReference"), "interface javax.xml.crypto.URIReference");
        check(hasMethod(c, "getHere"), "method getHere");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.crypto.dom.DOMURIReference a = new javax.xml.crypto.dom.DOMURIReference(){
            public org.w3c.dom.Node getHere() { return null; }
            public java.lang.String getURI() { return "s"; }
            public java.lang.String getType() { return "s"; }
        };
        check(a != null, "anon impl of DOMURIReference");
        check(a.getHere() == null, "getHere() returns fixed default");
        check("s".equals(a.getURI()), "getURI() returns fixed default");
        check("s".equals(a.getType()), "getType() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
