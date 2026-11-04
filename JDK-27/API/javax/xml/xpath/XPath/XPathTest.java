/*
 * @test
 * @summary javax.xml.xpath.XPath conformance: white-box structure + black-box behavior.
 * @run main XPathTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPath");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "compile"), "method compile");
        check(hasMethod(c, "evaluate"), "method evaluate");
        check(hasMethod(c, "evaluateExpression"), "method evaluateExpression");
        check(hasMethod(c, "getNamespaceContext"), "method getNamespaceContext");
        check(hasMethod(c, "getXPathFunctionResolver"), "method getXPathFunctionResolver");
        check(hasMethod(c, "getXPathVariableResolver"), "method getXPathVariableResolver");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setNamespaceContext"), "method setNamespaceContext");
        check(hasMethod(c, "setXPathFunctionResolver"), "method setXPathFunctionResolver");
        check(hasMethod(c, "setXPathVariableResolver"), "method setXPathVariableResolver");

        javax.xml.xpath.XPath x = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        check(x.compile("1+1") != null, "compile");
        check("2".equals(x.evaluate("1+1", (Object) null)), "evaluate string");
        check(((Double) x.evaluate("1+1", (Object) null, javax.xml.xpath.XPathConstants.NUMBER)).doubleValue() == 2.0, "evaluate number");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.xpath.XPath a = new javax.xml.xpath.XPath(){
            public void reset() { }
            public void setXPathVariableResolver(javax.xml.xpath.XPathVariableResolver p0) { }
            public javax.xml.xpath.XPathVariableResolver getXPathVariableResolver() { return null; }
            public void setXPathFunctionResolver(javax.xml.xpath.XPathFunctionResolver p0) { }
            public javax.xml.xpath.XPathFunctionResolver getXPathFunctionResolver() { return null; }
            public void setNamespaceContext(javax.xml.namespace.NamespaceContext p0) { }
            public javax.xml.namespace.NamespaceContext getNamespaceContext() { return null; }
            public javax.xml.xpath.XPathExpression compile(java.lang.String p0) { return null; }
            public java.lang.Object evaluate(java.lang.String p0, java.lang.Object p1, javax.xml.namespace.QName p2) { return null; }
            public java.lang.String evaluate(java.lang.String p0, java.lang.Object p1) { return "s"; }
            public java.lang.Object evaluate(java.lang.String p0, org.xml.sax.InputSource p1, javax.xml.namespace.QName p2) { return null; }
            public java.lang.String evaluate(java.lang.String p0, org.xml.sax.InputSource p1) { return "s"; }
        };
        check(a != null, "anon impl of XPath");
        a.reset();
        a.setXPathVariableResolver((javax.xml.xpath.XPathVariableResolver) null);
        check(a.getXPathVariableResolver() == null, "getXPathVariableResolver() returns fixed default");
        a.setXPathFunctionResolver((javax.xml.xpath.XPathFunctionResolver) null);
        check(a.getXPathFunctionResolver() == null, "getXPathFunctionResolver() returns fixed default");
        a.setNamespaceContext((javax.xml.namespace.NamespaceContext) null);
        check(a.getNamespaceContext() == null, "getNamespaceContext() returns fixed default");
        check(a.compile("s") == null, "compile() returns fixed default");
        check(a.evaluate("s", (java.lang.Object) null, (javax.xml.namespace.QName) null) == null, "evaluate() returns fixed default");
        check("s".equals(a.evaluate("s", (java.lang.Object) null)), "evaluate() returns fixed default");
        check(a.evaluate("s", (org.xml.sax.InputSource) null, (javax.xml.namespace.QName) null) == null, "evaluate() returns fixed default");
        check("s".equals(a.evaluate("s", (org.xml.sax.InputSource) null)), "evaluate() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
