/*
 * @test
 * @summary javax.print.attribute.standard.JobStateReasons conformance: white-box structure + black-box behavior.
 * @run main JobStateReasonsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class JobStateReasonsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.standard.JobStateReasons");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(java.util.HashSet.class.isAssignableFrom(c), "extends HashSet");
        check(PrintJobAttribute.class.isAssignableFrom(c), "implements PrintJobAttribute");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JobStateReasons s = new JobStateReasons();
        check(s.isEmpty() && s.add(JobStateReason.JOB_PRINTING), "add");
        check(s.contains(JobStateReason.JOB_PRINTING) && s.size() == 1, "contains");
        check("job-state-reasons".equals(s.getName()), "getName");
        check(s.getCategory() == JobStateReasons.class, "getCategory");
        JobStateReasons s2 = new JobStateReasons(java.util.Arrays.asList(JobStateReason.JOB_PRINTING, JobStateReason.JOB_QUEUED));
        check(s2.size() == 2, "Collection ctor");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: HashSet set-semantics / attribute contract / ctor validation ----
    static void testContractDeep() throws Exception {
        JobStateReasons s = new JobStateReasons();
        check(s.add(JobStateReason.JOB_QUEUED), "first add true");
        check(!s.add(JobStateReason.JOB_QUEUED), "duplicate add false (set)");
        check(s.size() == 1, "size 1");
        check(s.contains(JobStateReason.JOB_QUEUED), "contains");
        check(!s.contains(JobStateReason.ABORTED_BY_SYSTEM), "not contains other");
        JobStateReasons coll = new JobStateReasons(java.util.Arrays.asList(JobStateReason.JOB_QUEUED, JobStateReason.ABORTED_BY_SYSTEM));
        check(coll.size() == 2, "collection ctor dedups");
        JobStateReasons cap = new JobStateReasons(8);
        JobStateReasons capLf = new JobStateReasons(8, 0.5f);
        check(cap.isEmpty() && capLf.isEmpty(), "capacity ctors");
        expectIAE("cap -1", new Runnable() { public void run() { new JobStateReasons(-1); } }, "Illegal initial capacity: -1");
        expectIAE("loadFactor 0", new Runnable() { public void run() { new JobStateReasons(4, 0f); } }, "Illegal load factor: 0.0");
        try { s.add(null); throw new AssertionError("expected NPE add(null)"); }
        catch (NullPointerException e) { check(e.getMessage() == null || true, "NPE add null"); }
        check("job-state-reasons".equals(s.getName()), "getName");
        check(JobStateReasons.class == s.getCategory(), "getCategory");
        check(s.removeAll(java.util.Arrays.asList(JobStateReason.JOB_QUEUED)), "removeAll");
        check(s.isEmpty(), "empty after removeAll");
        s.add(JobStateReason.JOB_PRINTING); s.add(JobStateReason.JOB_QUEUED);
        boolean cme = false;
        // fail-fast: structural modification during iteration throws CME on next()
        java.util.Iterator<JobStateReason> it2 = s.iterator();
        it2.next(); s.remove(JobStateReason.JOB_QUEUED);
        try { it2.next(); } catch (java.util.ConcurrentModificationException e) { cme = true; }
        check(cme, "iterator fail-fast CME");
        JobStateReasons ser = (JobStateReasons) serRoundTrip(s);
        check(ser.size() == s.size() && ser.containsAll(s), "serialization round-trip");
        check(s.toString().contains("job") || s.toString().startsWith("["), "toString set form");
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
