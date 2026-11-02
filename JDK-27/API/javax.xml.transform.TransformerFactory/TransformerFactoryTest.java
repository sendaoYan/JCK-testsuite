/*
 * @test
 * @summary javax.xml.transform.TransformerFactory conformance: API type verification.
 * @run main TransformerFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class TransformerFactoryTest {
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
        Class<?> c = Class.forName("javax.xml.transform.TransformerFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "getAssociatedStylesheet"), "method getAssociatedStylesheet");
        check(hasMethod(c, "getAttribute"), "method getAttribute");
        check(hasMethod(c, "getErrorListener"), "method getErrorListener");
        check(hasMethod(c, "getFeature"), "method getFeature");
        check(hasMethod(c, "getURIResolver"), "method getURIResolver");
        check(hasMethod(c, "newDefaultInstance"), "method newDefaultInstance");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "newTemplates"), "method newTemplates");
        check(hasMethod(c, "newTransformer"), "method newTransformer");
        check(hasMethod(c, "setAttribute"), "method setAttribute");
        check(hasMethod(c, "setErrorListener"), "method setErrorListener");
        check(hasMethod(c, "setFeature"), "method setFeature");
        check(hasMethod(c, "setURIResolver"), "method setURIResolver");

        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer t = f.newTransformer();
        check(t != null, "newTransformer");
        java.io.StringWriter out = new java.io.StringWriter();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<a><b>c</b></a>")), new javax.xml.transform.stream.StreamResult(out));
        check(out.toString().contains("c"), "transform output");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
