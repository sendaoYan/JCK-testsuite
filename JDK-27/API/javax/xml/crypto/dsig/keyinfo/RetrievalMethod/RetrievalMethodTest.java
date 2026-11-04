/*
 * @test
 * @summary javax.xml.crypto.dsig.keyinfo.RetrievalMethod conformance: white-box structure + black-box behavior.
 * @run main RetrievalMethodTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class RetrievalMethodTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.keyinfo.RetrievalMethod");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.URIReference"), "interface javax.xml.crypto.URIReference");
        check(hasInterface(c, "javax.xml.crypto.XMLStructure"), "interface javax.xml.crypto.XMLStructure");
        check(hasMethod(c, "dereference"), "method dereference");
        check(hasMethod(c, "getTransforms"), "method getTransforms");
        check(hasMethod(c, "getURI"), "method getURI");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.crypto.dsig.keyinfo.RetrievalMethod a = new javax.xml.crypto.dsig.keyinfo.RetrievalMethod(){
            public java.util.List<javax.xml.crypto.dsig.Transform> getTransforms() { return null; }
            public java.lang.String getURI() { return "s"; }
            public javax.xml.crypto.Data dereference(javax.xml.crypto.XMLCryptoContext p0) { return null; }
            public java.lang.String getType() { return "s"; }
            public boolean isFeatureSupported(java.lang.String p0) { return true; }
        };
        check(a != null, "anon impl of RetrievalMethod");
        check(a.getTransforms() == null, "getTransforms() returns fixed default");
        check("s".equals(a.getURI()), "getURI() returns fixed default");
        check(a.dereference((javax.xml.crypto.XMLCryptoContext) null) == null, "dereference() returns fixed default");
        check("s".equals(a.getType()), "getType() returns fixed default");
        check(a.isFeatureSupported("s") == true, "isFeatureSupported() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
