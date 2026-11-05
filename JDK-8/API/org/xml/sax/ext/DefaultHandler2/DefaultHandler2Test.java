/*
 * @test
 * @summary org.xml.sax.ext.DefaultHandler2 conformance: default handler2.
 * @run main DefaultHandler2Test
 */


public class DefaultHandler2Test {
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
        org.xml.sax.ext.DefaultHandler2 h = new org.xml.sax.ext.DefaultHandler2();
        check(h instanceof org.xml.sax.ext.LexicalHandler, "is LexicalHandler");
        check(h instanceof org.xml.sax.ext.DeclHandler, "is DeclHandler");
        check(h instanceof org.xml.sax.ext.EntityResolver2, "is EntityResolver2");
        h.startCDATA();
        h.endCDATA();
        h.comment(new char[]{'-', '-'}, 0, 2);
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DefaultHandler2 contract (combined extension base) ----
    static void deepContract() throws Exception {
        org.xml.sax.ext.DefaultHandler2 h2 = new org.xml.sax.ext.DefaultHandler2();
        check(org.xml.sax.helpers.DefaultHandler.class.isInstance(h2), "DefaultHandler2 extends DefaultHandler");
        check(org.xml.sax.ext.LexicalHandler.class.isInstance(h2), "DefaultHandler2 implements LexicalHandler");
        check(org.xml.sax.ext.DeclHandler.class.isInstance(h2), "DefaultHandler2 implements DeclHandler");
        check(org.xml.sax.ext.EntityResolver2.class.isInstance(h2), "DefaultHandler2 implements EntityResolver2");
        check(h2.getExternalSubset("r", null) == null, "getExternalSubset default returns null");
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setContentHandler(h2);
        xr.setProperty("http://xml.org/sax/properties/lexical-handler", h2);
        xr.setProperty("http://xml.org/sax/properties/declaration-handler", h2);
        xr.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!ELEMENT r (#PCDATA)><!ATTLIST r a CDATA #IMPLIED>]><r><!--c--></r>")));
        check(true, "bare DefaultHandler2 parses a DTD+comment document silently (all roles no-op)");
        // subclass records comment / startDTD / attributeDecl / startCDATA simultaneously
        final java.util.List<String> log = new java.util.ArrayList<String>();
        org.xml.sax.ext.DefaultHandler2 rec = new org.xml.sax.ext.DefaultHandler2() {
            public void comment(char[] c, int s, int n) { log.add("comment(" + new String(c, s, n) + ")"); }
            public void startDTD(String name, String pid, String sid) { log.add("startDTD(" + name + ")"); }
            public void attributeDecl(String e, String a, String t, String m, String v) { log.add("attDecl(" + a + ")"); }
            public void startCDATA() { log.add("startCDATA"); }
            public void endCDATA() { log.add("endCDATA"); }
        };
        org.xml.sax.XMLReader xr2 = spf.newSAXParser().getXMLReader();
        xr2.setContentHandler(rec);
        xr2.setProperty("http://xml.org/sax/properties/lexical-handler", rec);
        xr2.setProperty("http://xml.org/sax/properties/declaration-handler", rec);
        xr2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r [<!ELEMENT r (#PCDATA)><!ATTLIST r a CDATA #IMPLIED>]><r><!--c--><![CDATA[cd]]></r>")));
        check(log.contains("startDTD(r)"), "one instance receives startDTD via the lexical slot");
        check(log.contains("attDecl(a)"), "same instance receives attributeDecl via the declaration slot");
        check(log.contains("comment(c)"), "same instance receives comments via the lexical slot");
        check(log.contains("startCDATA") && log.contains("endCDATA"), "CDATA section brackets reported via the lexical slot");
        int order = log.indexOf("startDTD(r)");
        check(order < log.indexOf("attDecl(a)") && log.indexOf("attDecl(a)") < log.indexOf("startCDATA"), "event order startDTD -> attributeDecl -> startCDATA preserved");
    }
}
