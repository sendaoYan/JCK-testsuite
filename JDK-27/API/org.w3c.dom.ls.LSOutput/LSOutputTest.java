/*
 * @test
 * @summary org.w3c.dom.ls.LSOutput conformance: output sink.
 * @run main LSOutputTest
 */

public class LSOutputTest {
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
        Class<?> k = L("org.w3c.dom.ls.LSOutput");
        check(k.isInterface(), "org.w3c.dom.ls.LSOutput is an interface");
        check(k.getInterfaces().length == 0, "LSOutput declares no superinterface");
        check(k.getMethod("getCharacterStream") != null, "getCharacterStream()");
        check(k.getMethod("setCharacterStream", L("java.io.Writer")) != null, "setCharacterStream(java.io.Writer)");
        check(k.getMethod("getByteStream") != null, "getByteStream()");
        check(k.getMethod("setByteStream", L("java.io.OutputStream")) != null, "setByteStream(java.io.OutputStream)");
        check(k.getMethod("getSystemId") != null, "getSystemId()");
        check(k.getMethod("getEncoding") != null, "getEncoding()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
