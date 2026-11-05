/*
 * @test
 * @summary javax.xml.transform.dom.DOMLocator conformance: interface hierarchy and methods.
 * @run main DOMLocatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DOMLocatorTest {
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
        Class<?> c = Class.forName("javax.xml.transform.dom.DOMLocator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.transform.SourceLocator"), "implements javax.xml.transform.SourceLocator");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOriginatingNode"), "has method getOriginatingNode");
        testAnonImpl();
        testAsSourceLocator();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: behavior + locator carried through exception + DOMLocator usable as SourceLocator
    static void testAnonImpl() throws Exception {
        final org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        javax.xml.transform.dom.DOMLocator loc = new javax.xml.transform.dom.DOMLocator() {
            public org.w3c.dom.Node getOriginatingNode() { return doc; }
            public String getPublicId() { return "pub"; }
            public String getSystemId() { return "http://base/x.xml"; }
            public int getLineNumber() { return 2; }
            public int getColumnNumber() { return 4; }
        };
        check(loc.getOriginatingNode() == doc, "getOriginatingNode returns the DOM node");
        check("pub".equals(loc.getPublicId()), "getPublicId");
        check("http://base/x.xml".equals(loc.getSystemId()), "getSystemId");
        check(loc.getLineNumber() == 2 && loc.getColumnNumber() == 4, "line/column");
    }

    static void testAsSourceLocator() {
        javax.xml.transform.dom.DOMLocator loc = new javax.xml.transform.dom.DOMLocator() {
            public org.w3c.dom.Node getOriginatingNode() { return null; }
            public String getPublicId() { return null; }
            public String getSystemId() { return "http://base/x.xml"; }
            public int getLineNumber() { return 6; }
            public int getColumnNumber() { return 9; }
        };
        check(loc instanceof javax.xml.transform.SourceLocator,
              "DOMLocator is usable wherever a SourceLocator is required");
        javax.xml.transform.TransformerException e =
            new javax.xml.transform.TransformerException("m", loc);
        check(e.getLocator() == loc, "TransformerException keeps the DOMLocator reference");
        check(e.getLocationAsString() != null && e.getLocationAsString().contains("6"),
              "DOMLocator rendered in getLocationAsString");
    }
}
