/*
 * @test
 * @summary org.xml.sax.helpers.XMLReaderFactory conformance: XML reader factory.
 * @run main XMLReaderFactoryTest
 */


public class XMLReaderFactoryTest {
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
        org.xml.sax.XMLReader r = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        check(r != null, "reader non-null");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral XMLReaderFactory contract (driver bootstrap) ----
    static void deepContract() throws Exception {
        org.xml.sax.XMLReader r = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        check(r != null && org.xml.sax.XMLReader.class.isInstance(r), "createXMLReader() bootstraps the built-in Xerces driver (got " + (r == null ? "null" : r.getClass().getName()) + ")");
        check("com.sun.org.apache.xerces.internal.parsers.SAXParser".equals(r.getClass().getName()), "default driver is com.sun.org.apache.xerces.internal.parsers.SAXParser");
        boolean npe = false;
        try { org.xml.sax.helpers.XMLReaderFactory.createXMLReader(null); }
        catch (NullPointerException e) { npe = true; }
        catch (org.xml.sax.SAXException e) { npe = false; }
        check(npe, "createXMLReader(null) -> NullPointerException");
        // org.xml.sax.driver property routes creation
        String prev = System.setProperty("org.xml.sax.driver", "com.sun.org.apache.xerces.internal.parsers.SAXParser");
        try {
            org.xml.sax.XMLReader viaProp = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
            check("com.sun.org.apache.xerces.internal.parsers.SAXParser".equals(viaProp.getClass().getName()), "org.xml.sax.driver property is honored");
        } finally {
            if (prev == null) System.clearProperty("org.xml.sax.driver"); else System.setProperty("org.xml.sax.driver", prev);
        }
        // the created reader is fully operational
        final boolean[] sd = new boolean[]{false};
        r.setContentHandler(new org.xml.sax.helpers.DefaultHandler() { public void startDocument() { sd[0] = true; } });
        r.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r/>")));
        check(sd[0], "factory-created reader parses a document end to end");
    }
}
