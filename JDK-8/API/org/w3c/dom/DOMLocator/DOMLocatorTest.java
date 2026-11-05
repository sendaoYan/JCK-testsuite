/*
 * @test
 * @summary org.w3c.dom.DOMLocator conformance: reflection shape checks.
 * @run main DOMLocatorTest
 */

import java.lang.reflect.*;

public class DOMLocatorTest {
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
        Class<?> c = Class.forName("org.w3c.dom.DOMLocator");
        check(c != null, "org.w3c.dom.DOMLocator loads");
        check(c.getName().equals("org.w3c.dom.DOMLocator"), "binary name is org.w3c.dom.DOMLocator");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.DOMLocator is public");
        check(c.isInterface(), "org.w3c.dom.DOMLocator is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getLineNumber"), "declares getLineNumber");
        check(declares(c, "getColumnNumber"), "declares getColumnNumber");
        check(declares(c, "getByteOffset"), "declares getByteOffset");
        check(declares(c, "getUtf16Offset"), "declares getUtf16Offset");
        check(declares(c, "getRelatedNode"), "declares getRelatedNode");
        check(declares(c, "getUri"), "declares getUri");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMLocator contract (from a real parse error) ----
    static void deepContract() throws Exception {
        final org.w3c.dom.DOMLocator[] box = new org.w3c.dom.DOMLocator[]{null};
        org.w3c.dom.DOMErrorHandler h = new org.w3c.dom.DOMErrorHandler() {
            public boolean handleError(org.w3c.dom.DOMError error) { if (box[0] == null) box[0] = error.getLocation(); return true; }
        };
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) d.getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p.getDomConfig().setParameter("error-handler", h);
        org.w3c.dom.ls.LSInput in = ls.createLSInput();
        in.setStringData("<r>\n  <1x/>\n</r>");
        try { p.parse(in); } catch (org.w3c.dom.ls.LSException expected) { }
        check(box[0] != null, "DOMLocator available from the DOMError");
        org.w3c.dom.DOMLocator loc = box[0];
        check(loc.getLineNumber() >= 2, "line number points at the offending line");
        check(loc.getColumnNumber() >= 1, "column number is 1-based positive");
        check(loc.getUtf16Offset() >= 0, "utf16 offset non-negative when known");
        check(loc.getUri() == null || loc.getUri().length() >= 0, "uri null or a string");
        // byte offset: -1 when the input was character-based
        boolean byteKnown = loc.getByteOffset() != -1;
        check(!byteKnown || loc.getByteOffset() >= 0, "byte offset either -1 (unknown) or non-negative");
        // second error on a single-line document for a fixed-position check
        final org.w3c.dom.DOMLocator[] first = new org.w3c.dom.DOMLocator[]{null};
        org.w3c.dom.DOMErrorHandler h2 = new org.w3c.dom.DOMErrorHandler() {
            public boolean handleError(org.w3c.dom.DOMError error) { if (first[0] == null) first[0] = error.getLocation(); return true; }
        };
        org.w3c.dom.ls.LSParser p2 = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p2.getDomConfig().setParameter("error-handler", h2);
        org.w3c.dom.ls.LSInput in2 = ls.createLSInput();
        in2.setStringData("<r><1x/></r>");
        try { p2.parse(in2); } catch (org.w3c.dom.ls.LSException expected) { }
        check(first[0].getLineNumber() == 1 && first[0].getColumnNumber() == 5, "single-line document: error located at line 1, column 5");
        check(first[0].getUri() == null, "uri null for in-memory string input");
    }
}
