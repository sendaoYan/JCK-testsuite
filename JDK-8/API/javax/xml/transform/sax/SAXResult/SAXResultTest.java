/*
 * @test
 * @summary javax.xml.transform.sax.SAXResult conformance: SAX result.
 * @run main SAXResultTest
 */


public class SAXResultTest {
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
        javax.xml.transform.sax.SAXResult r = new javax.xml.transform.sax.SAXResult();
        check(r.getHandler() == null, "handler null");
        check(r.getLexicalHandler() == null, "lexical handler null");
        r.setSystemId("sysid");
        check("sysid".equals(r.getSystemId()), "systemId");
        testCtorMatrix();
        testTransformToSaxResult();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // deepening: constructor matrix + real transform->SAXResult event closure
    static final String DOC =
        "<root><item id=\"3\" name=\"gamma\">third</item>"
      + "<item id=\"1\" name=\"alpha\">first</item>"
      + "<item id=\"2\" name=\"beta\">second</item></root>";

    static void testCtorMatrix() {
        org.xml.sax.ContentHandler h = new org.xml.sax.helpers.DefaultHandler();
        javax.xml.transform.sax.SAXResult r =
            new javax.xml.transform.sax.SAXResult(h);
        check(r.getHandler() == h, "(ContentHandler) ctor round-trip");
        check(r.getSystemId() == null, "(ContentHandler) ctor systemId null");
        org.xml.sax.ext.LexicalHandler lh = new org.xml.sax.ext.DefaultHandler2();
        r.setLexicalHandler(lh);
        check(r.getLexicalHandler() == lh, "setLexicalHandler/getLexicalHandler round-trip");
        org.xml.sax.ContentHandler h2 = new org.xml.sax.helpers.DefaultHandler();
        r.setHandler(h2);
        check(r.getHandler() == h2, "setHandler/getHandler round-trip");
        check("http://javax.xml.transform.sax.SAXResult/feature"
                  .equals(javax.xml.transform.sax.SAXResult.FEATURE),
              "SAXResult.FEATURE constant value");
    }

    static void testTransformToSaxResult() throws Exception {
        final java.util.List<String> ev = new java.util.ArrayList<String>();
        org.xml.sax.helpers.DefaultHandler rec = new org.xml.sax.helpers.DefaultHandler() {
            public void startDocument() { ev.add("SD"); }
            public void endDocument() { ev.add("ED"); }
            public void startElement(String u, String l, String q, org.xml.sax.Attributes at) {
                ev.add("SE:" + q + "[" + at.getValue("id") + "]");
            }
            public void endElement(String u, String l, String q) { ev.add("EE:" + q); }
            public void characters(char[] ch, int s, int len) {
                ev.add("CH:" + new String(ch, s, len));
            }
        };
        javax.xml.transform.Transformer id =
            javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        id.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(DOC)),
                     new javax.xml.transform.sax.SAXResult(rec));
        java.util.List<String> expected = java.util.Arrays.asList(
            "SD", "SE:root[null]", "SE:item[3]", "CH:third", "EE:item",
            "SE:item[1]", "CH:first", "EE:item",
            "SE:item[2]", "CH:second", "EE:item", "EE:root", "ED");
        check(ev.equals(expected), "identity transform into SAXResult delivers full SAX event sequence");
    }
}
