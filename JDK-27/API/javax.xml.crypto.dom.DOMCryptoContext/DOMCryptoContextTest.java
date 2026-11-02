/*
 * @test
 * @summary javax.xml.crypto.dom.DOMCryptoContext conformance: API type verification.
 * @run main DOMCryptoContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class DOMCryptoContextTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.dom.DOMCryptoContext");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.crypto.XMLCryptoContext"), "interface javax.xml.crypto.XMLCryptoContext");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "getBaseURI"), "method getBaseURI");
        check(hasMethod(c, "getDefaultNamespacePrefix"), "method getDefaultNamespacePrefix");
        check(hasMethod(c, "getElementById"), "method getElementById");
        check(hasMethod(c, "getKeySelector"), "method getKeySelector");
        check(hasMethod(c, "getNamespacePrefix"), "method getNamespacePrefix");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getURIDereferencer"), "method getURIDereferencer");
        check(hasMethod(c, "iterator"), "method iterator");
        check(hasMethod(c, "put"), "method put");
        check(hasMethod(c, "putNamespacePrefix"), "method putNamespacePrefix");
        check(hasMethod(c, "setBaseURI"), "method setBaseURI");
        check(hasMethod(c, "setDefaultNamespacePrefix"), "method setDefaultNamespacePrefix");
        check(hasMethod(c, "setIdAttributeNS"), "method setIdAttributeNS");
        check(hasMethod(c, "setKeySelector"), "method setKeySelector");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "setURIDereferencer"), "method setURIDereferencer");

        javax.xml.crypto.dom.DOMCryptoContext ctx = new javax.xml.crypto.dsig.dom.DOMSignContext(java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair().getPrivate(),
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("a"));
        ctx.setBaseURI("urn:base");
        check("urn:base".equals(ctx.getBaseURI()), "setBaseURI/getBaseURI");
        ctx.setDefaultNamespacePrefix("d");
        check("d".equals(ctx.getDefaultNamespacePrefix()), "defaultNamespacePrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
