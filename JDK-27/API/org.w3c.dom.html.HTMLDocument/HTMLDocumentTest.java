/*
 * @test
 * @summary org.w3c.dom.html.HTMLDocument conformance: Document subtype.
 * @run main HTMLDocumentTest
 */

public class HTMLDocumentTest {
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
        Class<?> k = L("org.w3c.dom.html.HTMLDocument");
        check(k.isInterface(), "org.w3c.dom.html.HTMLDocument is an interface");
        check(L("org.w3c.dom.Document").isAssignableFrom(k), "HTMLDocument extends Document");
        check(k.getInterfaces().length >= 1, "HTMLDocument has superinterface");
        check(k.getMethod("getTitle") != null, "getTitle()");
        check(k.getMethod("setTitle", String.class) != null, "setTitle(String)");
        check(k.getMethod("getBody") != null, "getBody()");
        check(k.getMethod("setBody", L("org.w3c.dom.html.HTMLElement")) != null, "setBody(org.w3c.dom.html.HTMLElement)");
        check(k.getMethod("getImages") != null, "getImages()");
        check(k.getMethod("getLinks") != null, "getLinks()");
        check(k.getMethod("getCookie") != null, "getCookie()");
        check(k.getMethod("open") != null, "open()");
        check(k.getMethod("write", String.class) != null, "write(String)");
        check(k.getMethod("getElementsByName", String.class) != null, "getElementsByName(String)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
