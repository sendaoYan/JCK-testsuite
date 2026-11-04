/*
 * @test
 * @summary javax.xml.crypto.dom.DOMCryptoContext conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.security.KeyPair kp = java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair();
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        org.w3c.dom.Element el = doc.createElement("a");
        javax.xml.crypto.dom.DOMCryptoContext ctx = new javax.xml.crypto.dsig.dom.DOMSignContext(kp.getPrivate(), el);
        check(ctx.getBaseURI() == null, "baseURI default null");
        ctx.setBaseURI("urn:b");
        check("urn:b".equals(ctx.getBaseURI()), "setBaseURI/getBaseURI");
        check(ctx.getDefaultNamespacePrefix() == null, "default ns prefix default null");
        ctx.setDefaultNamespacePrefix("d");
        check("d".equals(ctx.getDefaultNamespacePrefix()), "default ns prefix");
        check(ctx.putNamespacePrefix("p", "urn:p") == null, "putNamespacePrefix first null");
        check("urn:p".equals(ctx.getNamespacePrefix("p", "fallback")), "getNamespacePrefix");
        check("fallback".equals(ctx.getNamespacePrefix("unknown", "fallback")), "getNamespacePrefix fallback");
        check(ctx.getProperty("k") == null, "property default null");
        check(ctx.setProperty("k", "v") == null, "setProperty returns old");
        check("v".equals(ctx.getProperty("k")), "property get");
        check(ctx.get("gk") == null && ctx.put("gk", "gv") == null, "map put/get");
        check("gv".equals(ctx.get("gk")), "map get");
        el.setAttributeNS("urn:u", "local", "idval");
        ctx.setIdAttributeNS(el, "urn:u", "local");
        check(ctx.getElementById("idval") == el, "getElementById");
        check(ctx.getElementById("other") == null, "getElementById unknown null");
        java.util.Iterator<java.util.Map.Entry<java.lang.String, org.w3c.dom.Element>> it = ctx.iterator();
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        check(n == 1, "iterator count of id attributes");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
