/*
 * @test
 * @summary javax.swing.text.html.StyleSheet conformance: rule/declaration parsing,
 *          point-size conversion tables, color names, sheet linkage, HTML->CSS
 *          translation and negative cases (headless-safe).
 * @run main StyleSheetTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class StyleSheetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static HTMLEditorKit kit = new HTMLEditorKit();

    static HTMLDocument load(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), d, 0);
        return d;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
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
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRule"), "method getRule");
        check(names.contains("addRule"), "method addRule");
        check(names.contains("getDeclaration"), "method getDeclaration");
        check(names.contains("loadRules"), "method loadRules");
        check(names.contains("getViewAttributes"), "method getViewAttributes");
        check(names.contains("removeStyle"), "method removeStyle");
        check(names.contains("addStyleSheet"), "method addStyleSheet");
        check(names.contains("importStyleSheet"), "method importStyleSheet");
        check(names.contains("translateHTMLToCSS"), "method translateHTMLToCSS");
        check(names.contains("getPointSize"), "method getPointSize");
        check(names.contains("stringToColor"), "method stringToColor");
    }

    // ---- Part 2: declarations and rules ----
    static void testDeclarationsAndRules() {
        StyleSheet ss = new StyleSheet();
        check(ss != null, "new StyleSheet()");

        AttributeSet colorDecl = ss.getDeclaration("color: red");
        check(colorDecl.getAttributeCount() == 1, "getDeclaration color one attr");
        check(colorDecl.isDefined(CSS.Attribute.COLOR), "color key defined");
        check(!colorDecl.isDefined(CSS.Attribute.FONT_FAMILY), "only color defined");

        AttributeSet fontDecl = ss.getDeclaration("font-family: sans-serif");
        check(fontDecl.isDefined(CSS.Attribute.FONT_FAMILY), "font-family key defined");

        AttributeSet two = ss.getDeclaration("color: red; font-weight: bold");
        check(two.getAttributeCount() == 2, "two declarations parsed");
        check(two.isDefined(CSS.Attribute.FONT_WEIGHT), "font-weight key defined");

        check(ss.getDeclaration(null).getAttributeCount() == 0, "getDeclaration(null) empty");
        check(ss.getDeclaration("nonsense").getAttributeCount() == 0, "getDeclaration(nonsense) empty");
        check(ss.getDeclaration("").getAttributeCount() == 0, "getDeclaration(empty) empty");

        ss.addRule("p { color: red; margin-top: 10px; }");
        Style rule = ss.getRule("p");
        check(rule != null, "getRule(p) non-null");
        check(rule.isDefined(CSS.Attribute.COLOR), "rule has color");
        check("p".equals(rule.getAttribute(StyleConstants.NameAttribute)), "rule name p");
        check(ss.getRule("p") == rule, "getRule caches instance");
        check(ss.getRule("P") != null, "case-insensitive lookup non-null");

        ss.addRule("p { background-color: blue; }");
        check(ss.getRule("p") == rule, "same resolved style after second rule");
        check(rule.isDefined(CSS.Attribute.BACKGROUND_COLOR), "second rule merged in");

        Style unknown = ss.getRule("no-such-selector");
        check(unknown != null, "getRule(unknown) still non-null style");
        check(unknown.getAttributeCount() == 0, "unknown rule has no declarations");

        boolean npe = false;
        try { ss.getRule((String) null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getRule(null) NPE");

        // lenient rules: junk input is silently ignored (no exception)
        boolean ok = true;
        try {
            ss.addRule(null);
            ss.addRule("garbage no braces");
            ss.addRule("p { color: red"); // unterminated declaration still applied
        } catch (Throwable t) { ok = false; }
        check(ok, "addRule lenient on malformed input");

        // loadRules from a reader behaves like addRule
        StyleSheet loaded = new StyleSheet();
        boolean loadedOk = true;
        try { loaded.loadRules(new StringReader("h1 { color: green }"), null); }
        catch (Exception e) { loadedOk = false; }
        check(loadedOk, "loadRules succeeds");
        check(loaded.getRule("h1").isDefined(CSS.Attribute.COLOR), "loadRules applied rule");

        // importStyleSheet with a missing file is silently ignored
        boolean importOk = true;
        try { loaded.importStyleSheet(new java.net.URL("file:/nonexistent-dir-xyz/a.css")); }
        catch (Exception e) { importOk = false; }
        check(importOk, "importStyleSheet(missing file) tolerated");
        check(loaded.getRule("h1") != null, "rules intact after failed import");
    }

    // ---- Part 3: point size conversion table ----
    static void testPointSizes() {
        StyleSheet ss = new StyleSheet();
        float[] expected = {8f, 8f, 10f, 12f, 14f, 18f, 24f, 36f};
        for (int i = 0; i <= 7; i++) {
            check(ss.getPointSize(i) == expected[i], "getPointSize(" + i + ")==" + (int) expected[i]);
        }
        check(ss.getPointSize(-1) == 8f, "getPointSize(-1) clamps to smallest");
        check(ss.getPointSize(100) == 36f, "getPointSize(100) clamps to largest");
        check(ss.getPointSize(0) == 8f, "getPointSize(0) smallest");

        // 1-based index lookup: first map entry >= pt
        int[][] idxCases = {
            {8, 1}, {10, 2}, {12, 3}, {14, 4}, {18, 5}, {24, 6}, {36, 7},
            {0, 1}, {-1, 1}, {20, 6}, {100, 7},
        };
        for (int[] c : idxCases) {
            check(StyleSheet.getIndexOfSize((float) c[0]) == c[1],
                  "getIndexOfSize(" + c[0] + ")==" + c[1]);
        }
        check(StyleSheet.getIndexOfSize(11.9f) == 3, "getIndexOfSize(11.9) rounds into 12");
        check(StyleSheet.getIndexOfSize(13.0f) == 4, "getIndexOfSize(13) next bucket");

        // string forms: only plain/signed integer HTML size indexes are accepted
        check(ss.getPointSize("3") == 12f, "getPointSize(\"3\") == 12");
        check(ss.getPointSize("0") == 8f, "getPointSize(\"0\") == 8");
        check(ss.getPointSize("+1") == 18f, "getPointSize(\"+1\") == 18");
        check(ss.getPointSize("-1") == 12f, "getPointSize(\"-1\") == 12");
        check(ss.getPointSize("+2") == 24f, "getPointSize(\"+2\") == 24");
        check(ss.getPointSize("+3") == 36f, "getPointSize(\"+3\") == 36");
        String[] bad = {"12pt", "1.5em", "small", "medium", "large", "12px", "1cm", "50%", "abc", ""};
        for (String b : bad) {
            boolean nfe = false;
            try { ss.getPointSize(b); } catch (NumberFormatException e) { nfe = true; }
            check(nfe, "getPointSize(\"" + b + "\") NumberFormatException");
        }

        // base font size changes do not throw and keep the index table stable
        boolean ok = true;
        try {
            ss.setBaseFontSize(4);
            ss.setBaseFontSize("+1");
            ss.setBaseFontSize("-2");
            ss.setBaseFontSize(0);
        } catch (Throwable t) { ok = false; }
        check(ok, "setBaseFontSize variants tolerated");
        check(ss.getPointSize(3) == 12f, "getPointSize(3) still 12 after base changes");
    }

    // ---- Part 4: color parsing ----
    static void testColors() {
        StyleSheet ss = new StyleSheet();
        check(java.awt.Color.red.equals(ss.stringToColor("red")), "stringToColor(red)");
        check(java.awt.Color.red.equals(ss.stringToColor("RED")), "stringToColor(RED) case-insensitive");
        check(java.awt.Color.red.equals(ss.stringToColor("#FF0000")), "stringToColor(#FF0000)");
        check(java.awt.Color.red.equals(ss.stringToColor("#ff000080")), "6-digit plus alpha ignored");
        java.awt.Color rgb = ss.stringToColor("rgb(255,0,0)");
        check(rgb != null && rgb.getRed() == 255 && rgb.getGreen() == 0, "rgb() numeric");
        java.awt.Color rgbPct = ss.stringToColor("rgb(50%,0%,0%)");
        check(rgbPct != null && rgbPct.getRed() == 127, "rgb() percentage");
        check(java.awt.Color.blue.equals(ss.stringToColor("blue")), "named blue");
        java.awt.Color orange = ss.stringToColor("orange");
        check(orange != null && orange.getRed() == 255 && orange.getGreen() == 128, "named orange");
        java.awt.Color gray = ss.stringToColor("gray");
        check(gray != null && gray.getRed() == 128, "named gray");
        check(ss.stringToColor("cyan") == null, "cyan not in CSS1 name table");
        check(ss.stringToColor("magenta") == null, "magenta not in CSS1 name table");
        check(ss.stringToColor("grey") == null, "grey spelling not accepted");
        check(ss.stringToColor("nope") == null, "unknown name null");
        check(ss.stringToColor("0,0,0") == null, "bare tuple null");
        java.awt.Color empty = ss.stringToColor("");
        check(empty != null && empty.equals(java.awt.Color.black), "empty string black");

        // version divergence in 3-digit hex shorthand handling
        boolean jdk8 = System.getProperty("java.version").startsWith("1.8");
        java.awt.Color shortHex = ss.stringToColor("#F00");
        if (jdk8) {
            // JDK-8 mis-expands the 3-digit form
            check(shortHex.getRed() == 0 && shortHex.getGreen() == 15 && shortHex.getBlue() == 0,
                  "JDK-8 #F00 expansion (bug-compatible)");
        } else {
            check(java.awt.Color.red.equals(shortHex), "JDK-9+ #F00 expands to red");
        }
        java.awt.Color digits = ss.stringToColor("123");
        if (jdk8) {
            check(digits.getRed() == 0 && digits.getGreen() == 1 && digits.getBlue() == 35,
                  "JDK-8 decimal digits as int 0x123");
        } else {
            check(digits.getRed() == 17 && digits.getGreen() == 34 && digits.getBlue() == 51,
                  "JDK-9+ digits expanded per digit");
        }
        check(ss.stringToColor(null) == null, "stringToColor(null) returns null");
    }

    // ---- Part 5: sheet linkage and base ----
    static void testLinkage() {
        StyleSheet ss = new StyleSheet();
        check(ss.getBase() == null, "default base null");
        java.net.URL base;
        try { base = new java.net.URL("http://example.com/css/"); }
        catch (Exception e) { throw new AssertionError(e); }
        ss.setBase(base);
        check(base.equals(ss.getBase()), "setBase/getBase round-trip");
        ss.setBase(null);
        check(ss.getBase() == null, "setBase(null) clears");

        StyleSheet parent = new StyleSheet(), child = new StyleSheet();
        child.addRule("b { color: teal }");
        parent.addStyleSheet(child);
        StyleSheet[] sheets = parent.getStyleSheets();
        check(sheets != null && sheets.length == 1 && sheets[0] == child, "addStyleSheet/getStyleSheets");
        check(parent.getRule("b").isDefined(CSS.Attribute.COLOR), "parent resolves child rule");
        check(child.getStyleSheets() == null, "child has no linked sheets");
        parent.removeStyleSheet(child);
        check(parent.getStyleSheets() == null, "removeStyleSheet clears");
        check(!parent.getRule("b").isDefined(CSS.Attribute.COLOR), "child rule gone after removal");
        parent.removeStyleSheet(child); // removing again is a no-op
        check(parent.getStyleSheets() == null, "double remove tolerated");
    }

    // ---- Part 6: CSS attribute writing and HTML translation ----
    static void testCssAttributes() throws Exception {
        StyleSheet ss = new StyleSheet();

        SimpleAttributeSet s1 = new SimpleAttributeSet();
        ss.addCSSAttribute(s1, CSS.Attribute.FONT_SIZE, "12pt");
        check(s1.isDefined(CSS.Attribute.FONT_SIZE), "addCSSAttribute defines key");
        check("12pt".equals(String.valueOf(s1.getAttribute(CSS.Attribute.FONT_SIZE))), "addCSSAttribute keeps string");

        SimpleAttributeSet s2 = new SimpleAttributeSet();
        boolean fromHtmlInt = ss.addCSSAttributeFromHTML(s2, CSS.Attribute.FONT_SIZE, "6");
        check(fromHtmlInt, "addCSSAttributeFromHTML(6) accepted");
        check("6".equals(String.valueOf(s2.getAttribute(CSS.Attribute.FONT_SIZE))), "html font size 6 stored");

        SimpleAttributeSet s3 = new SimpleAttributeSet();
        boolean badColor = ss.addCSSAttributeFromHTML(s3, CSS.Attribute.BACKGROUND_COLOR, "notacolor");
        check(!badColor, "invalid color rejected");
        check(!s3.isDefined(CSS.Attribute.BACKGROUND_COLOR), "rejected color not stored");

        SimpleAttributeSet s4 = new SimpleAttributeSet();
        boolean okColor = ss.addCSSAttributeFromHTML(s4, CSS.Attribute.BACKGROUND_COLOR, "red");
        check(okColor, "valid color accepted");
        check("red".equals(String.valueOf(s4.getAttribute(CSS.Attribute.BACKGROUND_COLOR))), "color stored as text");

        // translateHTMLToCSS requires a real element's attribute set
        HTMLDocument doc = load("<html><body><p id='q' bgcolor='yellow'>T</p></body></html>");
        Element pe = doc.getElement("q");
        AttributeSet translated = ss.translateHTMLToCSS(pe.getAttributes());
        check(translated.isDefined(CSS.Attribute.BACKGROUND_COLOR), "bgcolor translated");
        check("yellow".equals(String.valueOf(translated.getAttribute(CSS.Attribute.BACKGROUND_COLOR))), "bgcolor value");

        boolean cce = false;
        try { ss.translateHTMLToCSS(new SimpleAttributeSet()); }
        catch (ClassCastException e) { cce = true; }
        check(cce, "translateHTMLToCSS(non-Element set) ClassCastException");
        boolean npe = false;
        try { ss.translateHTMLToCSS(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "translateHTMLToCSS(null) NPE");

        check(ss.getRule(HTML.Tag.BODY, doc.getDefaultRootElement().getElement(1)) != null,
              "getRule(Tag,Element) non-null");

        // raw string CSS values are not usable as colors without addCSSAttribute conversion
        SimpleAttributeSet raw = new SimpleAttributeSet();
        raw.addAttribute(CSS.Attribute.COLOR, "red");
        boolean rawCce = false;
        try { ss.getForeground(raw); } catch (ClassCastException e) { rawCce = true; }
        check(rawCce, "getForeground raw String value ClassCastException");

        SimpleAttributeSet viaCss = new SimpleAttributeSet();
        ss.addCSSAttribute(viaCss, CSS.Attribute.COLOR, "red");
        check(java.awt.Color.red.equals(ss.getForeground(viaCss)), "getForeground red");
        check(ss.getBackground(viaCss) == null, "getBackground null when unset");
        SimpleAttributeSet empty = new SimpleAttributeSet();
        check(java.awt.Color.black.equals(ss.getForeground(empty)), "default foreground black");
        check(ss.getBackground(empty) == null, "default background null");
        check(ss.getFont(empty) != null, "getFont non-null");
        check(ss.getFont(viaCss) != null, "getFont with color non-null");
    }

    // ---- Part 7: painters and style context ops ----
    static void testPaintersAndStyles() {
        StyleSheet ss = new StyleSheet();
        SimpleAttributeSet a = new SimpleAttributeSet();
        check(ss.getBoxPainter(a) != null, "getBoxPainter non-null");
        check(ss.getListPainter(a) != null, "getListPainter non-null");
        check(ss.getBoxPainter(a) != ss.getBoxPainter(a), "painter instances not cached identity");
        boolean npe1 = false, npe2 = false;
        try { ss.getBoxPainter(null); } catch (NullPointerException e) { npe1 = true; }
        try { ss.getListPainter(null); } catch (NullPointerException e) { npe2 = true; }
        check(npe1, "getBoxPainter(null) NPE");
        check(npe2, "getListPainter(null) NPE");

        Style st = ss.addStyle("mystyle", null);
        check(st != null && ss.getStyle("mystyle") == st, "addStyle/getStyle round-trip");
        check(ss.getStyle("mystyle2") == null, "unknown style null");
        ss.removeStyle("mystyle");
        check(ss.getStyle("mystyle") == null, "removeStyle removes");
        ss.removeStyle("mystyle"); // double remove tolerated
        check(ss.getStyle("mystyle") == null, "double removeStyle tolerated");
        check(ss.getStyleNames() != null, "getStyleNames enumeration");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testDeclarationsAndRules();
        testPointSizes();
        testColors();
        testLinkage();
        testCssAttributes();
        testPaintersAndStyles();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
