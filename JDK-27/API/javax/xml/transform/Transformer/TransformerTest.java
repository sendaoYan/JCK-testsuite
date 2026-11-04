/*
 * @test
 * @summary javax.xml.transform.Transformer conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.transform.TransformerFactory f = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer t = f.newTransformer();
        t.setParameter("p", "v");
        check("v".equals(t.getParameter("p")), "parameter round-trip");
        t.clearParameters();
        check(t.getParameter("p") == null, "clearParameters");
        java.util.Properties props = t.getOutputProperties();
        check(props != null, "output properties non-null");
        t.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
        check("xml".equals(t.getOutputProperty(javax.xml.transform.OutputKeys.METHOD)), "set/getOutputProperty");
        check("xml".equals(t.getOutputProperties().getProperty(javax.xml.transform.OutputKeys.METHOD)), "output properties reflect set method");
        boolean ok = false;
        try { t.setOutputProperty("http://example.invalid/unknownProp", "v"); }
        catch (IllegalArgumentException e) { ok = true; }
        check(ok, "setOutputProperty unknown name -> IllegalArgumentException");
        javax.xml.transform.ErrorListener el = new javax.xml.transform.ErrorListener() {
            public void warning(javax.xml.transform.TransformerException e) {}
            public void error(javax.xml.transform.TransformerException e) {}
            public void fatalError(javax.xml.transform.TransformerException e) {}
        };
        t.setErrorListener(el); check(t.getErrorListener() == el, "error listener round-trip");
        javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = db.parse(new java.io.ByteArrayInputStream("<a><b/></a>".getBytes("UTF-8")));
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(new javax.xml.transform.dom.DOMSource(d), new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().contains("<a><b"), "transform DOM -> stream");
        t.reset();
        check(t.getParameter("p") == null, "reset clears parameters");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
