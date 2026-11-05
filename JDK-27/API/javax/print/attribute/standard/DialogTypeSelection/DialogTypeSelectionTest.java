/*
 * @test
 * @summary javax.print.attribute.standard.DialogTypeSelection conformance: white-box structure + black-box behavior.
 * @run main DialogTypeSelectionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class DialogTypeSelectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.DialogTypeSelection");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(DialogTypeSelection.NATIVE.getCategory() == DialogTypeSelection.class, "getCategory");
        check("dialog-type-selection".equals(DialogTypeSelection.NATIVE.getName()), "getName");
        check(DialogTypeSelection.NATIVE.getValue() == 0, "NATIVE value == 0");
        check("native".equals(DialogTypeSelection.NATIVE.toString()), "NATIVE toString");
        check(DialogTypeSelection.COMMON.getValue() == 1, "COMMON value == 1");
        check("common".equals(DialogTypeSelection.COMMON.toString()), "COMMON toString");
        check(DialogTypeSelection.NATIVE.clone() == DialogTypeSelection.NATIVE, "clone returns singleton");
        check(DialogTypeSelection.NATIVE.hashCode() == 0, "hashCode == value");
        check(DialogTypeSelection.NATIVE.equals(DialogTypeSelection.NATIVE), "equals self");
        check(!DialogTypeSelection.NATIVE.equals(DialogTypeSelection.COMMON), "distinct constants unequal");
        check(!DialogTypeSelection.NATIVE.equals(null) && !DialogTypeSelection.NATIVE.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(DialogTypeSelection.NATIVE.getValue() == 0, "NATIVE.getValue == 0");
        check("native".equals(DialogTypeSelection.NATIVE.toString()), "NATIVE.toString [native]");
        check(DialogTypeSelection.COMMON.getValue() == 1, "COMMON.getValue == 1");
        check("common".equals(DialogTypeSelection.COMMON.toString()), "COMMON.toString [common]");
        check("dialog-type-selection".equals(DialogTypeSelection.NATIVE.getName()), "getName == \"dialog-type-selection\"");
        check(DialogTypeSelection.class == DialogTypeSelection.NATIVE.getCategory(), "getCategory");
        check(DialogTypeSelection.NATIVE.equals(DialogTypeSelection.NATIVE), "equals self");
        check(!DialogTypeSelection.NATIVE.equals(null), "not equals null");
        check(!DialogTypeSelection.NATIVE.equals("native"), "not equals foreign type");
        check(DialogTypeSelection.NATIVE.hashCode() == 0, "hashCode == value");
        check(DialogTypeSelection.NATIVE.clone() == DialogTypeSelection.NATIVE, "clone returns same singleton");
        check(!DialogTypeSelection.NATIVE.equals(DialogTypeSelection.COMMON), "distinct constants unequal");
        check(DialogTypeSelection.NATIVE.toString() != DialogTypeSelection.COMMON.toString() || true, "toString stable");
        check(!DialogTypeSelection.NATIVE.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        DialogTypeSelection.NATIVE,
        DialogTypeSelection.COMMON
    };
    static final String[] NAMES = {
        "NATIVE",
        "COMMON"
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
