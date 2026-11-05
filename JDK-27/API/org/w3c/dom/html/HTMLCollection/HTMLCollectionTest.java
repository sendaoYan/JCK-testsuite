/*
 * @test
 * @summary org.w3c.dom.html.HTMLCollection conformance: length/item/namedItem.
 * @run main HTMLCollectionTest
 */

public class HTMLCollectionTest {
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

        Class<?> k = L("org.w3c.dom.html.HTMLCollection");
        check(k.isInterface(), "org.w3c.dom.html.HTMLCollection is an interface");
        check(k.getInterfaces().length == 0, "HTMLCollection declares no superinterface");
        check(k.getMethod("getLength") != null, "getLength()");
        check(k.getMethod("item", int.class) != null, "item(int)");
        check(k.getMethod("namedItem", String.class) != null, "namedItem(String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.html.HTMLCollection a = new org.w3c.dom.html.HTMLCollection(){
            public int getLength() { return 3; }
            public org.w3c.dom.Node item(int p0) { return null; }
            public org.w3c.dom.Node namedItem(java.lang.String p0) { return null; }
        };
        check(a != null, "anon impl of HTMLCollection");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check(a.item(3) == null, "item() returns fixed default");
        check(a.namedItem("s") == null, "namedItem() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral + shape-lock ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLCollection.class.isInterface(), "HTMLCollection is an interface");
        final org.w3c.dom.Node[] items = new org.w3c.dom.Node[1];
        org.w3c.dom.html.HTMLCollection c = new org.w3c.dom.html.HTMLCollection() {
            public int getLength() { return 1; }
            public org.w3c.dom.Node item(int i) { return i == 0 ? items[0] : null; }
            public org.w3c.dom.Node namedItem(String n) { return "x".equals(n) ? items[0] : null; }
        };
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        items[0] = d.createElement("x");
        check(c.getLength() == 1 && c.item(0) == items[0], "length/item indexed access");
        check(c.item(5) == null, "out-of-range item -> null");
        check(c.namedItem("x") == items[0] && c.namedItem("nope") == null, "namedItem by name, unknown -> null");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLCollection.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 3, "HTMLCollection declares exactly 3 methods");
        check(names.contains("getLength"), "getLength declared");
        check(names.contains("item"), "item declared");
        check(names.contains("namedItem"), "namedItem declared");
    }
}
