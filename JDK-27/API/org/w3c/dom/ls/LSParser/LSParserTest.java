/*
 * @test
 * @summary org.w3c.dom.ls.LSParser conformance: parser.
 * @run main LSParserTest
 */

public class LSParserTest {
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

        Class<?> k = L("org.w3c.dom.ls.LSParser");
        check(k.isInterface(), "org.w3c.dom.ls.LSParser is an interface");
        check(k.getInterfaces().length == 0, "LSParser declares no superinterface");
        check(sconst("org.w3c.dom.ls.LSParser", "ACTION_APPEND_AS_CHILDREN") == 1, "ACTION_APPEND_AS_CHILDREN == 1");
        check(sconst("org.w3c.dom.ls.LSParser", "ACTION_REPLACE_CHILDREN") == 2, "ACTION_REPLACE_CHILDREN == 2");
        check(sconst("org.w3c.dom.ls.LSParser", "ACTION_INSERT_BEFORE") == 3, "ACTION_INSERT_BEFORE == 3");
        check(sconst("org.w3c.dom.ls.LSParser", "ACTION_INSERT_AFTER") == 4, "ACTION_INSERT_AFTER == 4");
        check(sconst("org.w3c.dom.ls.LSParser", "ACTION_REPLACE") == 5, "ACTION_REPLACE == 5");
        check(k.getMethod("getDomConfig") != null, "getDomConfig()");
        check(k.getMethod("getFilter") != null, "getFilter()");
        check(k.getMethod("setFilter", L("org.w3c.dom.ls.LSParserFilter")) != null, "setFilter(org.w3c.dom.ls.LSParserFilter)");
        check(k.getMethod("getAsync") != null, "getAsync()");
        check(k.getMethod("getBusy") != null, "getBusy()");
        check(k.getMethod("parse", L("org.w3c.dom.ls.LSInput")) != null, "parse(org.w3c.dom.ls.LSInput)");
        check(k.getMethod("parseURI", String.class) != null, "parseURI(String)");
        check(k.getMethod("parseWithContext", L("org.w3c.dom.ls.LSInput"), L("org.w3c.dom.Node"), short.class) != null, "parseWithContext(org.w3c.dom.ls.LSInput, org.w3c.dom.Node, short)");
        check(k.getMethod("abort") != null, "abort()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.ls.LSParser.ACTION_APPEND_AS_CHILDREN == 1, "ACTION_APPEND_AS_CHILDREN == 1");
        check(org.w3c.dom.ls.LSParser.ACTION_REPLACE_CHILDREN == 2, "ACTION_REPLACE_CHILDREN == 2");
        check(org.w3c.dom.ls.LSParser.ACTION_INSERT_BEFORE == 3, "ACTION_INSERT_BEFORE == 3");
        check(org.w3c.dom.ls.LSParser.ACTION_INSERT_AFTER == 4, "ACTION_INSERT_AFTER == 4");
        check(org.w3c.dom.ls.LSParser.ACTION_REPLACE == 5, "ACTION_REPLACE == 5");
        org.w3c.dom.ls.LSParser a = new org.w3c.dom.ls.LSParser(){
            public org.w3c.dom.DOMConfiguration getDomConfig() { return null; }
            public org.w3c.dom.ls.LSParserFilter getFilter() { return null; }
            public void setFilter(org.w3c.dom.ls.LSParserFilter p0) { }
            public boolean getAsync() { return true; }
            public boolean getBusy() { return true; }
            public org.w3c.dom.Document parse(org.w3c.dom.ls.LSInput p0) { return null; }
            public org.w3c.dom.Document parseURI(java.lang.String p0) { return null; }
            public org.w3c.dom.Node parseWithContext(org.w3c.dom.ls.LSInput p0, org.w3c.dom.Node p1, short p2) { return null; }
            public void abort() { }
        };
        check(a != null, "anon impl of LSParser");
        check(a.getDomConfig() == null, "getDomConfig() returns fixed default");
        check(a.getFilter() == null, "getFilter() returns fixed default");
        a.setFilter((org.w3c.dom.ls.LSParserFilter) null);
        check(a.getAsync() == true, "getAsync() returns fixed default");
        check(a.getBusy() == true, "getBusy() returns fixed default");
        check(a.parse((org.w3c.dom.ls.LSInput) null) == null, "parse() returns fixed default");
        check(a.parseURI("s") == null, "parseURI() returns fixed default");
        check(a.parseWithContext((org.w3c.dom.ls.LSInput) null, (org.w3c.dom.Node) null, (short)2) == null, "parseWithContext() returns fixed default");
        a.abort();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
