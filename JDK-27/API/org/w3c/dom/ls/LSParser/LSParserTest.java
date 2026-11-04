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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
