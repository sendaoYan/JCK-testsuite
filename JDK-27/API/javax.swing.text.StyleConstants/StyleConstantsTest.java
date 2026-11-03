/*
 * @test
 * @summary javax.swing.text.StyleConstants conformance: white-box structure + black-box behavior.
 * @run main StyleConstantsTest
 */

import java.awt.Color;
import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class StyleConstantsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        // Well-known public constants.
        check(StyleConstants.ALIGN_LEFT == 0, "ALIGN_LEFT == 0");
        check(StyleConstants.ALIGN_CENTER == 1, "ALIGN_CENTER == 1");
        check(StyleConstants.ALIGN_RIGHT == 2, "ALIGN_RIGHT == 2");
        check(StyleConstants.ALIGN_JUSTIFIED == 3, "ALIGN_JUSTIFIED == 3");
        check("component".equals(StyleConstants.ComponentElementName), "ComponentElementName");
        check("icon".equals(StyleConstants.IconElementName), "IconElementName");
        check(StyleConstants.Family == StyleConstants.FontFamily, "Family aliases FontFamily");
        check(StyleConstants.Size == StyleConstants.FontSize, "Size aliases FontSize");

        SimpleAttributeSet a = new SimpleAttributeSet();

        // Defaults when nothing is set.
        check(!StyleConstants.isBold(a), "default not bold");
        check(!StyleConstants.isItalic(a), "default not italic");
        check(!StyleConstants.isUnderline(a), "default not underline");
        check(!StyleConstants.isStrikeThrough(a), "default not strikethrough");
        check(!StyleConstants.isSuperscript(a), "default not superscript");
        check(!StyleConstants.isSubscript(a), "default not subscript");
        check("Monospaced".equals(StyleConstants.getFontFamily(a)), "default font family");
        check(StyleConstants.getFontSize(a) == 12, "default font size");
        check(StyleConstants.getAlignment(a) == StyleConstants.ALIGN_LEFT, "default alignment");
        check(StyleConstants.getForeground(a).equals(Color.black), "default foreground black");
        check(StyleConstants.getBackground(a).equals(Color.black), "default background black");
        check(StyleConstants.getBidiLevel(a) == 0, "default bidi level");
        check(StyleConstants.getLeftIndent(a) == 0f, "default left indent");
        check(StyleConstants.getRightIndent(a) == 0f, "default right indent");
        check(StyleConstants.getFirstLineIndent(a) == 0f, "default first line indent");
        check(StyleConstants.getLineSpacing(a) == 0f, "default line spacing");
        check(StyleConstants.getSpaceAbove(a) == 0f, "default space above");
        check(StyleConstants.getSpaceBelow(a) == 0f, "default space below");
        check(StyleConstants.getTabSet(a) == null, "default tabset null");
        check(StyleConstants.getComponent(a) == null, "default component null");
        check(StyleConstants.getIcon(a) == null, "default icon null");

        // Write then read: character attributes.
        StyleConstants.setBold(a, true);
        check(StyleConstants.isBold(a), "bold round trip");
        StyleConstants.setItalic(a, true);
        check(StyleConstants.isItalic(a), "italic round trip");
        StyleConstants.setUnderline(a, true);
        check(StyleConstants.isUnderline(a), "underline round trip");
        StyleConstants.setStrikeThrough(a, true);
        check(StyleConstants.isStrikeThrough(a), "strikethrough round trip");
        StyleConstants.setSuperscript(a, true);
        check(StyleConstants.isSuperscript(a), "superscript round trip");
        StyleConstants.setSubscript(a, true);
        check(StyleConstants.isSubscript(a), "subscript round trip");
        StyleConstants.setFontFamily(a, "Serif");
        check("Serif".equals(StyleConstants.getFontFamily(a)), "font family round trip");
        StyleConstants.setFontSize(a, 20);
        check(StyleConstants.getFontSize(a) == 20, "font size round trip");
        StyleConstants.setForeground(a, Color.red);
        check(Color.red.equals(StyleConstants.getForeground(a)), "foreground round trip");
        StyleConstants.setBackground(a, Color.blue);
        check(Color.blue.equals(StyleConstants.getBackground(a)), "background round trip");
        StyleConstants.setBidiLevel(a, 2);
        check(StyleConstants.getBidiLevel(a) == 2, "bidi level round trip");
        StyleConstants.setBold(a, false);
        check(!StyleConstants.isBold(a), "bold false round trip");

        // Write then read: paragraph attributes.
        StyleConstants.setAlignment(a, StyleConstants.ALIGN_RIGHT);
        check(StyleConstants.getAlignment(a) == StyleConstants.ALIGN_RIGHT, "alignment round trip");
        StyleConstants.setLeftIndent(a, 10.5f);
        check(StyleConstants.getLeftIndent(a) == 10.5f, "left indent round trip");
        StyleConstants.setRightIndent(a, 11.5f);
        check(StyleConstants.getRightIndent(a) == 11.5f, "right indent round trip");
        StyleConstants.setFirstLineIndent(a, 1.5f);
        check(StyleConstants.getFirstLineIndent(a) == 1.5f, "first line indent round trip");
        StyleConstants.setLineSpacing(a, 2.5f);
        check(StyleConstants.getLineSpacing(a) == 2.5f, "line spacing round trip");
        StyleConstants.setSpaceAbove(a, 3.5f);
        check(StyleConstants.getSpaceAbove(a) == 3.5f, "space above round trip");
        StyleConstants.setSpaceBelow(a, 4.5f);
        check(StyleConstants.getSpaceBelow(a) == 4.5f, "space below round trip");

        // TabSet round trip.
        TabSet ts = new TabSet(new TabStop[]{ new TabStop(36f) });
        StyleConstants.setTabSet(a, ts);
        check(StyleConstants.getTabSet(a) == ts, "tabset round trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
