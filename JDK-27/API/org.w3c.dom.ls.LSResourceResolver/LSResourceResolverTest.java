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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.ls.LSResourceResolver");
        check(k.isInterface(), "org.w3c.dom.ls.LSResourceResolver is an interface");
        check(k.getInterfaces().length == 0, "LSResourceResolver declares no superinterface");
        check(k.getMethod("resolveResource", String.class, String.class, String.class, String.class, String.class) != null, "resolveResource(String, String, String, String, String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ls.LSResourceResolver a = new org.w3c.dom.ls.LSResourceResolver(){
            public org.w3c.dom.ls.LSInput resolveResource(java.lang.String p0, java.lang.String p1, java.lang.String p2, java.lang.String p3, java.lang.String p4) { return null; }
        };
        check(a != null, "anon impl of LSResourceResolver");
        check(a.resolveResource("s", "s", "s", "s", "s") == null, "resolveResource() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
