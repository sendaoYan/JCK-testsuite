/*
 * @test
 * @summary org.w3c.dom.DOMError conformance: reflection shape checks.
 * @run main DOMErrorTest
 */

import java.lang.reflect.*;

public class DOMErrorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.DOMError");
        check(c != null, "org.w3c.dom.DOMError loads");
        check(c.getName().equals("org.w3c.dom.DOMError"), "binary name is org.w3c.dom.DOMError");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DOMError is public");
        check(c.isInterface(), "org.w3c.dom.DOMError is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getSeverity"), "declares getSeverity");
        check(declares(c, "getMessage"), "declares getMessage");
        check(declares(c, "getType"), "declares getType");
        check(declares(c, "getRelatedException"), "declares getRelatedException");
        check(declares(c, "getRelatedData"), "declares getRelatedData");
        check(declares(c, "getLocation"), "declares getLocation");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMError contract (live instance from LS parse failure) ----
    static void deepContract() throws Exception {
        final java.util.List<org.w3c.dom.DOMError> seen = new java.util.ArrayList<org.w3c.dom.DOMError>();
        org.w3c.dom.DOMErrorHandler handler = new org.w3c.dom.DOMErrorHandler() {
            public boolean handleError(org.w3c.dom.DOMError error) { seen.add(error); return true; }
        };
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) d.getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p.getDomConfig().setParameter("error-handler", handler);
        org.w3c.dom.ls.LSInput in = ls.createLSInput();
        in.setStringData("<r><1x/></r>");
        try { p.parse(in); } catch (org.w3c.dom.ls.LSException expected) { }
        check(seen.size() >= 1, "ill-formed input reaches the error handler");
        org.w3c.dom.DOMError e = seen.get(0);
        check(e.getSeverity() == org.w3c.dom.DOMError.SEVERITY_FATAL_ERROR, "well-formedness failure severity SEVERITY_FATAL_ERROR(3)");
        check(org.w3c.dom.DOMError.SEVERITY_WARNING == 1 && org.w3c.dom.DOMError.SEVERITY_ERROR == 2, "severity constants 1/2");
        check(e.getMessage() != null && !e.getMessage().isEmpty(), "DOMError message non-null");
        check(e.getType() != null && e.getType().startsWith("wf-"), "well-formedness error type starts with 'wf-'");
        check(e.getRelatedException() != null, "related exception attached to parse errors");
        check(e.getLocation() != null, "error location attached");
        // the same failure also surfaces as LSException from parse()
        final boolean[] ret = new boolean[]{true};
        org.w3c.dom.DOMErrorHandler flip = new org.w3c.dom.DOMErrorHandler() {
            public boolean handleError(org.w3c.dom.DOMError error) { return ret[0]; }
        };
        org.w3c.dom.ls.LSParser p2 = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p2.getDomConfig().setParameter("error-handler", flip);
        org.w3c.dom.ls.LSInput in2 = ls.createLSInput();
        in2.setStringData("<r><1y/></r>");
        boolean threw81 = false;
        try { p2.parse(in2); } catch (org.w3c.dom.ls.LSException ex) { threw81 = ex.code == org.w3c.dom.ls.LSException.PARSE_ERR && org.w3c.dom.ls.LSException.PARSE_ERR == 81; }
        check(threw81, "handler=true: fatal error still ends in LSException PARSE_ERR(81)");
        ret[0] = false;
        org.w3c.dom.ls.LSParser p3 = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p3.getDomConfig().setParameter("error-handler", flip);
        org.w3c.dom.ls.LSInput in3 = ls.createLSInput();
        in3.setStringData("<r><1z/></r>");
        boolean threw81b = false;
        try { p3.parse(in3); } catch (org.w3c.dom.ls.LSException ex) { threw81b = ex.code == 81; }
        check(threw81b, "handler=false: parse also ends in LSException PARSE_ERR(81)");
        // a well-formed document produces no callbacks
        seen.clear();
        org.w3c.dom.ls.LSParser p4 = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p4.getDomConfig().setParameter("error-handler", handler);
        org.w3c.dom.ls.LSInput in4 = ls.createLSInput();
        in4.setStringData("<r/>");
        p4.parse(in4);
        check(seen.isEmpty(), "well-formed parse issues no DOMError callbacks");
    }
}
