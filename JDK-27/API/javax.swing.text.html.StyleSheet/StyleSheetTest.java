/*
 * @test
 * @summary javax.swing.text.html.StyleSheet conformance: white-box structure + black-box behavior.
 * @run main StyleSheetTest
 */

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.text.AttributeSet;
import javax.swing.text.Style;
import javax.swing.text.html.CSS;
import javax.swing.text.html.StyleSheet;

public class StyleSheetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.StyleSheet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyleContext"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        StyleSheet ss = new StyleSheet();
        check(ss != null, "new StyleSheet()");

        AttributeSet colorDecl = ss.getDeclaration("color: red");
        check(colorDecl.getAttributeCount() == 1, "getDeclaration color one attr");
        check(colorDecl.isDefined(CSS.Attribute.COLOR), "color key defined");
        check(!colorDecl.isDefined(CSS.Attribute.FONT_FAMILY), "only color defined");

        AttributeSet fontDecl = ss.getDeclaration("font-family: sans-serif");
        check(fontDecl.isDefined(CSS.Attribute.FONT_FAMILY), "font-family key defined");

        check(ss.getDeclaration(null).getAttributeCount() == 0, "getDeclaration(null) empty");

        ss.addRule("p { color: red; }");
        Style rule = ss.getRule("p");
        check(rule != null, "getRule(p) non-null");
        check(rule.isDefined(CSS.Attribute.COLOR), "rule has color");
        check("p".equals(rule.getAttribute(javax.swing.text.StyleConstants.NameAttribute)), "rule name p");

        check(ss.getRule("no-such-selector") != null, "getRule(unknown) still non-null style");

        boolean npe = false;
        try { ss.getRule((String) null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getRule(null) NPE");

        check(java.awt.Color.red.equals(ss.stringToColor("red")), "stringToColor(red)");
        check(java.awt.Color.red.equals(ss.stringToColor("#ff0000")), "stringToColor(#ff0000)");
        check(ss.stringToColor("no-such-color") == null, "stringToColor unknown null");

        check(StyleSheet.getIndexOfSize(12.0f) == 3, "getIndexOfSize(12) == 3");
        check(ss.getPointSize(3) == 12.0f, "getPointSize(3) == 12");
        check(ss.getPointSize(7) == 36.0f, "getPointSize(7) == 36");
        check(ss.getPointSize("3") == 12.0f, "getPointSize(\"3\") == 12");

        URL base = new URL("http://example.com/css/");
        ss.setBase(base);
        check(base.equals(ss.getBase()), "setBase/getBase round-trip");

        StyleSheet sub = new StyleSheet();
        ss.addStyleSheet(sub);
        StyleSheet[] sheets = ss.getStyleSheets();
        check(sheets != null && sheets.length == 1 && sheets[0] == sub, "addStyleSheet/getStyleSheets");
        ss.removeStyleSheet(sub);
        check(ss.getStyleSheets() == null, "removeStyleSheet clears");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
