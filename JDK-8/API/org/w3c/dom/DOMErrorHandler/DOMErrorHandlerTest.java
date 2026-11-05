/*
 * @test
 * @summary org.w3c.dom.DOMErrorHandler conformance: reflection shape checks.
 * @run main DOMErrorHandlerTest
 */

import java.lang.reflect.*;

public class DOMErrorHandlerTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DOMErrorHandler");
        check(c != null, "org.w3c.dom.DOMErrorHandler loads");
        check(c.getName().equals("org.w3c.dom.DOMErrorHandler"), "binary name is org.w3c.dom.DOMErrorHandler");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DOMErrorHandler is public");
        check(c.isInterface(), "org.w3c.dom.DOMErrorHandler is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "handleError"), "declares handleError");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMErrorHandler contract (routing true/false) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) d.getImplementation().getFeature("LS", "3.0");
        final int[] calls = new int[]{0};
        final org.w3c.dom.DOMError[] last = new org.w3c.dom.DOMError[]{null};
        final boolean[] answer = new boolean[]{true};
        org.w3c.dom.DOMErrorHandler h = new org.w3c.dom.DOMErrorHandler() {
            public boolean handleError(org.w3c.dom.DOMError error) { calls[0]++; last[0] = error; return answer[0]; }
        };
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p.getDomConfig().setParameter("error-handler", h);
        org.w3c.dom.ls.LSInput in = ls.createLSInput();
        in.setStringData("<r><1x/></r>");
        boolean caught = false;
        try { p.parse(in); } catch (org.w3c.dom.ls.LSException expected) { caught = true; }
        check(calls[0] >= 1, "handler invoked for fatal well-formedness error");
        check(last[0] != null && last[0].getSeverity() == org.w3c.dom.DOMError.SEVERITY_FATAL_ERROR, "handler sees the DOMError");
        check(caught, "return true: fatal error still terminates parse with LSException");
        answer[0] = false;
        org.w3c.dom.ls.LSParser p2 = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p2.getDomConfig().setParameter("error-handler", h);
        org.w3c.dom.ls.LSInput in2 = ls.createLSInput();
        in2.setStringData("<r><1y/></r>");
        boolean caught2 = false;
        try { p2.parse(in2); } catch (org.w3c.dom.ls.LSException expected) { caught2 = true; }
        check(caught2, "return false: parse fails with LSException");
        // well-formed input: handler installed but silent
        calls[0] = 0;
        org.w3c.dom.ls.LSParser p3 = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p3.getDomConfig().setParameter("error-handler", h);
        org.w3c.dom.ls.LSInput in3 = ls.createLSInput();
        in3.setStringData("<r><a/></r>");
        p3.parse(in3);
        check(calls[0] == 0, "well-formed parse never calls the handler");
        // the same contract drives Document.normalizeDocument through the document configuration
        final boolean[] docCalled = new boolean[]{false};
        final String[] docType = new String[]{null};
        d.getDomConfig().setParameter("error-handler", new org.w3c.dom.DOMErrorHandler() {
            public boolean handleError(org.w3c.dom.DOMError error) { docCalled[0] = true; docType[0] = error.getType(); return true; }
        });
        d.appendChild(d.createElement("ok"));
        d.normalizeDocument();
        check(docCalled[0] && "NullLocalElementName".equals(docType[0]), "normalizeDocument flags non-NS element as NullLocalElementName (severity ERROR)");
        // a properly namespaced tree normalizes silently
        final boolean[] nsCalled = new boolean[]{false};
        javax.xml.parsers.DocumentBuilderFactory nf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        nf.setNamespaceAware(true);
        org.w3c.dom.Document nd = nf.newDocumentBuilder().newDocument();
        nd.getDomConfig().setParameter("error-handler", new org.w3c.dom.DOMErrorHandler() {
            public boolean handleError(org.w3c.dom.DOMError error) { nsCalled[0] = true; return true; }
        });
        nd.appendChild(nd.createElementNS("urn:z", "z:ok"));
        nd.normalizeDocument();
        check(!nsCalled[0], "normalizeDocument on a namespaced tree issues no callbacks");
    }
}
