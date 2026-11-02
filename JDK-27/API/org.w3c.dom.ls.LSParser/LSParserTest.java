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

    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
