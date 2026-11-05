/*
 * @test
 * @summary javax.xml.xpath.XPathFunctionException conformance: white-box structure + black-box behavior.
 * @run main XPathFunctionExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathFunctionExceptionTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathFunctionException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass().getName().equals("javax.xml.xpath.XPathExpressionException"), "superclass javax.xml.xpath.XPathExpressionException");

        javax.xml.xpath.XPathFunctionException e = new javax.xml.xpath.XPathFunctionException("m");
        check("m".equals(e.getMessage()), "msg");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.xpath.XPathFunctionException e = new javax.xml.xpath.XPathFunctionException("fn");
        check("fn".equals(e.getMessage()) && e instanceof javax.xml.xpath.XPathExpressionException, "subclass chain");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructors();
        testRealTrigger();        System.out.println("PASS (" + passed + " assertions)");
    }


    static void testConstructors() {
        // (String)
        javax.xml.xpath.XPathFunctionException e1 = new javax.xml.xpath.XPathFunctionException("fn failed");
        check("fn failed".equals(e1.getMessage()), "(String) message round-trip");
        check(e1.getCause() == null, "(String) cause null");
        // (Throwable)
        RuntimeException cause = new RuntimeException("fn root");
        javax.xml.xpath.XPathFunctionException e2 = new javax.xml.xpath.XPathFunctionException(cause);
        check(e2.getCause() == cause, "(Throwable) cause round-trip");
        check(e2.getMessage() != null && e2.getMessage().contains("fn root"), "(Throwable) message derived");
        // hierarchy: nested under expression failure
        check(e2 instanceof javax.xml.xpath.XPathExpressionException, "is-a XPathExpressionException");
        check(javax.xml.xpath.XPathFunctionException.class.getConstructors().length == 2,
              "exactly two public constructors");
    }

    // real trigger: exception thrown inside an XPathFunction propagates unchanged
    static void testRealTrigger() throws Exception {
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
            public String getNamespaceURI(String prefix) { return "f".equals(prefix) ? "urn:f" : javax.xml.XMLConstants.NULL_NS_URI; }
            public String getPrefix(String uri) { return "urn:f".equals(uri) ? "f" : null; }
            public java.util.Iterator<String> getPrefixes(String uri) { return java.util.Collections.singletonList("f").iterator(); }
        });
        xp.setXPathFunctionResolver((qn, arity) -> new javax.xml.xpath.XPathFunction() {
            public Object evaluate(java.util.List args) throws javax.xml.xpath.XPathFunctionException {
                throw new javax.xml.xpath.XPathFunctionException("boom");
            }
        });
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root/>".getBytes("UTF-8")));
        boolean saw = false;
        try { xp.evaluate("f:g(1)", d, javax.xml.xpath.XPathConstants.NUMBER); }
        catch (javax.xml.xpath.XPathFunctionException e) { saw = "boom".equals(e.getMessage()); }
        check(saw, "XPathFunctionException thrown inside function propagates unchanged");
    }
}

