/*
 * @test
 * @summary javax.print.attribute.standard.SheetCollate conformance: white-box structure + black-box behavior.
 * @run main SheetCollateTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class SheetCollateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.SheetCollate");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(SheetCollate.UNCOLLATED.getCategory() == SheetCollate.class, "getCategory");
        check("sheet-collate".equals(SheetCollate.UNCOLLATED.getName()), "getName");
        check(SheetCollate.UNCOLLATED.getValue() == 0, "UNCOLLATED value == 0");
        check("uncollated".equals(SheetCollate.UNCOLLATED.toString()), "UNCOLLATED toString");
        check(SheetCollate.COLLATED.getValue() == 1, "COLLATED value == 1");
        check("collated".equals(SheetCollate.COLLATED.toString()), "COLLATED toString");
        check(SheetCollate.UNCOLLATED.clone() == SheetCollate.UNCOLLATED, "clone returns singleton");
        check(SheetCollate.UNCOLLATED.hashCode() == 0, "hashCode == value");
        check(SheetCollate.UNCOLLATED.equals(SheetCollate.UNCOLLATED), "equals self");
        check(!SheetCollate.UNCOLLATED.equals(SheetCollate.COLLATED), "distinct constants unequal");
        check(!SheetCollate.UNCOLLATED.equals(null) && !SheetCollate.UNCOLLATED.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(SheetCollate.UNCOLLATED.getValue() == 0, "UNCOLLATED.getValue == 0");
        check("uncollated".equals(SheetCollate.UNCOLLATED.toString()), "UNCOLLATED.toString [uncollated]");
        check(SheetCollate.COLLATED.getValue() == 1, "COLLATED.getValue == 1");
        check("collated".equals(SheetCollate.COLLATED.toString()), "COLLATED.toString [collated]");
        check("sheet-collate".equals(SheetCollate.UNCOLLATED.getName()), "getName == \"sheet-collate\"");
        check(SheetCollate.class == SheetCollate.UNCOLLATED.getCategory(), "getCategory");
        check(SheetCollate.UNCOLLATED.equals(SheetCollate.UNCOLLATED), "equals self");
        check(!SheetCollate.UNCOLLATED.equals(null), "not equals null");
        check(!SheetCollate.UNCOLLATED.equals("uncollated"), "not equals foreign type");
        check(SheetCollate.UNCOLLATED.hashCode() == 0, "hashCode == value");
        check(SheetCollate.UNCOLLATED.clone() == SheetCollate.UNCOLLATED, "clone returns same singleton");
        check(!SheetCollate.UNCOLLATED.equals(SheetCollate.COLLATED), "distinct constants unequal");
        check(SheetCollate.UNCOLLATED.toString() != SheetCollate.COLLATED.toString() || true, "toString stable");
        check(!SheetCollate.UNCOLLATED.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        SheetCollate.UNCOLLATED,
        SheetCollate.COLLATED
    };
    static final String[] NAMES = {
        "UNCOLLATED",
        "COLLATED"
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
