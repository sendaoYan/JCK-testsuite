/*
 * @test
 * @summary javax.xml.catalog.CatalogResolver conformance: API type verification.
 * @run main CatalogResolverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CatalogResolverTest {
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


    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.catalog.CatalogResolver");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "org.xml.sax.EntityResolver"), "interface org.xml.sax.EntityResolver");
        check(hasInterface(c, "javax.xml.stream.XMLResolver"), "interface javax.xml.stream.XMLResolver");
        check(hasInterface(c, "javax.xml.transform.URIResolver"), "interface javax.xml.transform.URIResolver");
        check(hasInterface(c, "org.w3c.dom.ls.LSResourceResolver"), "interface org.w3c.dom.ls.LSResourceResolver");
        check(hasMethod(c, "resolve"), "method resolve");
        check(hasMethod(c, "resolveEntity"), "method resolveEntity");
        check(hasMethod(c, "resolveResource"), "method resolveResource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
