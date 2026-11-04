/*
 * @test
 * @summary javax.swing.text.StyleContext conformance: white-box structure + black-box behavior.
 * @run main StyleContextTest
 */

import java.awt.Font;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class StyleContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleContext");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.text.AbstractDocument$AttributeContext"), "interface 1");
    }

    static void testBlackBox() {
        check("default".equals(StyleContext.DEFAULT_STYLE), "DEFAULT_STYLE constant");

        StyleContext ctx = new StyleContext();

        // Default style is pre-registered.
        Style def = ctx.getStyle(StyleContext.DEFAULT_STYLE);
        check(def != null, "default style exists");
        check("default".equals(def.getName()), "default style name");

        // addStyle/getStyle round trip.
        Style s = ctx.addStyle("a", null);
        check(s != null, "addStyle non-null");
        check("a".equals(s.getName()), "added style name");
        check(ctx.getStyle("a") == s, "getStyle returns same instance");
        ctx.addStyle("b", null);

        // getStyleNames contains all registered names.
        List<String> names = Collections.list((java.util.Enumeration<String>) ctx.getStyleNames());
        Collections.sort(names);
        check(names.size() == 3, "three registered styles");
        check(names.contains("default") && names.contains("a") && names.contains("b"),
                "style names include default/a/b");

        // removeStyle.
        ctx.removeStyle("a");
        check(ctx.getStyle("a") == null, "removed style gone");
        check(ctx.getStyle("b") != null, "other style still present");
        check(ctx.getStyle("missing") == null, "unknown style is null");

        // getDefaultStyleContext is a singleton.
        check(StyleContext.getDefaultStyleContext() == StyleContext.getDefaultStyleContext(),
                "default context singleton");

        // AttributeContext: immutable set operations leave the source unchanged.
        SimpleAttributeSet base = new SimpleAttributeSet();
        base.addAttribute(StyleConstants.Bold, Boolean.TRUE);
        AttributeSet up = ctx.addAttribute(base, StyleConstants.Italic, Boolean.TRUE);
        check(up.getAttributeCount() == 2, "added set count 2");
        check(base.getAttributeCount() == 1, "source unchanged after addAttribute");
        check(Boolean.TRUE.equals(up.getAttribute(StyleConstants.Bold)), "added set keeps Bold");
        check(Boolean.TRUE.equals(up.getAttribute(StyleConstants.Italic)), "added set has Italic");
        AttributeSet removed = ctx.removeAttribute(up, StyleConstants.Bold);
        check(removed.getAttributeCount() == 1, "removed set count 1");
        check(removed.getAttribute(StyleConstants.Bold) == null, "removed set lacks Bold");

        // getEmptySet.
        check(ctx.getEmptySet().getAttributeCount() == 0, "empty set count 0");

        // Font resolution from attributes and direct font lookup.
        SimpleAttributeSet fontAttr = new SimpleAttributeSet();
        StyleConstants.setBold(fontAttr, true);
        StyleConstants.setItalic(fontAttr, true);
        StyleConstants.setFontFamily(fontAttr, "Serif");
        StyleConstants.setFontSize(fontAttr, 20);
        Font f = ctx.getFont(fontAttr);
        check(f.getStyle() == (Font.BOLD | Font.ITALIC), "font style resolved");
        check(f.getSize() == 20, "font size resolved");
        check("Serif".equals(f.getFamily()), "font family resolved");

        Font direct = ctx.getFont("Monospaced", Font.PLAIN, 14);
        check(direct.getSize() == 14, "direct font size");
        check("Monospaced".equals(direct.getFamily()), "direct font family");
        check(direct.getStyle() == Font.PLAIN, "direct font style");

        // Static attribute key registry (pre-populated with StyleConstants keys).
        Object ioFmt = StyleContext.getStaticAttributeKey(StyleConstants.Bold);
        check(ioFmt instanceof String, "static key format is String");
        check(StyleContext.getStaticAttribute(ioFmt) == StyleConstants.Bold, "static attribute round trip");
        check(StyleContext.getStaticAttribute("no.such.key") == null, "unknown static attribute null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
