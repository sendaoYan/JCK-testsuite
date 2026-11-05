/*
 * @test
 * @summary org.w3c.dom.html.HTMLCollection conformance: reflection shape checks.
 * @run main HTMLCollectionTest
 */

import java.lang.reflect.*;

public class HTMLCollectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.html.HTMLCollection");
        check(c != null, "org.w3c.dom.html.HTMLCollection loads");
        check(c.getName().equals("org.w3c.dom.html.HTMLCollection"), "binary name is org.w3c.dom.html.HTMLCollection");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.html.HTMLCollection is public");
        check(c.isInterface(), "org.w3c.dom.html.HTMLCollection is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "item"), "declares item");
        check(declares(c, "namedItem"), "declares namedItem");
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
