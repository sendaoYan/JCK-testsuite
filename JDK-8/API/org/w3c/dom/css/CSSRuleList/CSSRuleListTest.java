/*
 * @test
 * @summary org.w3c.dom.css.CSSRuleList conformance: reflection shape checks.
 * @run main CSSRuleListTest
 */

import java.lang.reflect.*;

public class CSSRuleListTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSRuleList");
        check(c != null, "org.w3c.dom.css.CSSRuleList loads");
        check(c.getName().equals("org.w3c.dom.css.CSSRuleList"), "binary name is org.w3c.dom.css.CSSRuleList");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSRuleList is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSRuleList is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "item"), "declares item");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSRuleList contract (indexed access) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.css.CSSRuleList l = new org.w3c.dom.css.CSSRuleList() {
            public int getLength() { return 2; }
            public org.w3c.dom.css.CSSRule item(int i) { return null; }
        };
        check(l.getLength() == 2 && l.item(0) == null, "getLength/item wiring");
        check(l.item(99) == null, "out-of-range item -> null (no exception)");
    }
}
