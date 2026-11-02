/*
 * @test
 * @summary org.w3c.dom.ls.LSLoadEvent conformance: load event.
 * @run main LSLoadEventTest
 */

public class LSLoadEventTest {
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
        Class<?> k = L("org.w3c.dom.ls.LSLoadEvent");
        check(k.isInterface(), "org.w3c.dom.ls.LSLoadEvent is an interface");
        check(L("org.w3c.dom.events.Event").isAssignableFrom(k), "LSLoadEvent extends Event");
        check(k.getInterfaces().length >= 1, "LSLoadEvent has superinterface");
        check(k.getMethod("getNewDocument") != null, "getNewDocument()");
        check(k.getMethod("getInput") != null, "getInput()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
