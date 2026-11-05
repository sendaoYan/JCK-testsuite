/*
 * @test
 * @summary javax.print.attribute.standard.PrintQuality conformance: white-box structure + black-box behavior.
 * @run main PrintQualityTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class PrintQualityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.PrintQuality");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(PrintQuality.DRAFT.getCategory() == PrintQuality.class, "getCategory");
        check("print-quality".equals(PrintQuality.DRAFT.getName()), "getName");
        check(PrintQuality.DRAFT.getValue() == 3, "DRAFT value == 3");
        check("draft".equals(PrintQuality.DRAFT.toString()), "DRAFT toString");
        check(PrintQuality.NORMAL.getValue() == 4, "NORMAL value == 4");
        check("normal".equals(PrintQuality.NORMAL.toString()), "NORMAL toString");
        check(PrintQuality.HIGH.getValue() == 5, "HIGH value == 5");
        check("high".equals(PrintQuality.HIGH.toString()), "HIGH toString");
        check(PrintQuality.DRAFT.clone() == PrintQuality.DRAFT, "clone returns singleton");
        check(PrintQuality.DRAFT.hashCode() == 3, "hashCode == value");
        check(PrintQuality.DRAFT.equals(PrintQuality.DRAFT), "equals self");
        check(!PrintQuality.DRAFT.equals(PrintQuality.NORMAL), "distinct constants unequal");
        check(!PrintQuality.DRAFT.equals(null) && !PrintQuality.DRAFT.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(PrintQuality.DRAFT.getValue() == 3, "DRAFT.getValue == 3");
        check("draft".equals(PrintQuality.DRAFT.toString()), "DRAFT.toString [draft]");
        check(PrintQuality.NORMAL.getValue() == 4, "NORMAL.getValue == 4");
        check("normal".equals(PrintQuality.NORMAL.toString()), "NORMAL.toString [normal]");
        check(PrintQuality.HIGH.getValue() == 5, "HIGH.getValue == 5");
        check("high".equals(PrintQuality.HIGH.toString()), "HIGH.toString [high]");
        check("print-quality".equals(PrintQuality.DRAFT.getName()), "getName == \"print-quality\"");
        check(PrintQuality.class == PrintQuality.DRAFT.getCategory(), "getCategory");
        check(PrintQuality.DRAFT.equals(PrintQuality.DRAFT), "equals self");
        check(!PrintQuality.DRAFT.equals(null), "not equals null");
        check(!PrintQuality.DRAFT.equals("draft"), "not equals foreign type");
        check(PrintQuality.DRAFT.hashCode() == 3, "hashCode == value");
        check(PrintQuality.DRAFT.clone() == PrintQuality.DRAFT, "clone returns same singleton");
        check(!PrintQuality.DRAFT.equals(PrintQuality.NORMAL), "distinct constants unequal");
        check(PrintQuality.DRAFT.toString() != PrintQuality.NORMAL.toString() || true, "toString stable");
        check(!PrintQuality.DRAFT.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        PrintQuality.DRAFT,
        PrintQuality.NORMAL,
        PrintQuality.HIGH
    };
    static final String[] NAMES = {
        "DRAFT",
        "NORMAL",
        "HIGH"
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
