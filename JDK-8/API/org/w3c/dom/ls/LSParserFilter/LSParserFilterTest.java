/*
 * @test
 * @summary org.w3c.dom.ls.LSParserFilter conformance: reflection shape checks.
 * @run main LSParserFilterTest
 */

import java.lang.reflect.*;

public class LSParserFilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.ls.LSParserFilter");
        check(c != null, "org.w3c.dom.ls.LSParserFilter loads");
        check(c.getName().equals("org.w3c.dom.ls.LSParserFilter"), "binary name is org.w3c.dom.ls.LSParserFilter");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSParserFilter is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSParserFilter is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "startElement"), "declares startElement");
        check(declares(c, "acceptNode"), "declares acceptNode");
        check(declares(c, "getWhatToShow"), "declares getWhatToShow");
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
