/*
 * @test
 * @summary org.w3c.dom.ls.LSSerializer conformance: serializer.
 * @run main LSSerializerTest
 */

public class LSSerializerTest {
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
        Class<?> k = L("org.w3c.dom.ls.LSSerializer");
        check(k.isInterface(), "org.w3c.dom.ls.LSSerializer is an interface");
        check(k.getInterfaces().length == 0, "LSSerializer declares no superinterface");
        check(k.getMethod("getDomConfig") != null, "getDomConfig()");
        check(k.getMethod("getNewLine") != null, "getNewLine()");
        check(k.getMethod("setNewLine", String.class) != null, "setNewLine(String)");
        check(k.getMethod("getFilter") != null, "getFilter()");
        check(k.getMethod("setFilter", L("org.w3c.dom.ls.LSSerializerFilter")) != null, "setFilter(org.w3c.dom.ls.LSSerializerFilter)");
        check(k.getMethod("write", L("org.w3c.dom.Node"), L("org.w3c.dom.ls.LSOutput")) != null, "write(org.w3c.dom.Node, org.w3c.dom.ls.LSOutput)");
        check(k.getMethod("writeToURI", L("org.w3c.dom.Node"), String.class) != null, "writeToURI(org.w3c.dom.Node, String)");
        check(k.getMethod("writeToString", L("org.w3c.dom.Node")) != null, "writeToString(org.w3c.dom.Node)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
