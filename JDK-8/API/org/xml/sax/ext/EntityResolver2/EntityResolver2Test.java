/*
 * @test
 * @summary org.xml.sax.ext.EntityResolver2 conformance: interface hierarchy and methods.
 * @run main EntityResolver2Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class EntityResolver2Test {
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
        Class<?> c = Class.forName("org.xml.sax.ext.EntityResolver2");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.EntityResolver"), "implements org.xml.sax.EntityResolver");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getExternalSubset"), "has method getExternalSubset");
        check(names.contains("resolveEntity"), "has method resolveEntity");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral EntityResolver2 contract (4-arg resolution channel) ----
    static void deepContract() throws Exception {
        check(org.xml.sax.EntityResolver.class.isAssignableFrom(org.xml.sax.ext.EntityResolver2.class), "EntityResolver2 extends EntityResolver");
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        org.xml.sax.ext.EntityResolver2 er2 = new org.xml.sax.ext.EntityResolver2() {
            public org.xml.sax.InputSource getExternalSubset(String name, String baseURI) { calls.add("subset(" + name + ")"); return null; }
            public org.xml.sax.InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) { calls.add("resolve4(" + name + "," + systemId + ")"); return null; }
            public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) { calls.add("resolve2(" + systemId + ")"); return null; }
        };
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setEntityResolver(er2);
        boolean fnf = false;
        try { xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r SYSTEM 'missing.dtd'><r/>"))); }
        catch (java.io.FileNotFoundException e) { fnf = true; }
        check(calls.contains("resolve4(null,missing.dtd)"), "external DTD resolution goes through the 4-arg resolveEntity");
        check(fnf, "returning null defers to default resolution (missing file -> FileNotFoundException)");
        // 2-arg channel used when resolver is only an EntityResolver (JDK-8 legacy path)
        final java.util.List<String> calls2 = new java.util.ArrayList<String>();
        org.xml.sax.EntityResolver er1 = new org.xml.sax.EntityResolver() {
            public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) { calls2.add("resolve2(" + systemId + ")"); return null; }
        };
        org.xml.sax.XMLReader xr2 = spf.newSAXParser().getXMLReader();
        xr2.setEntityResolver(er1);
        check(xr2.getEntityResolver() == er1, "setEntityResolver/getEntityResolver round-trip");
        boolean fnf2 = false;
        try { xr2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r SYSTEM 'missing.dtd'><r/>"))); }
        catch (java.io.FileNotFoundException e) { fnf2 = true; }
        check(fnf2, "plain EntityResolver null return also defers to default resolution");
    }
}
