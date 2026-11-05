/*
 * @test
 * @summary javax.print.attribute.SupportedValuesAttribute conformance: white-box structure + black-box behavior.
 * @run main SupportedValuesAttributeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class SupportedValuesAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.SupportedValuesAttribute");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(Attribute.class.isAssignableFrom(c), "extends Attribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SupportedValuesAttribute a = new CopiesSupported(1, 5);
        check("copies-supported".equals(a.getName()), "SupportedValuesAttribute marker");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: SupportedValuesAttribute membership ----
    static void testContractDeep() throws Exception {
        javax.print.attribute.SupportedValuesAttribute v = new javax.print.attribute.standard.CopiesSupported(1, 99);
        check(v instanceof javax.print.attribute.Attribute, "extends Attribute");
        check(javax.print.attribute.standard.CopiesSupported.class == v.getCategory(), "getCategory");
        check("copies-supported".equals(v.getName()), "getName");
        javax.print.attribute.SupportedValuesAttribute[] others = {
            new javax.print.attribute.standard.NumberUpSupported(1, 4),
            new javax.print.attribute.standard.JobImpressionsSupported(0, 1000),
            new javax.print.attribute.standard.JobKOctetsSupported(0, 1000),
            new javax.print.attribute.standard.JobMediaSheetsSupported(0, 100) };
        String[] names = { "number-up-supported", "job-impressions-supported", "job-k-octets-supported", "job-media-sheets-supported" };
        for (int i = 0; i < others.length; i++) check(names[i].equals(others[i].getName()), names[i]);
        check(!javax.print.attribute.SupportedValuesAttribute.class.isInstance(new javax.print.attribute.standard.Copies(1)), "Copies is not SupportedValues");
        Object rt = serRoundTrip(v);
        check(rt.equals(v), "serialization round-trip");
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
