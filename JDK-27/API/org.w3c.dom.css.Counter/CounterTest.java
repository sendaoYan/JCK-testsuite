/*
 * @test
 * @summary org.w3c.dom.css.Counter conformance: counter values.
 * @run main CounterTest
 */

public class CounterTest {
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
        Class<?> k = L("org.w3c.dom.css.Counter");
        check(k.isInterface(), "org.w3c.dom.css.Counter is an interface");
        check(k.getInterfaces().length == 0, "Counter declares no superinterface");
        check(k.getMethod("getIdentifier") != null, "getIdentifier()");
        check(k.getMethod("getListStyle") != null, "getListStyle()");
        check(k.getMethod("getSeparator") != null, "getSeparator()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
