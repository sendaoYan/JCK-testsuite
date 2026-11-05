/*
 * @test
 * @summary org.xml.sax.Locator conformance: interface hierarchy and methods.
 * @run main LocatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class LocatorTest {
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
        Class<?> c = Class.forName("org.xml.sax.Locator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPublicId"), "has method getPublicId");
        check(names.contains("getSystemId"), "has method getSystemId");
        check(names.contains("getLineNumber"), "has method getLineNumber");
        check(names.contains("getColumnNumber"), "has method getColumnNumber");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Locator contract (live positions) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final org.xml.sax.Locator[] loc = new org.xml.sax.Locator[1];
        final int[] lineAtSecondDoc = new int[]{-1};
        final int[] colAtSecondDoc = new int[]{-1};
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) { loc[0] = l; }
            public void startElement(String u, String l, String q, org.xml.sax.Attributes a) {
                if (lineAtSecondDoc[0] < 0 && "a".equals(q)) { lineAtSecondDoc[0] = loc[0].getLineNumber(); colAtSecondDoc[0] = loc[0].getColumnNumber(); }
            }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r>\n<a/></r>")));
        check(loc[0] != null, "setDocumentLocator delivers a live Locator");
        check(lineAtSecondDoc[0] == 2, "line number advances (element on line 2 reports 2)");
        check(colAtSecondDoc[0] > 0, "column number positive");
        check(loc[0].getPublicId() == null && loc[0].getSystemId() == null, "string ids default null for an anonymous stream");
        check(org.xml.sax.Locator.class.isInterface(), "Locator is an interface");
    }
    static int l0(org.xml.sax.Locator l) { return l.getLineNumber(); }
}
