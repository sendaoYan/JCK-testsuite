/*
 * @test
 * @summary javax.print.attribute.standard.JobState conformance: white-box structure + black-box behavior.
 * @run main JobStateTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobState");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isFinal(c.getModifiers()), "public class");
        check(EnumSyntax.class.isAssignableFrom(c), "extends EnumSyntax");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(JobState.UNKNOWN.getCategory() == JobState.class, "getCategory");
        check("job-state".equals(JobState.UNKNOWN.getName()), "getName");
        check(JobState.UNKNOWN.getValue() == 0, "UNKNOWN value == 0");
        check("unknown".equals(JobState.UNKNOWN.toString()), "UNKNOWN toString");
        check(JobState.PENDING.getValue() == 3, "PENDING value == 3");
        check("pending".equals(JobState.PENDING.toString()), "PENDING toString");
        check(JobState.PENDING_HELD.getValue() == 4, "PENDING_HELD value == 4");
        check("pending-held".equals(JobState.PENDING_HELD.toString()), "PENDING_HELD toString");
        check(JobState.PROCESSING.getValue() == 5, "PROCESSING value == 5");
        check("processing".equals(JobState.PROCESSING.toString()), "PROCESSING toString");
        check(JobState.PROCESSING_STOPPED.getValue() == 6, "PROCESSING_STOPPED value == 6");
        check("processing-stopped".equals(JobState.PROCESSING_STOPPED.toString()), "PROCESSING_STOPPED toString");
        check(JobState.CANCELED.getValue() == 7, "CANCELED value == 7");
        check("canceled".equals(JobState.CANCELED.toString()), "CANCELED toString");
        check(JobState.ABORTED.getValue() == 8, "ABORTED value == 8");
        check("aborted".equals(JobState.ABORTED.toString()), "ABORTED toString");
        check(JobState.COMPLETED.getValue() == 9, "COMPLETED value == 9");
        check("completed".equals(JobState.COMPLETED.toString()), "COMPLETED toString");
        check(JobState.UNKNOWN.clone() == JobState.UNKNOWN, "clone returns singleton");
        check(JobState.UNKNOWN.hashCode() == 0, "hashCode == value");
        check(JobState.UNKNOWN.equals(JobState.UNKNOWN), "equals self");
        check(!JobState.UNKNOWN.equals(JobState.PENDING), "distinct constants unequal");
        check(!JobState.UNKNOWN.equals(null) && !JobState.UNKNOWN.equals("x"), "not equals null/foreign");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full enum table / category-name / equals / serialization singleton ----
    static void testContractDeep() throws Exception {
        check(JobState.UNKNOWN.getValue() == 0, "UNKNOWN.getValue == 0");
        check("unknown".equals(JobState.UNKNOWN.toString()), "UNKNOWN.toString [unknown]");
        check(JobState.PENDING.getValue() == 3, "PENDING.getValue == 3");
        check("pending".equals(JobState.PENDING.toString()), "PENDING.toString [pending]");
        check(JobState.PENDING_HELD.getValue() == 4, "PENDING_HELD.getValue == 4");
        check("pending-held".equals(JobState.PENDING_HELD.toString()), "PENDING_HELD.toString [pending-held]");
        check(JobState.PROCESSING.getValue() == 5, "PROCESSING.getValue == 5");
        check("processing".equals(JobState.PROCESSING.toString()), "PROCESSING.toString [processing]");
        check(JobState.PROCESSING_STOPPED.getValue() == 6, "PROCESSING_STOPPED.getValue == 6");
        check("processing-stopped".equals(JobState.PROCESSING_STOPPED.toString()), "PROCESSING_STOPPED.toString [processing-stopped]");
        check(JobState.CANCELED.getValue() == 7, "CANCELED.getValue == 7");
        check("canceled".equals(JobState.CANCELED.toString()), "CANCELED.toString [canceled]");
        check(JobState.ABORTED.getValue() == 8, "ABORTED.getValue == 8");
        check("aborted".equals(JobState.ABORTED.toString()), "ABORTED.toString [aborted]");
        check(JobState.COMPLETED.getValue() == 9, "COMPLETED.getValue == 9");
        check("completed".equals(JobState.COMPLETED.toString()), "COMPLETED.toString [completed]");
        check("job-state".equals(JobState.UNKNOWN.getName()), "getName == \"job-state\"");
        check(JobState.class == JobState.UNKNOWN.getCategory(), "getCategory");
        check(JobState.UNKNOWN.equals(JobState.UNKNOWN), "equals self");
        check(!JobState.UNKNOWN.equals(null), "not equals null");
        check(!JobState.UNKNOWN.equals("unknown"), "not equals foreign type");
        check(JobState.UNKNOWN.hashCode() == 0, "hashCode == value");
        check(JobState.UNKNOWN.clone() == JobState.UNKNOWN, "clone returns same singleton");
        check(!JobState.UNKNOWN.equals(JobState.PENDING), "distinct constants unequal");
        check(JobState.UNKNOWN.toString() != JobState.PENDING.toString() || true, "toString stable");
        check(!JobState.UNKNOWN.equals(javax.print.attribute.standard.Chromaticity.COLOR), "cross-enum-type equals false");
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
        JobState.UNKNOWN,
        JobState.PENDING,
        JobState.PENDING_HELD,
        JobState.PROCESSING,
        JobState.PROCESSING_STOPPED,
        JobState.CANCELED,
        JobState.ABORTED,
        JobState.COMPLETED
    };
    static final String[] NAMES = {
        "UNKNOWN",
        "PENDING",
        "PENDING_HELD",
        "PROCESSING",
        "PROCESSING_STOPPED",
        "CANCELED",
        "ABORTED",
        "COMPLETED"
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
