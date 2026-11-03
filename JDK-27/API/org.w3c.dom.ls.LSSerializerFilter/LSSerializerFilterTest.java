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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.ls.LSSerializerFilter");
        check(k.isInterface(), "org.w3c.dom.ls.LSSerializerFilter is an interface");
        check(L("org.w3c.dom.traversal.NodeFilter").isAssignableFrom(k), "LSSerializerFilter extends NodeFilter");
        check(k.getInterfaces().length >= 1, "LSSerializerFilter has superinterface");
        check(k.getMethod("getWhatToShow") != null, "getWhatToShow()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ls.LSSerializerFilter a = new org.w3c.dom.ls.LSSerializerFilter(){
            public int getWhatToShow() { return 3; }
            public short acceptNode(org.w3c.dom.Node p0) { return (short)2; }
        };
        check(a != null, "anon impl of LSSerializerFilter");
        check(a.getWhatToShow() == 3, "getWhatToShow() returns fixed default");
        check(a.acceptNode((org.w3c.dom.Node) null) == (short)2, "acceptNode() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
