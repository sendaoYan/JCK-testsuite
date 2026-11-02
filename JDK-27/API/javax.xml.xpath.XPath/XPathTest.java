/*
 * @test
 * @summary javax.xml.xpath.XPath conformance: API type verification.
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


    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
