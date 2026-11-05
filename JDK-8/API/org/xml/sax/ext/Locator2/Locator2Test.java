/*
 * @test
 * @summary org.xml.sax.ext.Locator2 conformance: interface hierarchy and methods.
 * @run main Locator2Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class Locator2Test {
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
        Class<?> c = Class.forName("org.xml.sax.ext.Locator2");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.Locator"), "implements org.xml.sax.Locator");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getXMLVersion"), "has method getXMLVersion");
        check(names.contains("getEncoding"), "has method getEncoding");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Locator2 contract (version/encoding from a live parse) ----
    static void deepContract() throws Exception {
        check(org.xml.sax.Locator.class.isAssignableFrom(org.xml.sax.ext.Locator2.class), "Locator2 extends Locator");
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        final org.xml.sax.Locator[] loc = new org.xml.sax.Locator[1];
        final String[] verAtLocator = new String[]{null};
        final String[] verAtElement = new String[]{null};
        final String[] encAtElement = new String[]{null};
        xr.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) {
                loc[0] = l;
                if (l instanceof org.xml.sax.ext.Locator2) verAtLocator[0] = ((org.xml.sax.ext.Locator2) l).getXMLVersion();
            }
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) {
                if (loc[0] instanceof org.xml.sax.ext.Locator2) {
                    verAtElement[0] = ((org.xml.sax.ext.Locator2) loc[0]).getXMLVersion();
                    encAtElement[0] = ((org.xml.sax.ext.Locator2) loc[0]).getEncoding();
                }
            }
        });
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<?xml version='1.1'?><r/>")));
        check(loc[0] instanceof org.xml.sax.ext.Locator2, "JAXP reader hands out a Locator2");
        check("1.0".equals(verAtLocator[0]), "at setDocumentLocator time version still reports 1.0");
        check("1.1".equals(verAtElement[0]), "at first element the version reflects the declaration (1.1)");
        check(encAtElement[0] == null, "encoding null for a character-stream source");
    }
}
