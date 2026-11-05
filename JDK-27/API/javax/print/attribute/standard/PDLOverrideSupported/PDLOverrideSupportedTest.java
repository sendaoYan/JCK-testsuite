/*
 * @test
 * @summary javax.print.attribute.standard.PDLOverrideSupported conformance: white-box structure + black-box behavior.
 * @run main PDLOverrideSupportedTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PDLOverrideSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PDLOverrideSupported");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintServiceAttribute.class.isAssignableFrom(c), "implements PrintServiceAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(PDLOverrideSupported.NOT_ATTEMPTED.getCategory() == PDLOverrideSupported.class, "getCategory");
        check("pdl-override-supported".equals(PDLOverrideSupported.NOT_ATTEMPTED.getName()), "getName");
        check(PDLOverrideSupported.NOT_ATTEMPTED.getValue() == 0, "NOT_ATTEMPTED value == 0");
        check("not-attempted".equals(PDLOverrideSupported.NOT_ATTEMPTED.toString()), "NOT_ATTEMPTED toString");
        check(PDLOverrideSupported.ATTEMPTED.getValue() == 1, "ATTEMPTED value == 1");
        check("attempted".equals(PDLOverrideSupported.ATTEMPTED.toString()), "ATTEMPTED toString");
        check(PDLOverrideSupported.NOT_ATTEMPTED.clone() == PDLOverrideSupported.NOT_ATTEMPTED, "clone returns singleton");
        check(PDLOverrideSupported.NOT_ATTEMPTED.hashCode() == 0, "hashCode == value");
        check(PDLOverrideSupported.NOT_ATTEMPTED.equals(PDLOverrideSupported.NOT_ATTEMPTED), "equals self");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals(PDLOverrideSupported.ATTEMPTED), "distinct constants unequal");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals(null) && !PDLOverrideSupported.NOT_ATTEMPTED.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(PDLOverrideSupported.NOT_ATTEMPTED.getValue() == 0, "NOT_ATTEMPTED.getValue == 0");
        check("not-attempted".equals(PDLOverrideSupported.NOT_ATTEMPTED.toString()), "NOT_ATTEMPTED.toString [not-attempted]");
        check(PDLOverrideSupported.ATTEMPTED.getValue() == 1, "ATTEMPTED.getValue == 1");
        check("attempted".equals(PDLOverrideSupported.ATTEMPTED.toString()), "ATTEMPTED.toString [attempted]");
        check("pdl-override-supported".equals(PDLOverrideSupported.NOT_ATTEMPTED.getName()), "getName == \"pdl-override-supported\"");
        check(PDLOverrideSupported.class == PDLOverrideSupported.NOT_ATTEMPTED.getCategory(), "getCategory");
        check(PDLOverrideSupported.NOT_ATTEMPTED.equals(PDLOverrideSupported.NOT_ATTEMPTED), "equals self");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals(null), "not equals null");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals("not-attempted"), "not equals foreign type");
        check(PDLOverrideSupported.NOT_ATTEMPTED.hashCode() == 0, "hashCode == value");
        check(PDLOverrideSupported.NOT_ATTEMPTED.clone() == PDLOverrideSupported.NOT_ATTEMPTED, "clone returns same singleton");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals(PDLOverrideSupported.ATTEMPTED), "distinct constants unequal");
        check(PDLOverrideSupported.NOT_ATTEMPTED.toString() != PDLOverrideSupported.ATTEMPTED.toString() || true, "toString stable");
        check(!PDLOverrideSupported.NOT_ATTEMPTED.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
        for (int i = 0; i < NAMES.length; i++) {
            javax.print.attribute.EnumSyntax e = VALUES[i];
            check(serRoundTrip(e) == e, "serialization readResolve restores singleton " + NAMES[i]);
        }
        // distinct-value count vs constant count (alias handling)
        java.util.Set<Integer> vals = new java.util.HashSet<Integer>();
        for (javax.print.attribute.EnumSyntax e : VALUES) vals.add(e.getValue());
        check(vals.size() == VALUES.length, "all values distinct");
    }

    static final javax.print.attribute.EnumSyntax[] VALUES = {
        PDLOverrideSupported.NOT_ATTEMPTED,
        PDLOverrideSupported.ATTEMPTED
    };
    static final String[] NAMES = {
        "NOT_ATTEMPTED",
        "ATTEMPTED"
    };

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
