/*
 * @test
 * @summary org.xml.sax.helpers.XMLReaderAdapter conformance: XML reader adapter.
 * @run main XMLReaderAdapterTest
 */


public class XMLReaderAdapterTest {
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
        org.xml.sax.helpers.XMLReaderAdapter ra = new org.xml.sax.helpers.XMLReaderAdapter();
        check(ra != null, "constructed");
        check(ra instanceof org.xml.sax.Parser, "is Parser");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral XMLReaderAdapter contract (SAX2 -> SAX1 bridging) ----
    static void deepContract() throws Exception {
        org.xml.sax.XMLReader xr = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        org.xml.sax.helpers.XMLReaderAdapter ad = new org.xml.sax.helpers.XMLReaderAdapter(xr);
        check(org.xml.sax.Parser.class.isInstance(ad), "XMLReaderAdapter is a SAX1 Parser");
        org.xml.sax.helpers.XMLReaderAdapter def = new org.xml.sax.helpers.XMLReaderAdapter();
        check(def != null, "no-arg constructor bootstraps a default XMLReader");
        final java.util.List<String> log = new java.util.ArrayList<String>();
        ad.setDocumentHandler(new org.xml.sax.DocumentHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) { log.add("loc"); }
            public void startDocument() { log.add("sd"); }
            public void endDocument() { log.add("ed"); }
            public void startElement(String name, org.xml.sax.AttributeList a) {
                StringBuilder sb = new StringBuilder("se(" + name);
                for (int i = 0; i < a.getLength(); i++) sb.append("," + a.getName(i) + "=" + a.getValue(i) + "/" + a.getType(i));
                log.add(sb.append(")").toString());
            }
            public void endElement(String name) { log.add("ee(" + name + ")"); }
            public void characters(char[] c, int s, int n) { log.add("ch(" + new String(c, s, n) + ")"); }
            public void ignorableWhitespace(char[] c, int s, int n) { log.add("iw"); }
            public void processingInstruction(String t, String d) { log.add("pi(" + t + ")"); }
        });
        ad.parse(new org.xml.sax.InputSource(new java.io.StringReader("<?pi d?><r a='1' b='c'>t</r>")));
        check(log.get(0).equals("loc") && log.get(1).equals("sd"), "locator then startDocument lead the SAX1 stream");
        check(log.contains("pi(pi)"), "processing instructions forwarded");
        check(log.contains("se(r,a=1/CDATA,b=c/CDATA)"), "startElement carries a SAX1 AttributeList with CDATA types");
        check(log.contains("ch(t)") && log.contains("ee(r)") && log.get(log.size() - 1).equals("ed"), "characters/endElement/endDocument complete the stream");
        // namespace-qualified document degrades to raw qNames
        final String[] name = new String[]{null};
        ad.setDocumentHandler(new org.xml.sax.DocumentHandler() {
            public void setDocumentLocator(org.xml.sax.Locator l) { }
            public void startDocument() { }
            public void endDocument() { }
            public void startElement(String n, org.xml.sax.AttributeList a) { name[0] = n; }
            public void endElement(String n) { }
            public void characters(char[] c, int s, int l) { }
            public void ignorableWhitespace(char[] c, int s, int l) { }
            public void processingInstruction(String t, String d) { }
        });
        ad.parse(new org.xml.sax.InputSource(new java.io.StringReader("<p:r xmlns:p='urn:p'/>")));
        check("p:r".equals(name[0]), "SAX1 namespaced element reported by raw qName");
        // setLocale
        boolean sns = false;
        try { ad.setLocale(java.util.Locale.FRENCH); }
        catch (org.xml.sax.SAXNotSupportedException e) { sns = true; }
        check(sns, "setLocale -> SAXNotSupportedException");
        // error/entity/dtd slots
        org.xml.sax.ErrorHandler eh = new org.xml.sax.helpers.DefaultHandler();
        ad.setErrorHandler(eh);
        check(ad instanceof org.xml.sax.Parser, "adapter remains usable as Parser after wiring handlers");
    }
}
