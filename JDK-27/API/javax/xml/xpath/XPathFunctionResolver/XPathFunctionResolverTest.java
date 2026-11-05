/*
 * @test
 * @summary javax.xml.xpath.XPathFunctionResolver conformance: white-box structure + black-box behavior.
 * @run main XPathFunctionResolverTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathFunctionResolverTest {
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

        Class<?> c = Class.forName("javax.xml.xpath.XPathFunctionResolver");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "resolveFunction"), "method resolveFunction");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.xpath.XPathFunctionResolver a = new javax.xml.xpath.XPathFunctionResolver(){
            public javax.xml.xpath.XPathFunction resolveFunction(javax.xml.namespace.QName p0, int p1) { return null; }
        };
        check(a != null, "anon impl of XPathFunctionResolver");
        check(a.resolveFunction((javax.xml.namespace.QName) null, 3) == null, "resolveFunction() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testResolution();        System.out.println("PASS (" + passed + " assertions)");
    }


    static javax.xml.namespace.NamespaceContext NS_F = new javax.xml.namespace.NamespaceContext() {
        public String getNamespaceURI(String prefix) { return "f".equals(prefix) ? "urn:f" : javax.xml.XMLConstants.NULL_NS_URI; }
        public String getPrefix(String uri) { return "urn:f".equals(uri) ? "f" : null; }
        public java.util.Iterator<String> getPrefixes(String uri) { return java.util.Collections.singletonList("f").iterator(); }
    };

    static org.w3c.dom.Document emptyDoc() throws Exception {
        return javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root/>".getBytes("UTF-8")));
    }

    static void testResolution() throws Exception {
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(NS_F);
        final javax.xml.namespace.QName[] asked = new javax.xml.namespace.QName[1];
        final int[] arity = new int[1];
        xp.setXPathFunctionResolver(new javax.xml.xpath.XPathFunctionResolver() {
            public javax.xml.xpath.XPathFunction resolveFunction(javax.xml.namespace.QName fn, int a) {
                asked[0] = fn;
                arity[0] = a;
                return new javax.xml.xpath.XPathFunction() {
                    public Object evaluate(java.util.List args) { return Double.valueOf(5); }
                };
            }
        });
        org.w3c.dom.Document d = emptyDoc();
        Double r = (Double) xp.evaluate("f:myfn(1)", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(r != null && r.doubleValue() == 5.0, "resolved function executes (closed loop)");
        check(asked[0] != null && "urn:f".equals(asked[0].getNamespaceURI())
              && "myfn".equals(asked[0].getLocalPart()),
              "resolver receives prefixed QName {urn:f}myfn");
        check(arity[0] == 1, "resolver receives arity");

        // arity is part of the resolution: distinct arities seen
        xp.evaluate("f:myfn(1,2,3)", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(arity[0] == 3, "resolver receives arity 3 for 3-arg call");

        // resolver returning null -> evaluation fails
        xp.setXPathFunctionResolver((qn, a) -> null);
        boolean saw = false;
        try { xp.evaluate("f:any(1)", d, javax.xml.xpath.XPathConstants.NUMBER); }
        catch (javax.xml.xpath.XPathExpressionException e) { saw = true; }
        check(saw, "resolver returning null -> XPathExpressionException");

        // resolver not consulted for built-in functions
        final boolean[] consulted = {false};
        xp.setXPathFunctionResolver((qn, a) -> { consulted[0] = true; return null; });
        Double builtIn = (Double) xp.evaluate("count(//*)", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(builtIn != null && builtIn.doubleValue() == 1.0 && !consulted[0],
              "built-in count() never consults the resolver");
    }
}

