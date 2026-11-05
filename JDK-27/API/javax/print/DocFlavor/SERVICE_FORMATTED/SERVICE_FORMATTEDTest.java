/*
 * @test
 * @summary javax.print.DocFlavor.SERVICE_FORMATTED conformance: white-box structure + black-box behavior.
 * @run main SERVICE_FORMATTEDTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class SERVICE_FORMATTEDTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.DocFlavor$SERVICE_FORMATTED");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(DocFlavor.class.isAssignableFrom(c), "extends DocFlavor");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE != null, "RENDERABLE_IMAGE non-null");
        check("java.awt.image.renderable.RenderableImage".equals(DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE.getRepresentationClassName()), "RENDERABLE_IMAGE repr class");
        check(DocFlavor.SERVICE_FORMATTED.PRINTABLE != null, "PRINTABLE non-null");
        check("java.awt.print.Printable".equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE.getRepresentationClassName()), "PRINTABLE repr class");
        check(DocFlavor.SERVICE_FORMATTED.PAGEABLE != null, "PAGEABLE non-null");
        check("java.awt.print.Pageable".equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE.getRepresentationClassName()), "PAGEABLE repr class");
        check("application/x-java-jvm-local-objectref".equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE.getMimeType()), "PAGEABLE mime exact");
        check(DocFlavor.SERVICE_FORMATTED.PAGEABLE.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) && DocFlavor.SERVICE_FORMATTED.PAGEABLE.hashCode() == DocFlavor.SERVICE_FORMATTED.PAGEABLE.hashCode(), "PAGEABLE equals/hash");
        check(!DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE), "distinct constants unequal");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full predefined constant table / rep-class family / mime spot checks ----
    static void testContractDeep() throws Exception {
        check(javax.print.DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE.getRepresentationClassName().equals("java.awt.image.renderable.RenderableImage"), "rep");
        check(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE.getRepresentationClassName().equals("java.awt.print.Printable"), "rep");
        check(javax.print.DocFlavor.SERVICE_FORMATTED.PAGEABLE.getRepresentationClassName().equals("java.awt.print.Pageable"), "rep");
        check(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE.getMimeType().equals("application/x-java-jvm-local-objectref"), "local objectref mime");
        javax.print.DocFlavor p = new javax.print.DocFlavor("application/x-java-jvm-local-objectref", "java.awt.print.Printable");
        check(p.equals(javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE), "constructed equals predefined");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
