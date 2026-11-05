/*
 * @test
 * @summary javax.xml.xpath.XPathVariableResolver conformance: white-box structure + black-box behavior.
 * @run main XPathVariableResolverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathVariableResolverTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathVariableResolver");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "resolveVariable"), "method resolveVariable");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.xpath.XPathVariableResolver a = new javax.xml.xpath.XPathVariableResolver(){
            public java.lang.Object resolveVariable(javax.xml.namespace.QName p0) { return null; }
        };
        check(a != null, "anon impl of XPathVariableResolver");
        check(a.resolveVariable((javax.xml.namespace.QName) null) == null, "resolveVariable() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testResolution();        System.out.println("PASS (" + passed + " assertions)");
    }


    static org.w3c.dom.Document emptyDoc() throws Exception {
        return javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root/>".getBytes("UTF-8")));
    }

    static void testResolution() throws Exception {
        org.w3c.dom.Document d = emptyDoc();
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();

        // resolver receives the variable QName and its value participates in the expression
        final javax.xml.namespace.QName[] asked = new javax.xml.namespace.QName[1];
        final int[] calls = {0};
        xp.setXPathVariableResolver(new javax.xml.xpath.XPathVariableResolver() {
            public Object resolveVariable(javax.xml.namespace.QName v) {
                calls[0]++;
                asked[0] = v;
                return Integer.valueOf(42);
            }
        });
        Double r = (Double) xp.evaluate("$x+1", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(r != null && r.doubleValue() == 43.0, "$x=42 -> $x+1 == 43.0 (closed loop)");
        check(calls[0] == 1, "resolveVariable called exactly once");
        check(asked[0] != null && "x".equals(asked[0].getLocalPart())
              && asked[0].getNamespaceURI().length() == 0,
              "unprefixed variable QName has empty namespace");

        // different value types flow through: Double and String
        xp.setXPathVariableResolver(v -> Double.valueOf(2.5));
        Double r2 = (Double) xp.evaluate("$x*2", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(r2 != null && r2.doubleValue() == 5.0, "Double variable value: $x*2 == 5.0");
        xp.setXPathVariableResolver(v -> "hello");
        String r3 = (String) xp.evaluate("$x", d, javax.xml.xpath.XPathConstants.STRING);
        check("hello".equals(r3), "String variable value readable as STRING");

        // resolver returning null -> XPathExpressionException
        xp.setXPathVariableResolver(v -> null);
        boolean sawNull = false;
        try { xp.evaluate("$x", d, javax.xml.xpath.XPathConstants.NUMBER); }
        catch (javax.xml.xpath.XPathExpressionException e) { sawNull = true; }
        check(sawNull, "resolver returning null -> XPathExpressionException");

        // resolver consulted only for referenced variables
        final int[] calls2 = {0};
        xp.setXPathVariableResolver(v -> { calls2[0]++; return Integer.valueOf(1); });
        xp.evaluate("1+1", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(calls2[0] == 0, "resolver not consulted when expression has no variables");
        xp.evaluate("$a + $b", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(calls2[0] == 2, "resolver consulted once per referenced variable ($a + $b)");
    }
}

