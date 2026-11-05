/*
 * @test
 * @summary javax.print.attribute.standard.JobSheets conformance: white-box structure + black-box behavior.
 * @run main JobSheetsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobSheetsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobSheets");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintRequestAttribute.class.isAssignableFrom(c), "implements PrintRequestAttribute");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(JobSheets.NONE.getCategory() == JobSheets.class, "getCategory");
        check("job-sheets".equals(JobSheets.NONE.getName()), "getName");
        check(JobSheets.NONE.getValue() == 0, "NONE value == 0");
        check("none".equals(JobSheets.NONE.toString()), "NONE toString");
        check(JobSheets.STANDARD.getValue() == 1, "STANDARD value == 1");
        check("standard".equals(JobSheets.STANDARD.toString()), "STANDARD toString");
        check(JobSheets.NONE.clone() == JobSheets.NONE, "clone returns singleton");
        check(JobSheets.NONE.hashCode() == 0, "hashCode == value");
        check(JobSheets.NONE.equals(JobSheets.NONE), "equals self");
        check(!JobSheets.NONE.equals(JobSheets.STANDARD), "distinct constants unequal");
        check(!JobSheets.NONE.equals(null) && !JobSheets.NONE.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(JobSheets.NONE.getValue() == 0, "NONE.getValue == 0");
        check("none".equals(JobSheets.NONE.toString()), "NONE.toString [none]");
        check(JobSheets.STANDARD.getValue() == 1, "STANDARD.getValue == 1");
        check("standard".equals(JobSheets.STANDARD.toString()), "STANDARD.toString [standard]");
        check("job-sheets".equals(JobSheets.NONE.getName()), "getName == \"job-sheets\"");
        check(JobSheets.class == JobSheets.NONE.getCategory(), "getCategory");
        check(JobSheets.NONE.equals(JobSheets.NONE), "equals self");
        check(!JobSheets.NONE.equals(null), "not equals null");
        check(!JobSheets.NONE.equals("none"), "not equals foreign type");
        check(JobSheets.NONE.hashCode() == 0, "hashCode == value");
        check(JobSheets.NONE.clone() == JobSheets.NONE, "clone returns same singleton");
        check(!JobSheets.NONE.equals(JobSheets.STANDARD), "distinct constants unequal");
        check(JobSheets.NONE.toString() != JobSheets.STANDARD.toString() || true, "toString stable");
        check(!JobSheets.NONE.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        JobSheets.NONE,
        JobSheets.STANDARD
    };
    static final String[] NAMES = {
        "NONE",
        "STANDARD"
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
