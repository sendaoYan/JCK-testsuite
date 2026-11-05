/*
 * @test
 * @summary org.xml.sax.helpers.XMLFilterImpl conformance: XML filter impl.
 * @run main XMLFilterImplTest
 */


public class XMLFilterImplTest {
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
        org.xml.sax.helpers.XMLFilterImpl f = new org.xml.sax.helpers.XMLFilterImpl();
        check(f.getParent() == null, "parent null");
        check(f instanceof org.xml.sax.XMLFilter, "is XMLFilter");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral XMLFilterImpl contract (filter chain + selective event forwarding) ----
    static void deepContract() throws Exception {
        org.xml.sax.XMLReader xr = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        org.xml.sax.helpers.XMLFilterImpl f = new org.xml.sax.helpers.XMLFilterImpl(xr);
        check(f.getParent() == xr, "constructor wires the parent reader");
        check(f.getContentHandler() == null && f.getDTDHandler() == null && f.getErrorHandler() == null && f.getEntityResolver() == null, "all handler slots default to null");
        // event forwarding
        final java.util.List<String> log = new java.util.ArrayList<String>();
        f.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { log.add("se(" + q + ")"); }
            public void characters(char[] c, int s, int n) { log.add("ch(" + new String(c, s, n) + ")"); }
            public void endElement(String u, String ln, String q) { log.add("ee(" + q + ")"); }
        });
        f.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r>txt</r>")));
        check(log.equals(java.util.Arrays.asList("se(r)", "ch(txt)", "ee(r)")), "events forwarded parent -> filter -> handler in order");
        // filter-level entity resolver intercepts before the parent resolves
        final String[] resolved = new String[]{null};
        f.setEntityResolver(new org.xml.sax.EntityResolver() {
            public org.xml.sax.InputSource resolveEntity(String p, String s) { resolved[0] = s; return null; }
        });
        boolean fnf = false;
        try { f.parse(new org.xml.sax.InputSource(new java.io.StringReader("<!DOCTYPE r SYSTEM 'missing-x.dtd'><r/>"))); }
        catch (java.io.FileNotFoundException e) { fnf = true; }
        check(resolved[0] != null && resolved[0].endsWith("missing-x.dtd"), "filter entity resolver consulted for the parent's DTD (systemId=" + resolved[0] + ")");
        check(fnf, "null from the filter resolver defers to default resolution");
        // selective filtering: drop element 'drop' by qName
        org.xml.sax.helpers.XMLFilterImpl sel = new org.xml.sax.helpers.XMLFilterImpl(xr) {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) throws org.xml.sax.SAXException { if (!"drop".equals(q)) super.startElement(u, ln, q, a); }
            public void endElement(String u, String ln, String q) throws org.xml.sax.SAXException { if (!"drop".equals(q)) super.endElement(u, ln, q); }
        };
        final java.util.List<String> kept = new java.util.ArrayList<String>();
        sel.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String u, String ln, String q, org.xml.sax.Attributes a) { kept.add(q); }
        });
        sel.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r><keep/><drop/></r>")));
        check(kept.equals(java.util.Arrays.asList("r", "keep")), "subclass suppresses selected elements (kept=" + kept + ")");
        // two-level chain
        org.xml.sax.helpers.XMLFilterImpl f1 = new org.xml.sax.helpers.XMLFilterImpl(xr);
        org.xml.sax.helpers.XMLFilterImpl f2 = new org.xml.sax.helpers.XMLFilterImpl(f1);
        final StringBuilder chain = new StringBuilder();
        f2.setContentHandler(new org.xml.sax.helpers.DefaultHandler() { public void characters(char[] c, int s, int n) { chain.append(new String(c, s, n)); } });
        f2.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r>chain</r>")));
        check("chain".equals(chain.toString()), "two-level filter chain forwards events end to end");
        // orphan filter: parse -> NPE, feature/property access -> SAXNotRecognizedException
        org.xml.sax.helpers.XMLFilterImpl orphan = new org.xml.sax.helpers.XMLFilterImpl();
        check(orphan.getParent() == null, "no-arg constructor leaves parent null");
        boolean orphanNpe = false;
        try { orphan.parse(new org.xml.sax.InputSource(new java.io.StringReader("<r/>"))); }
        catch (NullPointerException e) { orphanNpe = true; }
        check(orphanNpe, "orphan parse -> NullPointerException");
        boolean snr1 = false, snr2 = false, snr3 = false;
        try { orphan.setFeature("http://xml.org/sax/features/namespaces", true); } catch (org.xml.sax.SAXNotRecognizedException e) { snr1 = true; }
        try { orphan.getFeature("http://xml.org/sax/features/namespaces"); } catch (org.xml.sax.SAXNotRecognizedException e) { snr2 = true; }
        try { orphan.setProperty("http://xml.org/sax/properties/lexical-handler", new Object()); } catch (org.xml.sax.SAXNotRecognizedException e) { snr3 = true; }
        check(snr1 && snr2 && snr3, "orphan feature/property access -> SAXNotRecognizedException");
    }
}
