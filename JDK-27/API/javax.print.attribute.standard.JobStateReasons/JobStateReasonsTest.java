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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
