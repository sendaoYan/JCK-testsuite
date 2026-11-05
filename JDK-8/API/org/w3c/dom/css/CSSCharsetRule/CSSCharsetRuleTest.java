/*
 * @test
 * @summary org.w3c.dom.css.CSSCharsetRule conformance: reflection shape checks.
 * @run main CSSCharsetRuleTest
 */

import java.lang.reflect.*;

public class CSSCharsetRuleTest {
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
        Class<?> c = Class.forName("org.w3c.dom.css.CSSCharsetRule");
        check(c != null, "org.w3c.dom.css.CSSCharsetRule loads");
        check(c.getName().equals("org.w3c.dom.css.CSSCharsetRule"), "binary name is org.w3c.dom.css.CSSCharsetRule");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.CSSCharsetRule is public");
        check(c.isInterface(), "org.w3c.dom.css.CSSCharsetRule is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.css.CSSRule")), "direct superinterfaces");
        check(declares(c, "getEncoding"), "declares getEncoding");
        check(declares(c, "setEncoding"), "declares setEncoding");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: CSSCharsetRule contract (encoding accessor + rule typing) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.css.CSSRule.class.isAssignableFrom(org.w3c.dom.css.CSSCharsetRule.class), "CSSCharsetRule extends CSSRule");
        org.w3c.dom.css.CSSCharsetRule r = new org.w3c.dom.css.CSSCharsetRule() {
            String enc = "UTF-8";
            public String getEncoding() { return enc; }
            public void setEncoding(String e) { enc = e; }
            public String getCssText() { return null; }
            public void setCssText(String t) { }
            public short getType() { return org.w3c.dom.css.CSSRule.CHARSET_RULE; }
            public org.w3c.dom.css.CSSStyleSheet getParentStyleSheet() { return null; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check("UTF-8".equals(r.getEncoding()), "getEncoding round-trip");
        r.setEncoding("ISO-8859-1");
        check("ISO-8859-1".equals(r.getEncoding()), "setEncoding mutates the wired field");
        check(r.getType() == org.w3c.dom.css.CSSRule.CHARSET_RULE, "rule reports CHARSET_RULE(2)");
    }
}
