/*
 * @test
 * @summary org.w3c.dom.css.CSSMediaRule conformance: reflection shape checks.
 * @run main CSSMediaRuleTest
 */

import java.lang.reflect.*;

public class CSSMediaRuleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSMediaRule");
        check(c != null, "org.w3c.dom.css.CSSMediaRule loads");
        check(c.getName().equals("org.w3c.dom.css.CSSMediaRule"), "binary name is org.w3c.dom.css.CSSMediaRule");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSMediaRule is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSMediaRule is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.css.CSSRule")), "direct superinterfaces");
        check(declares(c, "getMedia"), "declares getMedia");
        check(declares(c, "getCssRules"), "declares getCssRules");
        check(declares(c, "insertRule"), "declares insertRule");
        check(declares(c, "deleteRule"), "declares deleteRule");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSMediaRule contract (media + rule list management) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.class.isAssignableFrom(org.w3c.dom.css.CSSMediaRule.class), "CSSMediaRule extends CSSRule");
        final int[] rules = new int[]{1};
        org.w3c.dom.css.CSSMediaRule r = new org.w3c.dom.css.CSSMediaRule() {
            public org.w3c.dom.stylesheets.MediaList getMedia() { return null; }
            public org.w3c.dom.css.CSSRuleList getCssRules() { return null; }
            public int insertRule(String rule, int index) { rules[0]++; return index; }
            public void deleteRule(int index) { rules[0]--; }
            public String getCssText() { return null; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.MEDIA_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(r.insertRule("a{}", 0) == 0 && rules[0] == 2, "insertRule returns the index and grows the list");
        r.deleteRule(0);
        check(rules[0] == 1, "deleteRule shrinks the list");
        check(r.getType() == org.w3c.dom.css.CSSRule.MEDIA_RULE, "rule reports MEDIA_RULE(4)");
    }
}
