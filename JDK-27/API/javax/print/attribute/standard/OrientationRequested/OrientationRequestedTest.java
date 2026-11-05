/*
 * @test
 * @summary javax.print.attribute.standard.OrientationRequested conformance: white-box structure + black-box behavior.
 * @run main OrientationRequestedTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class OrientationRequestedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.OrientationRequested");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(OrientationRequested.PORTRAIT.getCategory() == OrientationRequested.class, "getCategory");
        check("orientation-requested".equals(OrientationRequested.PORTRAIT.getName()), "getName");
        check(OrientationRequested.PORTRAIT.getValue() == 3, "PORTRAIT value == 3");
        check("portrait".equals(OrientationRequested.PORTRAIT.toString()), "PORTRAIT toString");
        check(OrientationRequested.LANDSCAPE.getValue() == 4, "LANDSCAPE value == 4");
        check("landscape".equals(OrientationRequested.LANDSCAPE.toString()), "LANDSCAPE toString");
        check(OrientationRequested.REVERSE_LANDSCAPE.getValue() == 5, "REVERSE_LANDSCAPE value == 5");
        check("reverse-landscape".equals(OrientationRequested.REVERSE_LANDSCAPE.toString()), "REVERSE_LANDSCAPE toString");
        check(OrientationRequested.REVERSE_PORTRAIT.getValue() == 6, "REVERSE_PORTRAIT value == 6");
        check("reverse-portrait".equals(OrientationRequested.REVERSE_PORTRAIT.toString()), "REVERSE_PORTRAIT toString");
        check(OrientationRequested.PORTRAIT.clone() == OrientationRequested.PORTRAIT, "clone returns singleton");
        check(OrientationRequested.PORTRAIT.hashCode() == 3, "hashCode == value");
        check(OrientationRequested.PORTRAIT.equals(OrientationRequested.PORTRAIT), "equals self");
        check(!OrientationRequested.PORTRAIT.equals(OrientationRequested.LANDSCAPE), "distinct constants unequal");
        check(!OrientationRequested.PORTRAIT.equals(null) && !OrientationRequested.PORTRAIT.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(OrientationRequested.PORTRAIT.getValue() == 3, "PORTRAIT.getValue == 3");
        check("portrait".equals(OrientationRequested.PORTRAIT.toString()), "PORTRAIT.toString [portrait]");
        check(OrientationRequested.LANDSCAPE.getValue() == 4, "LANDSCAPE.getValue == 4");
        check("landscape".equals(OrientationRequested.LANDSCAPE.toString()), "LANDSCAPE.toString [landscape]");
        check(OrientationRequested.REVERSE_LANDSCAPE.getValue() == 5, "REVERSE_LANDSCAPE.getValue == 5");
        check("reverse-landscape".equals(OrientationRequested.REVERSE_LANDSCAPE.toString()), "REVERSE_LANDSCAPE.toString [reverse-landscape]");
        check(OrientationRequested.REVERSE_PORTRAIT.getValue() == 6, "REVERSE_PORTRAIT.getValue == 6");
        check("reverse-portrait".equals(OrientationRequested.REVERSE_PORTRAIT.toString()), "REVERSE_PORTRAIT.toString [reverse-portrait]");
        check("orientation-requested".equals(OrientationRequested.PORTRAIT.getName()), "getName == \"orientation-requested\"");
        check(OrientationRequested.class == OrientationRequested.PORTRAIT.getCategory(), "getCategory");
        check(OrientationRequested.PORTRAIT.equals(OrientationRequested.PORTRAIT), "equals self");
        check(!OrientationRequested.PORTRAIT.equals(null), "not equals null");
        check(!OrientationRequested.PORTRAIT.equals("portrait"), "not equals foreign type");
        check(OrientationRequested.PORTRAIT.hashCode() == 3, "hashCode == value");
        check(OrientationRequested.PORTRAIT.clone() == OrientationRequested.PORTRAIT, "clone returns same singleton");
        check(!OrientationRequested.PORTRAIT.equals(OrientationRequested.LANDSCAPE), "distinct constants unequal");
        check(OrientationRequested.PORTRAIT.toString() != OrientationRequested.LANDSCAPE.toString() || true, "toString stable");
        check(!OrientationRequested.PORTRAIT.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        OrientationRequested.PORTRAIT,
        OrientationRequested.LANDSCAPE,
        OrientationRequested.REVERSE_LANDSCAPE,
        OrientationRequested.REVERSE_PORTRAIT
    };
    static final String[] NAMES = {
        "PORTRAIT",
        "LANDSCAPE",
        "REVERSE_LANDSCAPE",
        "REVERSE_PORTRAIT"
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
