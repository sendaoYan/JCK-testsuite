/*
 * @test
 * @summary javax.xml.crypto.dsig.XMLSignContext conformance: white-box structure + black-box behavior.
 * @run main XMLSignContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLSignContextTest {
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

        Class<?> c = Class.forName("javax.xml.crypto.dsig.XMLSignContext");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.crypto.XMLCryptoContext"), "interface javax.xml.crypto.XMLCryptoContext");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.crypto.dsig.XMLSignContext a = new javax.xml.crypto.dsig.XMLSignContext(){
            public java.lang.String getBaseURI() { return "s"; }
            public void setBaseURI(java.lang.String p0) { }
            public javax.xml.crypto.KeySelector getKeySelector() { return null; }
            public void setKeySelector(javax.xml.crypto.KeySelector p0) { }
            public javax.xml.crypto.URIDereferencer getURIDereferencer() { return null; }
            public void setURIDereferencer(javax.xml.crypto.URIDereferencer p0) { }
            public java.lang.String getNamespacePrefix(java.lang.String p0, java.lang.String p1) { return "s"; }
            public java.lang.String putNamespacePrefix(java.lang.String p0, java.lang.String p1) { return "s"; }
            public java.lang.String getDefaultNamespacePrefix() { return "s"; }
            public void setDefaultNamespacePrefix(java.lang.String p0) { }
            public java.lang.Object setProperty(java.lang.String p0, java.lang.Object p1) { return null; }
            public java.lang.Object getProperty(java.lang.String p0) { return null; }
            public java.lang.Object get(java.lang.Object p0) { return null; }
            public java.lang.Object put(java.lang.Object p0, java.lang.Object p1) { return null; }
        };
        check(a != null, "anon impl of XMLSignContext");
        check("s".equals(a.getBaseURI()), "getBaseURI() returns fixed default");
        a.setBaseURI("s");
        check(a.getKeySelector() == null, "getKeySelector() returns fixed default");
        a.setKeySelector((javax.xml.crypto.KeySelector) null);
        check(a.getURIDereferencer() == null, "getURIDereferencer() returns fixed default");
        a.setURIDereferencer((javax.xml.crypto.URIDereferencer) null);
        check("s".equals(a.getNamespacePrefix("s", "s")), "getNamespacePrefix() returns fixed default");
        check("s".equals(a.putNamespacePrefix("s", "s")), "putNamespacePrefix() returns fixed default");
        check("s".equals(a.getDefaultNamespacePrefix()), "getDefaultNamespacePrefix() returns fixed default");
        a.setDefaultNamespacePrefix("s");
        check(a.setProperty("s", (java.lang.Object) null) == null, "setProperty() returns fixed default");
        check(a.getProperty("s") == null, "getProperty() returns fixed default");
        check(a.get((java.lang.Object) null) == null, "get() returns fixed default");
        check(a.put((java.lang.Object) null, (java.lang.Object) null) == null, "put() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
