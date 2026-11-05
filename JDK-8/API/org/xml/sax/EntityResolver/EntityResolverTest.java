/*
 * @test
 * @summary org.xml.sax.EntityResolver conformance: interface hierarchy and methods.
 * @run main EntityResolverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class EntityResolverTest {
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
        Class<?> c = Class.forName("org.xml.sax.EntityResolver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("resolveEntity"), "has method resolveEntity");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral EntityResolver contract (substitution + default) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final org.xml.sax.InputSource[] substitution = new org.xml.sax.InputSource[1];
        final java.util.List<String> asked = new java.util.ArrayList<String>();
        xr.setEntityResolver(new org.xml.sax.EntityResolver() {
            public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
                asked.add(systemId);
                return substitution[0];
            }
        });
        final StringBuilder body = new StringBuilder();
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void characters(char[] c, int s, int n) { body.append(c, s, n); }
        });
        // returning a replacement InputSource: its content is used
        substitution[0] = new org.xml.sax.InputSource(new java.io.StringReader("[resolved]"));
        String xml = "<!DOCTYPE r [<!ENTITY e SYSTEM 'ent.xml'>]><r>&e;</r>";
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        check(body.toString().contains("[resolved]"), "resolver-provided InputSource content flows into characters");
        check(!asked.isEmpty(), "resolveEntity consulted with the (absolutized) systemId");
        // returning null -> default resolution (missing file -> FileNotFoundException)
        substitution[0] = null;
        boolean fnf = false;
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml))); }
        catch (java.io.FileNotFoundException e) { fnf = true; }
        check(fnf, "resolver returning null falls back to default resolution (missing file -> FileNotFoundException)");
        check(xr.getEntityResolver() != null, "getEntityResolver round-trip");
    }
}
