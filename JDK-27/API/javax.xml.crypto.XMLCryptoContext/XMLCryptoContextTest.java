/*
 * @test
 * @summary javax.xml.crypto.XMLCryptoContext conformance: API type verification.
 * @run main XMLCryptoContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLCryptoContextTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.XMLCryptoContext");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "getBaseURI"), "method getBaseURI");
        check(hasMethod(c, "getDefaultNamespacePrefix"), "method getDefaultNamespacePrefix");
        check(hasMethod(c, "getKeySelector"), "method getKeySelector");
        check(hasMethod(c, "getNamespacePrefix"), "method getNamespacePrefix");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getURIDereferencer"), "method getURIDereferencer");
        check(hasMethod(c, "put"), "method put");
        check(hasMethod(c, "putNamespacePrefix"), "method putNamespacePrefix");
        check(hasMethod(c, "setBaseURI"), "method setBaseURI");
        check(hasMethod(c, "setDefaultNamespacePrefix"), "method setDefaultNamespacePrefix");
        check(hasMethod(c, "setKeySelector"), "method setKeySelector");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "setURIDereferencer"), "method setURIDereferencer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
