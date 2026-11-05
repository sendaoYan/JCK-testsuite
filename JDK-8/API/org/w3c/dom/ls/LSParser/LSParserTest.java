/*
 * @test
 * @summary org.w3c.dom.ls.LSParser conformance: reflection shape checks.
 * @run main LSParserTest
 */

import java.lang.reflect.*;

public class LSParserTest {
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
        Class<?> c = Class.forName("org.w3c.dom.ls.LSParser");
        check(c != null, "org.w3c.dom.ls.LSParser loads");
        check(c.getName().equals("org.w3c.dom.ls.LSParser"), "binary name is org.w3c.dom.ls.LSParser");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.LSParser is public");
        check(c.isInterface(), "org.w3c.dom.ls.LSParser is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getDomConfig"), "declares getDomConfig");
        check(declares(c, "getFilter"), "declares getFilter");
        check(declares(c, "setFilter"), "declares setFilter");
        check(declares(c, "getAsync"), "declares getAsync");
        check(declares(c, "getBusy"), "declares getBusy");
        check(declares(c, "parse"), "declares parse");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral LSParser contract ----
    static void deepContract() throws Exception {
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument().getImplementation().getFeature("LS", "3.0");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        // ACTION_* constants
        check(org.w3c.dom.ls.LSParser.ACTION_APPEND_AS_CHILDREN == 1 && org.w3c.dom.ls.LSParser.ACTION_REPLACE_CHILDREN == 2, "ACTION_APPEND_AS_CHILDREN=1 ACTION_REPLACE_CHILDREN=2");
        check(org.w3c.dom.ls.LSParser.ACTION_INSERT_BEFORE == 3 && org.w3c.dom.ls.LSParser.ACTION_INSERT_AFTER == 4, "ACTION_INSERT_BEFORE=3 ACTION_INSERT_AFTER=4");
        check(org.w3c.dom.ls.LSParser.ACTION_REPLACE == 5, "ACTION_REPLACE=5");
        check(!p.getBusy() && !p.getAsync(), "synchronous parser: busy=false async=false");
        org.w3c.dom.ls.LSInput in = ls.createLSInput();
        in.setStringData("<r><a x='1'>T</a><b/></r>");
        org.w3c.dom.Document pd = p.parse(in);
        check(pd != null && pd.getDocumentElement().getChildNodes().getLength() == 2, "parse builds the full tree");
        // parser is reusable
        in.setStringData("<second/>");
        check("second".equals(p.parse(in).getDocumentElement().getNodeName()), "parser reusable for a second document");
        // parseWithContext is not supported by this implementation
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document ctx = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element host = ctx.createElement("host"); ctx.appendChild(host);
        org.w3c.dom.ls.LSInput pin = ls.createLSInput(); pin.setStringData("<a/>");
        try { p.parseWithContext(pin, host, org.w3c.dom.ls.LSParser.ACTION_APPEND_AS_CHILDREN); check(false, "parseWithContext must throw here"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "parseWithContext -> NOT_SUPPORTED_ERR(9)"); }
        check(host.getChildNodes().getLength() == 0, "failed parseWithContext leaves the context node untouched");
        // null input
        try { p.parse(null); check(false, "parse(null) must throw"); }
        catch (NullPointerException expected) { check(true, "parse(null) -> NullPointerException (type only)"); }
        // DomConfig channel
        check(p.getDomConfig() != null && p.getDomConfig().canSetParameter("comments", Boolean.FALSE), "parser DomConfig accepts comments=false");
        p.getDomConfig().setParameter("comments", Boolean.FALSE);
        org.w3c.dom.ls.LSInput cin = ls.createLSInput(); cin.setStringData("<r><!--c--><a/></r>");
        check(p.parse(cin).getDocumentElement().getChildNodes().getLength() == 1, "comments=false strips comments during parse");
        // filter channel
        org.w3c.dom.ls.LSParserFilter noFilter = p.getFilter();
        org.w3c.dom.ls.LSParserFilter flt = new org.w3c.dom.ls.LSParserFilter() {
            public short startElement(org.w3c.dom.Element e) { return org.w3c.dom.ls.LSParserFilter.FILTER_ACCEPT; }
            public short acceptNode(org.w3c.dom.Node n) { return org.w3c.dom.ls.LSParserFilter.FILTER_ACCEPT; }
            public int getWhatToShow() { return org.w3c.dom.traversal.NodeFilter.SHOW_ALL; }
        };
        p.setFilter(flt);
        check(p.getFilter() == flt && noFilter == null, "setFilter/getFilter round-trip (default null)");
    }
}
