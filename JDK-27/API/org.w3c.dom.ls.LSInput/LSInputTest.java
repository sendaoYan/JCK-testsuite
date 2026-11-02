/*
 * @test
 * @summary org.w3c.dom.ls.LSInput conformance: input source.
 * @run main LSInputTest
 */

public class LSInputTest {
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
        Class<?> k = L("org.w3c.dom.ls.LSInput");
        check(k.isInterface(), "org.w3c.dom.ls.LSInput is an interface");
        check(k.getInterfaces().length == 0, "LSInput declares no superinterface");
        check(k.getMethod("getCharacterStream") != null, "getCharacterStream()");
        check(k.getMethod("setCharacterStream", L("java.io.Reader")) != null, "setCharacterStream(java.io.Reader)");
        check(k.getMethod("getByteStream") != null, "getByteStream()");
        check(k.getMethod("setByteStream", L("java.io.InputStream")) != null, "setByteStream(java.io.InputStream)");
        check(k.getMethod("getStringData") != null, "getStringData()");
        check(k.getMethod("setStringData", String.class) != null, "setStringData(String)");
        check(k.getMethod("getSystemId") != null, "getSystemId()");
        check(k.getMethod("getPublicId") != null, "getPublicId()");
        check(k.getMethod("getBaseURI") != null, "getBaseURI()");
        check(k.getMethod("getEncoding") != null, "getEncoding()");
        check(k.getMethod("getCertifiedText") != null, "getCertifiedText()");
        check(k.getMethod("setCertifiedText", boolean.class) != null, "setCertifiedText(boolean)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
