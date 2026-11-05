/*
 * @test
 * @summary javax.xml.xpath.XPathFunction conformance: white-box structure + black-box behavior.
 * @run main XPathFunctionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathFunctionTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathFunction");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "evaluate"), "method evaluate");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.xpath.XPathFunction a = new javax.xml.xpath.XPathFunction(){
            public java.lang.Object evaluate(java.util.List<?> p0) { return null; }
        };
        check(a != null, "anon impl of XPathFunction");
        check(a.evaluate((java.util.List<?>) null) == null, "evaluate() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInvocation();        System.out.println("PASS (" + passed + " assertions)");
    }


    // XPathFunction.evaluate(List) receives XPath 1.0-typed arguments and its
    // return value flows back into the expression (closed loop through the runtime).
    static void testInvocation() throws Exception {
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
            public String getNamespaceURI(String prefix) { return "f".equals(prefix) ? "urn:f" : javax.xml.XMLConstants.NULL_NS_URI; }
            public String getPrefix(String uri) { return "urn:f".equals(uri) ? "f" : null; }
            public java.util.Iterator<String> getPrefixes(String uri) { return java.util.Collections.singletonList("f").iterator(); }
        });
        final java.util.List<Object> seen = new java.util.ArrayList<Object>();
        xp.setXPathFunctionResolver((qn, arity) -> new javax.xml.xpath.XPathFunction() {
            public Object evaluate(java.util.List args) {
                seen.addAll(args);
                return Double.valueOf(9);
            }
        });
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root/>".getBytes("UTF-8")));
        Double r = (Double) xp.evaluate("f:g(1, 's', true())", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(r != null && r.doubleValue() == 9.0, "returned Double flows back as NUMBER result");
        check(seen.size() == 3, "evaluate receives all 3 arguments in order");
        check(seen.get(0) instanceof Double && ((Double) seen.get(0)).doubleValue() == 1.0,
              "numeric argument mapped to Double");
        check("s".equals(seen.get(1)), "string literal passed as String");
        check(Boolean.TRUE.equals(seen.get(2)), "boolean literal passed as Boolean");

        // Boolean and String returns map to BOOLEAN/STRING results
        xp.setXPathFunctionResolver((qn, arity) -> new javax.xml.xpath.XPathFunction() {
            public Object evaluate(java.util.List args) { return Boolean.TRUE; }
        });
        check(Boolean.TRUE.equals(xp.evaluate("f:g()", d, javax.xml.xpath.XPathConstants.BOOLEAN)),
              "Boolean return drives BOOLEAN result");
        xp.setXPathFunctionResolver((qn, arity) -> new javax.xml.xpath.XPathFunction() {
            public Object evaluate(java.util.List args) { return "strresult"; }
        });
        check("strresult".equals(xp.evaluate("f:g()", d, javax.xml.xpath.XPathConstants.STRING)),
              "String return drives STRING result");
    }
}

