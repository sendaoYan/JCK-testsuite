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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.css.Counter");
        check(k.isInterface(), "org.w3c.dom.css.Counter is an interface");
        check(k.getInterfaces().length == 0, "Counter declares no superinterface");
        check(k.getMethod("getIdentifier") != null, "getIdentifier()");
        check(k.getMethod("getListStyle") != null, "getListStyle()");
        check(k.getMethod("getSeparator") != null, "getSeparator()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.Counter a = new org.w3c.dom.css.Counter(){
            public java.lang.String getIdentifier() { return "s"; }
            public java.lang.String getListStyle() { return "s"; }
            public java.lang.String getSeparator() { return "s"; }
        };
        check(a != null, "anon impl of Counter");
        check("s".equals(a.getIdentifier()), "getIdentifier() returns fixed default");
        check("s".equals(a.getListStyle()), "getListStyle() returns fixed default");
        check("s".equals(a.getSeparator()), "getSeparator() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
