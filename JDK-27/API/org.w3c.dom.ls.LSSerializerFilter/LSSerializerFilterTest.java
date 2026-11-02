/*
 * @test
 * @summary org.w3c.dom.ls.LSSerializerFilter conformance: serializer filter.
 * @run main LSSerializerFilterTest
 */

public class LSSerializerFilterTest {
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
        Class<?> k = L("org.w3c.dom.ls.LSSerializerFilter");
        check(k.isInterface(), "org.w3c.dom.ls.LSSerializerFilter is an interface");
        check(L("org.w3c.dom.traversal.NodeFilter").isAssignableFrom(k), "LSSerializerFilter extends NodeFilter");
        check(k.getInterfaces().length >= 1, "LSSerializerFilter has superinterface");
        check(k.getMethod("getWhatToShow") != null, "getWhatToShow()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
