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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: Counter contract (shape + anonymous routing) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.css.Counter.class.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getIdentifier") && ms.contains("getListStyle") && ms.contains("getSeparator") && ms.size() == 3, "three accessor methods, no constants");
        org.w3c.dom.css.Counter c = new org.w3c.dom.css.Counter() {
            public String getIdentifier() { return "c"; }
            public String getListStyle() { return "decimal"; }
            public String getSeparator() { return "."; }
        };
        check("c".equals(c.getIdentifier()) && "decimal".equals(c.getListStyle()) && ".".equals(c.getSeparator()), "anonymous Counter returns the wired strings");
    }
}
