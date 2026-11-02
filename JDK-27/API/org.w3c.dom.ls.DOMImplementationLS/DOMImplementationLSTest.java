/*
 * @test
 * @summary org.w3c.dom.ls.DOMImplementationLS conformance: LS factory.
 * @run main DOMImplementationLSTest
 */

public class DOMImplementationLSTest {
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
        Class<?> k = L("org.w3c.dom.ls.DOMImplementationLS");
        check(k.isInterface(), "org.w3c.dom.ls.DOMImplementationLS is an interface");
        check(k.getInterfaces().length == 0, "DOMImplementationLS declares no superinterface");
        check(sconst("org.w3c.dom.ls.DOMImplementationLS", "MODE_SYNCHRONOUS") == 1, "MODE_SYNCHRONOUS == 1");
        check(sconst("org.w3c.dom.ls.DOMImplementationLS", "MODE_ASYNCHRONOUS") == 2, "MODE_ASYNCHRONOUS == 2");
        check(k.getMethod("createLSParser", short.class, String.class) != null, "createLSParser(short, String)");
        check(k.getMethod("createLSSerializer") != null, "createLSSerializer()");
        check(k.getMethod("createLSInput") != null, "createLSInput()");
        check(k.getMethod("createLSOutput") != null, "createLSOutput()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
