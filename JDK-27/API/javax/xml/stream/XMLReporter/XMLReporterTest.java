/*
 * @test
 * @summary javax.xml.stream.XMLReporter conformance: white-box structure + black-box behavior.
 * @run main XMLReporterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLReporterTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.stream.XMLReporter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "report"), "method report");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.XMLReporter a = new javax.xml.stream.XMLReporter(){
            public void report(java.lang.String p0, java.lang.String p1, java.lang.Object p2, javax.xml.stream.Location p3) { }
        };
        check(a != null, "anon impl of XMLReporter");
        a.report("s", "s", (java.lang.Object) null, (javax.xml.stream.Location) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReporterContract();
        testReporterTriggerScenarios();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: interface contract + installation/getter identity ----
    static void testReporterContract() throws Exception {
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        javax.xml.stream.XMLReporter rep = new javax.xml.stream.XMLReporter() {
            public void report(String message, String errorType, Object relatedInfo, javax.xml.stream.Location location) {
                calls.add(errorType + ":" + message);
            }
        };
        // direct invocation contract: all-null args tolerated, invocation recorded
        rep.report(null, null, null, null);
        check(calls.size() == 1, "report() invoked directly with nulls tolerated");
        // install / getter identity
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        f.setXMLReporter(rep);
        check(f.getXMLReporter() == rep, "setXMLReporter/getXMLReporter identity");
        javax.xml.stream.XMLInputFactory f2 = javax.xml.stream.XMLInputFactory.newFactory();
        check(f2.getXMLReporter() == null, "default factory has no reporter");
    }

    // ---- Deepened 2: trigger scenarios (empirically probed; none fire on this stack) ----
    static void testReporterTriggerScenarios() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        final java.util.List<String> reports = new java.util.ArrayList<String>();
        f.setXMLReporter(new javax.xml.stream.XMLReporter() {
            public void report(String message, String errorType, Object relatedInfo, javax.xml.stream.Location location) {
                reports.add(errorType + ":" + message);
            }
        });
        // (a) duplicate entity declaration in the internal subset: parse completes, no report observed
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader(
            "<!DOCTYPE r [<!ENTITY e 'a'><!ENTITY e 'b'>]><r>&e;</r>"));
        int events = 0;
        while (r.hasNext()) { r.next(); events++; }
        r.close();
        check(events > 0, "duplicate entity decl document still parses");
        check(reports.isEmpty(), "JDK-8: reporter NOT triggered by duplicate entity decl (locked: no callback)");
        // (b) fatal error surfaces as XMLStreamException, not via reporter
        boolean thrown = false;
        javax.xml.stream.XMLStreamReader r2 = f.createXMLStreamReader(new java.io.StringReader("<a><b></a>"));
        try { while (r2.hasNext()) r2.next(); }
        catch (javax.xml.stream.XMLStreamException e) { thrown = true; }
        check(thrown && reports.isEmpty(),
            "fatal well-formedness error -> XMLStreamException, never routed through reporter");
        if (r2 != null) { try { r2.close(); } catch (Throwable ignore) {} }
        // (c) reporter present does not alter parse results
        javax.xml.stream.XMLStreamReader r3 = f.createXMLStreamReader(new java.io.StringReader(
            "<r><k>v</k></r>"));
        StringBuilder names = new StringBuilder();
        while (r3.hasNext()) { int ev = r3.next(); if (ev == 1) names.append(r3.getLocalName()); }
        r3.close();
        check("rk".equals(names.toString()), "installed reporter does not change parse output");
    }
}
