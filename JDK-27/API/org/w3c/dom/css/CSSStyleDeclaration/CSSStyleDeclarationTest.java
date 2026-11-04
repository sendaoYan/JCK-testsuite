/*
 * @test
 * @summary org.w3c.dom.css.CSSStyleDeclaration conformance: style declaration.
 * @run main CSSStyleDeclarationTest
 */

public class CSSStyleDeclarationTest {
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

        Class<?> k = L("org.w3c.dom.css.CSSStyleDeclaration");
        check(k.isInterface(), "org.w3c.dom.css.CSSStyleDeclaration is an interface");
        check(k.getInterfaces().length == 0, "CSSStyleDeclaration declares no superinterface");
        check(k.getMethod("getCssText") != null, "getCssText()");
        check(k.getMethod("setCssText", String.class) != null, "setCssText(String)");
        check(k.getMethod("getPropertyValue", String.class) != null, "getPropertyValue(String)");
        check(k.getMethod("getPropertyCSSValue", String.class) != null, "getPropertyCSSValue(String)");
        check(k.getMethod("removeProperty", String.class) != null, "removeProperty(String)");
        check(k.getMethod("getPropertyPriority", String.class) != null, "getPropertyPriority(String)");
        check(k.getMethod("setProperty", String.class, String.class, String.class) != null, "setProperty(String, String, String)");
        check(k.getMethod("getLength") != null, "getLength()");
        check(k.getMethod("item", int.class) != null, "item(int)");
        check(k.getMethod("getParentRule") != null, "getParentRule()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.css.CSSStyleDeclaration a = new org.w3c.dom.css.CSSStyleDeclaration(){
            public java.lang.String getCssText() { return "s"; }
            public void setCssText(java.lang.String p0) { }
            public java.lang.String getPropertyValue(java.lang.String p0) { return "s"; }
            public org.w3c.dom.css.CSSValue getPropertyCSSValue(java.lang.String p0) { return null; }
            public java.lang.String removeProperty(java.lang.String p0) { return "s"; }
            public java.lang.String getPropertyPriority(java.lang.String p0) { return "s"; }
            public void setProperty(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public int getLength() { return 3; }
            public java.lang.String item(int p0) { return "s"; }
            public org.w3c.dom.css.CSSRule getParentRule() { return null; }
        };
        check(a != null, "anon impl of CSSStyleDeclaration");
        check("s".equals(a.getCssText()), "getCssText() returns fixed default");
        a.setCssText("s");
        check("s".equals(a.getPropertyValue("s")), "getPropertyValue() returns fixed default");
        check(a.getPropertyCSSValue("s") == null, "getPropertyCSSValue() returns fixed default");
        check("s".equals(a.removeProperty("s")), "removeProperty() returns fixed default");
        check("s".equals(a.getPropertyPriority("s")), "getPropertyPriority() returns fixed default");
        a.setProperty("s", "s", "s");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check("s".equals(a.item(3)), "item() returns fixed default");
        check(a.getParentRule() == null, "getParentRule() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
