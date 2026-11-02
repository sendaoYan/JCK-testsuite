/*
 * @test
 * @summary org.w3c.dom.ls.LSResourceResolver conformance: resource resolver.
 * @run main LSResourceResolverTest
 */

public class LSResourceResolverTest {
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
        Class<?> k = L("org.w3c.dom.ls.LSResourceResolver");
        check(k.isInterface(), "org.w3c.dom.ls.LSResourceResolver is an interface");
        check(k.getInterfaces().length == 0, "LSResourceResolver declares no superinterface");
        check(k.getMethod("resolveResource", String.class, String.class, String.class, String.class, String.class) != null, "resolveResource(String, String, String, String, String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
