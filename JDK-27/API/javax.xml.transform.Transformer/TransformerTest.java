/*
 * @test
 * @summary javax.xml.transform.Transformer conformance: API type verification.
 * @run main TransformerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformerTest {
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
        Class<?> c = Class.forName("javax.xml.transform.Transformer");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "clearParameters"), "method clearParameters");
        check(hasMethod(c, "getErrorListener"), "method getErrorListener");
        check(hasMethod(c, "getOutputProperties"), "method getOutputProperties");
        check(hasMethod(c, "getOutputProperty"), "method getOutputProperty");
        check(hasMethod(c, "getParameter"), "method getParameter");
        check(hasMethod(c, "getURIResolver"), "method getURIResolver");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "setErrorListener"), "method setErrorListener");
        check(hasMethod(c, "setOutputProperties"), "method setOutputProperties");
        check(hasMethod(c, "setOutputProperty"), "method setOutputProperty");
        check(hasMethod(c, "setParameter"), "method setParameter");
        check(hasMethod(c, "setURIResolver"), "method setURIResolver");
        check(hasMethod(c, "transform"), "method transform");

        javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
        check("xml".equals(t.getOutputProperty(javax.xml.transform.OutputKeys.METHOD)), "output property");
        java.io.StringWriter out = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<a/>")), new javax.xml.transform.stream.StreamResult(out));
        check(out.toString().length() > 0, "transform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
