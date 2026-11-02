/*
 * @test
 * @summary org.w3c.dom.ls.LSProgressEvent conformance: progress event.
 * @run main LSProgressEventTest
 */

public class LSProgressEventTest {
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
        Class<?> k = L("org.w3c.dom.ls.LSProgressEvent");
        check(k.isInterface(), "org.w3c.dom.ls.LSProgressEvent is an interface");
        check(L("org.w3c.dom.events.Event").isAssignableFrom(k), "LSProgressEvent extends Event");
        check(k.getInterfaces().length >= 1, "LSProgressEvent has superinterface");
        check(k.getMethod("getInput") != null, "getInput()");
        check(k.getMethod("getPosition") != null, "getPosition()");
        check(k.getMethod("getTotalSize") != null, "getTotalSize()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
