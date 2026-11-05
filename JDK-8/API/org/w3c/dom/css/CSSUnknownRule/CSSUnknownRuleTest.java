/*
 * @test
 * @summary org.w3c.dom.css.CSSUnknownRule conformance: reflection shape checks.
 * @run main CSSUnknownRuleTest
 */

import java.lang.reflect.*;

public class CSSUnknownRuleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSUnknownRule");
        check(c != null, "org.w3c.dom.css.CSSUnknownRule loads");
        check(c.getName().equals("org.w3c.dom.css.CSSUnknownRule"), "binary name is org.w3c.dom.css.CSSUnknownRule");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSUnknownRule is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSUnknownRule is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.css.CSSRule")), "direct superinterfaces");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSUnknownRule contract (marker subtype) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.class.isAssignableFrom(org.w3c.dom.css.CSSUnknownRule.class), "CSSUnknownRule extends CSSRule");
        check(org.w3c.dom.css.CSSUnknownRule.class.getDeclaredMethods().length == 0, "declares no methods of its own");
        org.w3c.dom.css.CSSUnknownRule r = new org.w3c.dom.css.CSSUnknownRule() {
            public String getCssText() { return "@x"; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.UNKNOWN_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(r.getType() == org.w3c.dom.css.CSSRule.UNKNOWN_RULE && "@x".equals(r.getCssText()), "unknown rule carries UNKNOWN_RULE(0)");
    }
}
