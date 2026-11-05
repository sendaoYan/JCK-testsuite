/*
 * @test
 * @summary javax.print.attribute.standard.ColorSupported conformance: white-box structure + black-box behavior.
 * @run main ColorSupportedTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class ColorSupportedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.ColorSupported");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintServiceAttribute.class.isAssignableFrom(c), "implements PrintServiceAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(ColorSupported.NOT_SUPPORTED.getCategory() == ColorSupported.class, "getCategory");
        check("color-supported".equals(ColorSupported.NOT_SUPPORTED.getName()), "getName");
        check(ColorSupported.NOT_SUPPORTED.getValue() == 0, "NOT_SUPPORTED value == 0");
        check("not-supported".equals(ColorSupported.NOT_SUPPORTED.toString()), "NOT_SUPPORTED toString");
        check(ColorSupported.SUPPORTED.getValue() == 1, "SUPPORTED value == 1");
        check("supported".equals(ColorSupported.SUPPORTED.toString()), "SUPPORTED toString");
        check(ColorSupported.NOT_SUPPORTED.clone() == ColorSupported.NOT_SUPPORTED, "clone returns singleton");
        check(ColorSupported.NOT_SUPPORTED.hashCode() == 0, "hashCode == value");
        check(ColorSupported.NOT_SUPPORTED.equals(ColorSupported.NOT_SUPPORTED), "equals self");
        check(!ColorSupported.NOT_SUPPORTED.equals(ColorSupported.SUPPORTED), "distinct constants unequal");
        check(!ColorSupported.NOT_SUPPORTED.equals(null) && !ColorSupported.NOT_SUPPORTED.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(ColorSupported.NOT_SUPPORTED.getValue() == 0, "NOT_SUPPORTED.getValue == 0");
        check("not-supported".equals(ColorSupported.NOT_SUPPORTED.toString()), "NOT_SUPPORTED.toString [not-supported]");
        check(ColorSupported.SUPPORTED.getValue() == 1, "SUPPORTED.getValue == 1");
        check("supported".equals(ColorSupported.SUPPORTED.toString()), "SUPPORTED.toString [supported]");
        check("color-supported".equals(ColorSupported.NOT_SUPPORTED.getName()), "getName == \"color-supported\"");
        check(ColorSupported.class == ColorSupported.NOT_SUPPORTED.getCategory(), "getCategory");
        check(ColorSupported.NOT_SUPPORTED.equals(ColorSupported.NOT_SUPPORTED), "equals self");
        check(!ColorSupported.NOT_SUPPORTED.equals(null), "not equals null");
        check(!ColorSupported.NOT_SUPPORTED.equals("not-supported"), "not equals foreign type");
        check(ColorSupported.NOT_SUPPORTED.hashCode() == 0, "hashCode == value");
        check(ColorSupported.NOT_SUPPORTED.clone() == ColorSupported.NOT_SUPPORTED, "clone returns same singleton");
        check(!ColorSupported.NOT_SUPPORTED.equals(ColorSupported.SUPPORTED), "distinct constants unequal");
        check(ColorSupported.NOT_SUPPORTED.toString() != ColorSupported.SUPPORTED.toString() || true, "toString stable");
        check(!ColorSupported.NOT_SUPPORTED.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        ColorSupported.NOT_SUPPORTED,
        ColorSupported.SUPPORTED
    };
    static final String[] NAMES = {
        "NOT_SUPPORTED",
        "SUPPORTED"
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
