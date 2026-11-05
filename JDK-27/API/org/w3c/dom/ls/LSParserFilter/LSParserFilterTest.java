/*
 * @test
 * @summary org.w3c.dom.ls.LSParserFilter conformance: parser filter.
 * @run main LSParserFilterTest
 */

public class LSParserFilterTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.ls.LSParserFilter");
        check(k.isInterface(), "org.w3c.dom.ls.LSParserFilter is an interface");
        check(k.getInterfaces().length == 0, "LSParserFilter declares no superinterface");
        check(sconst("org.w3c.dom.ls.LSParserFilter", "FILTER_ACCEPT") == 1, "FILTER_ACCEPT == 1");
        check(sconst("org.w3c.dom.ls.LSParserFilter", "FILTER_REJECT") == 2, "FILTER_REJECT == 2");
        check(sconst("org.w3c.dom.ls.LSParserFilter", "FILTER_SKIP") == 3, "FILTER_SKIP == 3");
        check(sconst("org.w3c.dom.ls.LSParserFilter", "FILTER_INTERRUPT") == 4, "FILTER_INTERRUPT == 4");
        check(k.getMethod("startElement", L("org.w3c.dom.Element")) != null, "startElement(org.w3c.dom.Element)");
        check(k.getMethod("acceptNode", L("org.w3c.dom.Node")) != null, "acceptNode(org.w3c.dom.Node)");
        check(k.getMethod("getWhatToShow") != null, "getWhatToShow()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.ls.LSParserFilter.FILTER_ACCEPT == 1, "FILTER_ACCEPT == 1");
        check(org.w3c.dom.ls.LSParserFilter.FILTER_REJECT == 2, "FILTER_REJECT == 2");
        check(org.w3c.dom.ls.LSParserFilter.FILTER_SKIP == 3, "FILTER_SKIP == 3");
        check(org.w3c.dom.ls.LSParserFilter.FILTER_INTERRUPT == 4, "FILTER_INTERRUPT == 4");
        org.w3c.dom.ls.LSParserFilter a = new org.w3c.dom.ls.LSParserFilter(){
            public short startElement(org.w3c.dom.Element p0) { return (short)2; }
            public short acceptNode(org.w3c.dom.Node p0) { return (short)2; }
            public int getWhatToShow() { return 3; }
        };
        check(a != null, "anon impl of LSParserFilter");
        check(a.startElement((org.w3c.dom.Element) null) == (short)2, "startElement() returns fixed default");
        check(a.acceptNode((org.w3c.dom.Node) null) == (short)2, "acceptNode() returns fixed default");
        check(a.getWhatToShow() == 3, "getWhatToShow() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LSParserFilter contract ----
    static void deepContract() throws Exception {
        check(org.w3c.dom.ls.LSParserFilter.FILTER_ACCEPT == 1, "FILTER_ACCEPT = 1");
        check(org.w3c.dom.ls.LSParserFilter.FILTER_REJECT == 2, "FILTER_REJECT = 2");
        check(org.w3c.dom.ls.LSParserFilter.FILTER_SKIP == 3, "FILTER_SKIP = 3");
        check(org.w3c.dom.ls.LSParserFilter.FILTER_INTERRUPT == 4, "FILTER_INTERRUPT = 4");
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument().getImplementation().getFeature("LS", "3.0");
        final int[] verdict = new int[]{org.w3c.dom.ls.LSParserFilter.FILTER_ACCEPT};
        final java.util.List<String> seen = new java.util.ArrayList<String>();
        org.w3c.dom.ls.LSParserFilter flt = new org.w3c.dom.ls.LSParserFilter() {
            public short startElement(org.w3c.dom.Element e) { return (short) verdict[0]; }
            public short acceptNode(org.w3c.dom.Node n) { seen.add(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE ? n.getNodeValue() : n.getNodeName()); return (short) verdict[0]; }
            public int getWhatToShow() { return org.w3c.dom.traversal.NodeFilter.SHOW_ALL; }
        };
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        p.setFilter(flt);
        org.w3c.dom.ls.LSInput in = ls.createLSInput(); in.setStringData("<r><a>TA</a><b/></r>");
        org.w3c.dom.Document fd = p.parse(in);
        check(fd.getDocumentElement().getChildNodes().getLength() == 2, "FILTER_ACCEPT keeps everything");
        check(seen.contains("TA") && seen.contains("a") && seen.contains("b"), "acceptNode consults text and elements");
        // REJECT prunes every element from the result (root itself survives)
        verdict[0] = org.w3c.dom.ls.LSParserFilter.FILTER_REJECT;
        org.w3c.dom.ls.LSParser pr = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        pr.setFilter(flt);
        org.w3c.dom.ls.LSInput rin = ls.createLSInput(); rin.setStringData("<r><a>TA</a><b/></r>");
        org.w3c.dom.Document rd = pr.parse(rin);
        check("r".equals(rd.getDocumentElement().getNodeName()) && rd.getDocumentElement().getChildNodes().getLength() == 0, "FILTER_REJECT drops all filtered children (root kept)");
        // INTERRUPT halts parsing
        verdict[0] = org.w3c.dom.ls.LSParserFilter.FILTER_INTERRUPT;
        org.w3c.dom.ls.LSParser pi = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        pi.setFilter(flt);
        org.w3c.dom.ls.LSInput iin = ls.createLSInput(); iin.setStringData("<r><a>TA</a><b/></r>");
        org.w3c.dom.Document idoc = pi.parse(iin);
        check(idoc.getDocumentElement() != null && idoc.getDocumentElement().getChildNodes().getLength() == 0, "FILTER_INTERRUPT stops the build early (empty root)");
        // getWhatToShow influences which nodes reach the filter
        org.w3c.dom.ls.LSParserFilter elemOnly = new org.w3c.dom.ls.LSParserFilter() {
            public short startElement(org.w3c.dom.Element e) { return org.w3c.dom.ls.LSParserFilter.FILTER_ACCEPT; }
            public short acceptNode(org.w3c.dom.Node n) { seen.add("mask:" + n.getNodeName()); return org.w3c.dom.ls.LSParserFilter.FILTER_ACCEPT; }
            public int getWhatToShow() { return org.w3c.dom.traversal.NodeFilter.SHOW_ELEMENT; }
        };
        seen.clear();
        org.w3c.dom.ls.LSParser pm = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        pm.setFilter(elemOnly);
        org.w3c.dom.ls.LSInput min = ls.createLSInput(); min.setStringData("<r><a>TA</a></r>");
        pm.parse(min);
        boolean anyText = false;
        for (String s : seen) if (s.equals("mask:#text") || s.equals("mask:TA")) anyText = true;
        check(!anyText && seen.contains("mask:a"), "whatToShow=SHOW_ELEMENT keeps text nodes away from acceptNode");
    }
}
