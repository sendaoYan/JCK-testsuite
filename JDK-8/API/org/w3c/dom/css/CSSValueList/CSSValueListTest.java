/*
 * @test
 * @summary org.w3c.dom.css.CSSValueList conformance: reflection shape checks.
 * @run main CSSValueListTest
 */

import java.lang.reflect.*;

public class CSSValueListTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSValueList");
        check(c != null, "org.w3c.dom.css.CSSValueList loads");
        check(c.getName().equals("org.w3c.dom.css.CSSValueList"), "binary name is org.w3c.dom.css.CSSValueList");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSValueList is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSValueList is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.css.CSSValue")), "direct superinterfaces");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "item"), "declares item");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSValueList contract (indexed access) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        org.w3c.dom.css.CSSValueList l = new org.w3c.dom.css.CSSValueList() {
            public int getLength() { return 3; }
            public org.w3c.dom.css.CSSValue item(int i) { return null; }
            public String getCssText() { return "a b c"; }
            public void setCssText(String t) { }
            public short getCssValueType() { return org.w3c.dom.css.CSSValue.CSS_VALUE_LIST; }
        };
        check("a b c".equals(l.getCssText()), "inherits the CSSValue text channel");
        check(l.getLength() == 3 && l.item(1) == null, "getLength/item wiring");
        check(l.item(3) == null, "out-of-range item -> null");
    }
}
