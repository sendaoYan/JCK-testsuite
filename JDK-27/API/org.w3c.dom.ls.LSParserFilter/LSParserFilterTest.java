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

    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
