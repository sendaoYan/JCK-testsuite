/*
 * @test
 * @summary javax.xml.stream.XMLReporter conformance: interface hierarchy and methods.
 * @run main XMLReporterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLReporterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.XMLReporter");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("report"), "has method report");
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
