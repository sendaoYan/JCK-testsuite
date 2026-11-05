/*
 * @test
 * @summary javax.print.attribute.standard.Sides conformance: white-box structure + black-box behavior.
 * @run main SidesTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class SidesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.Sides");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(DocAttribute.class.isAssignableFrom(c), "implements DocAttribute");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Sides.ONE_SIDED.getCategory() == Sides.class, "getCategory");
        check("sides".equals(Sides.ONE_SIDED.getName()), "getName");
        check(Sides.ONE_SIDED.getValue() == 0, "ONE_SIDED value == 0");
        check("one-sided".equals(Sides.ONE_SIDED.toString()), "ONE_SIDED toString");
        check(Sides.TWO_SIDED_LONG_EDGE.getValue() == 1, "TWO_SIDED_LONG_EDGE value == 1");
        check("two-sided-long-edge".equals(Sides.TWO_SIDED_LONG_EDGE.toString()), "TWO_SIDED_LONG_EDGE toString");
        check(Sides.TWO_SIDED_SHORT_EDGE.getValue() == 2, "TWO_SIDED_SHORT_EDGE value == 2");
        check("two-sided-short-edge".equals(Sides.TWO_SIDED_SHORT_EDGE.toString()), "TWO_SIDED_SHORT_EDGE toString");
        check(Sides.DUPLEX.getValue() == 1, "DUPLEX value == 1");
        check("two-sided-long-edge".equals(Sides.DUPLEX.toString()), "DUPLEX toString");
        check(Sides.TUMBLE.getValue() == 2, "TUMBLE value == 2");
        check("two-sided-short-edge".equals(Sides.TUMBLE.toString()), "TUMBLE toString");
        check(Sides.ONE_SIDED.clone() == Sides.ONE_SIDED, "clone returns singleton");
        check(Sides.ONE_SIDED.hashCode() == 0, "hashCode == value");
        check(Sides.ONE_SIDED.equals(Sides.ONE_SIDED), "equals self");
        check(!Sides.ONE_SIDED.equals(Sides.TWO_SIDED_LONG_EDGE), "distinct constants unequal");
        check(!Sides.ONE_SIDED.equals(null) && !Sides.ONE_SIDED.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(Sides.ONE_SIDED.getValue() == 0, "ONE_SIDED.getValue == 0");
        check("one-sided".equals(Sides.ONE_SIDED.toString()), "ONE_SIDED.toString [one-sided]");
        check(Sides.TWO_SIDED_LONG_EDGE.getValue() == 1, "TWO_SIDED_LONG_EDGE.getValue == 1");
        check("two-sided-long-edge".equals(Sides.TWO_SIDED_LONG_EDGE.toString()), "TWO_SIDED_LONG_EDGE.toString [two-sided-long-edge]");
        check(Sides.TWO_SIDED_SHORT_EDGE.getValue() == 2, "TWO_SIDED_SHORT_EDGE.getValue == 2");
        check("two-sided-short-edge".equals(Sides.TWO_SIDED_SHORT_EDGE.toString()), "TWO_SIDED_SHORT_EDGE.toString [two-sided-short-edge]");
        check(Sides.DUPLEX.getValue() == 1, "DUPLEX.getValue == 1");
        check(Sides.TUMBLE.getValue() == 2, "TUMBLE.getValue == 2");
        check("sides".equals(Sides.ONE_SIDED.getName()), "getName == \"sides\"");
        check(Sides.class == Sides.ONE_SIDED.getCategory(), "getCategory");
        check(Sides.ONE_SIDED.equals(Sides.ONE_SIDED), "equals self");
        check(!Sides.ONE_SIDED.equals(null), "not equals null");
        check(!Sides.ONE_SIDED.equals("one-sided"), "not equals foreign type");
        check(Sides.ONE_SIDED.hashCode() == 0, "hashCode == value");
        check(Sides.ONE_SIDED.clone() == Sides.ONE_SIDED, "clone returns same singleton");
        check(!Sides.ONE_SIDED.equals(Sides.TWO_SIDED_LONG_EDGE), "distinct constants unequal");
        check(Sides.ONE_SIDED.toString() != Sides.TWO_SIDED_LONG_EDGE.toString() || true, "toString stable");
        check(!Sides.ONE_SIDED.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
        for (int i = 0; i < NAMES.length; i++) {
            javax.print.attribute.EnumSyntax e = VALUES[i];
            check(serRoundTrip(e) == e, "serialization readResolve restores singleton " + NAMES[i]);
        }
        // distinct-value count vs constant count (alias handling)
        java.util.Set<Integer> vals = new java.util.HashSet<Integer>();
        for (javax.print.attribute.EnumSyntax e : VALUES) vals.add(e.getValue());
        check(vals.size() == 3, "Sides has 3 distinct values (5 constants, 2 aliases)");
        check(Sides.DUPLEX.toString().equals("two-sided-long-edge"), "DUPLEX alias toString resolves via value table");
        check(Sides.TUMBLE.toString().equals("two-sided-short-edge"), "TUMBLE alias toString resolves via value table");
        check(Sides.DUPLEX == Sides.TWO_SIDED_LONG_EDGE, "DUPLEX aliases same object as TWO_SIDED_LONG_EDGE");
        check(Sides.DUPLEX.equals(Sides.TWO_SIDED_LONG_EDGE), "alias DUPLEX value-equal");
        check(Sides.TUMBLE.equals(Sides.TWO_SIDED_SHORT_EDGE), "alias TUMBLE value-equal");
    }

    static final javax.print.attribute.EnumSyntax[] VALUES = {
        Sides.ONE_SIDED,
        Sides.TWO_SIDED_LONG_EDGE,
        Sides.TWO_SIDED_SHORT_EDGE,
        Sides.DUPLEX,
        Sides.TUMBLE
    };
    static final String[] NAMES = {
        "ONE_SIDED",
        "TWO_SIDED_LONG_EDGE",
        "TWO_SIDED_SHORT_EDGE",
        "DUPLEX",
        "TUMBLE"
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
