/*
 * @test
 * @summary javax.swing.text.StyleConstants conformance: key constants + typed get/set helpers + defaults + element-name side effects (headless-safe).
 * @run main StyleConstantsTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Label;
import javax.swing.Icon;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class StyleConstantsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testElementNames() {
        check("component".equals(StyleConstants.ComponentElementName), "ComponentElementName");
        check("icon".equals(StyleConstants.IconElementName), "IconElementName");
    }

    static void testKeyToString() {
        // The keys are typesafe constants whose string form is fixed.
        check("name".equals(StyleConstants.NameAttribute.toString()), "NameAttribute");
        check("resolver".equals(StyleConstants.ResolveAttribute.toString()), "ResolveAttribute");
        check("model".equals(StyleConstants.ModelAttribute.toString()), "ModelAttribute");
        check("bidiLevel".equals(StyleConstants.BidiLevel.toString()), "BidiLevel");
        check("family".equals(StyleConstants.FontFamily.toString()), "FontFamily");
        check("size".equals(StyleConstants.FontSize.toString()), "FontSize");
        check("bold".equals(StyleConstants.Bold.toString()), "Bold");
        check("italic".equals(StyleConstants.Italic.toString()), "Italic");
        check("underline".equals(StyleConstants.Underline.toString()), "Underline");
        check("strikethrough".equals(StyleConstants.StrikeThrough.toString()), "StrikeThrough");
        check("superscript".equals(StyleConstants.Superscript.toString()), "Superscript");
        check("subscript".equals(StyleConstants.Subscript.toString()), "Subscript");
        check("foreground".equals(StyleConstants.Foreground.toString()), "Foreground");
        check("background".equals(StyleConstants.Background.toString()), "Background");
        check("component".equals(StyleConstants.ComponentAttribute.toString()), "ComponentAttribute");
        check("icon".equals(StyleConstants.IconAttribute.toString()), "IconAttribute");
        check("composed text".equals(StyleConstants.ComposedTextAttribute.toString()), "ComposedTextAttribute");
        check("FirstLineIndent".equals(StyleConstants.FirstLineIndent.toString()), "FirstLineIndent");
        check("LeftIndent".equals(StyleConstants.LeftIndent.toString()), "LeftIndent");
        check("RightIndent".equals(StyleConstants.RightIndent.toString()), "RightIndent");
        check("LineSpacing".equals(StyleConstants.LineSpacing.toString()), "LineSpacing");
        check("SpaceAbove".equals(StyleConstants.SpaceAbove.toString()), "SpaceAbove");
        check("SpaceBelow".equals(StyleConstants.SpaceBelow.toString()), "SpaceBelow");
        check("Alignment".equals(StyleConstants.Alignment.toString()), "Alignment");
        check("TabSet".equals(StyleConstants.TabSet.toString()), "TabSet");
        check("Orientation".equals(StyleConstants.Orientation.toString()), "Orientation");
        // aliases are the very same key objects
        check(StyleConstants.Family == StyleConstants.FontFamily, "Family alias");
        check(StyleConstants.Size == StyleConstants.FontSize, "Size alias");
    }

    static void testAlignmentConstants() {
        check(StyleConstants.ALIGN_LEFT == 0, "ALIGN_LEFT 0");
        check(StyleConstants.ALIGN_CENTER == 1, "ALIGN_CENTER 1");
        check(StyleConstants.ALIGN_RIGHT == 2, "ALIGN_RIGHT 2");
        check(StyleConstants.ALIGN_JUSTIFIED == 3, "ALIGN_JUSTIFIED 3");
    }

    static void testDefaults() {
        AttributeSet empty = new SimpleAttributeSet();
        check(StyleConstants.getBidiLevel(empty) == 0, "default bidi level 0");
        check("Monospaced".equals(StyleConstants.getFontFamily(empty)), "default family Monospaced");
        check(StyleConstants.getFontSize(empty) == 12, "default size 12");
        check(!StyleConstants.isBold(empty), "default bold false");
        check(!StyleConstants.isItalic(empty), "default italic false");
        check(!StyleConstants.isUnderline(empty), "default underline false");
        check(!StyleConstants.isStrikeThrough(empty), "default strike false");
        check(!StyleConstants.isSuperscript(empty), "default superscript false");
        check(!StyleConstants.isSubscript(empty), "default subscript false");
        check(StyleConstants.getForeground(empty).equals(Color.black), "default foreground black");
        check(StyleConstants.getBackground(empty).equals(Color.black), "default background black");
        check(StyleConstants.getFirstLineIndent(empty) == 0f, "default first line indent 0");
        check(StyleConstants.getLeftIndent(empty) == 0f, "default left indent 0");
        check(StyleConstants.getRightIndent(empty) == 0f, "default right indent 0");
        check(StyleConstants.getLineSpacing(empty) == 0f, "default line spacing 0");
        check(StyleConstants.getSpaceAbove(empty) == 0f, "default space above 0");
        check(StyleConstants.getSpaceBelow(empty) == 0f, "default space below 0");
        check(StyleConstants.getAlignment(empty) == StyleConstants.ALIGN_LEFT, "default alignment left");
        check(StyleConstants.getTabSet(empty) == null, "default tab set null");
        check(StyleConstants.getComponent(empty) == null, "default component null");
        check(StyleConstants.getIcon(empty) == null, "default icon null");
    }

    static void testCharacterRoundTrips() {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setBidiLevel(a, 2);
        check(StyleConstants.getBidiLevel(a) == 2, "bidi round trip");
        check(a.getAttribute(StyleConstants.BidiLevel).equals(Integer.valueOf(2)), "bidi stored as Integer");

        StyleConstants.setFontFamily(a, "Serif");
        check("Serif".equals(StyleConstants.getFontFamily(a)), "family round trip");
        check(a.getAttribute(StyleConstants.FontFamily).equals("Serif"), "family stored under FontFamily key");

        StyleConstants.setFontSize(a, 24);
        check(StyleConstants.getFontSize(a) == 24, "size round trip");

        StyleConstants.setBold(a, true);
        check(StyleConstants.isBold(a), "bold round trip");
        check(a.getAttribute(StyleConstants.Bold).equals(Boolean.TRUE), "bold stored as Boolean");

        StyleConstants.setItalic(a, true);
        check(StyleConstants.isItalic(a), "italic round trip");
        StyleConstants.setUnderline(a, true);
        check(StyleConstants.isUnderline(a), "underline round trip");
        StyleConstants.setStrikeThrough(a, true);
        check(StyleConstants.isStrikeThrough(a), "strike round trip");
        StyleConstants.setSuperscript(a, true);
        check(StyleConstants.isSuperscript(a), "superscript round trip");
        StyleConstants.setSubscript(a, true);
        check(StyleConstants.isSubscript(a), "subscript round trip");

        StyleConstants.setForeground(a, Color.red);
        check(StyleConstants.getForeground(a) == Color.red, "foreground round trip");
        StyleConstants.setBackground(a, Color.blue);
        check(StyleConstants.getBackground(a) == Color.blue, "background round trip");

        // switching back to false keeps the attribute present but false
        StyleConstants.setBold(a, false);
        check(!StyleConstants.isBold(a), "bold false after set false");
        check(a.isDefined(StyleConstants.Bold), "bold still defined");

        // extreme sizes are stored verbatim
        StyleConstants.setFontSize(a, Integer.MAX_VALUE);
        check(StyleConstants.getFontSize(a) == Integer.MAX_VALUE, "max size round trip");
        StyleConstants.setFontSize(a, 0);
        check(StyleConstants.getFontSize(a) == 0, "zero size round trip");
        StyleConstants.setFontSize(a, -3);
        check(StyleConstants.getFontSize(a) == -3, "negative size stored");
    }

    static void testParagraphRoundTrips() {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setFirstLineIndent(a, 10.5f);
        check(StyleConstants.getFirstLineIndent(a) == 10.5f, "first line indent");
        StyleConstants.setLeftIndent(a, -1.25f);
        check(StyleConstants.getLeftIndent(a) == -1.25f, "negative left indent kept");
        StyleConstants.setRightIndent(a, 3.75f);
        check(StyleConstants.getRightIndent(a) == 3.75f, "right indent");
        StyleConstants.setLineSpacing(a, 2.0f);
        check(StyleConstants.getLineSpacing(a) == 2.0f, "line spacing");
        StyleConstants.setSpaceAbove(a, 5.5f);
        check(StyleConstants.getSpaceAbove(a) == 5.5f, "space above");
        StyleConstants.setSpaceBelow(a, 6.5f);
        check(StyleConstants.getSpaceBelow(a) == 6.5f, "space below");
        StyleConstants.setAlignment(a, StyleConstants.ALIGN_JUSTIFIED);
        check(StyleConstants.getAlignment(a) == StyleConstants.ALIGN_JUSTIFIED, "alignment");

        TabSet ts = new TabSet(new TabStop[] { new TabStop(40f) });
        StyleConstants.setTabSet(a, ts);
        check(StyleConstants.getTabSet(a) == ts, "tab set round trip identity");
        check(a.getAttribute(StyleConstants.TabSet) == ts, "tab set stored under key");
    }

    static void testComponentAndIcon() {
        SimpleAttributeSet a = new SimpleAttributeSet();
        Component c = new Component() { }; // bare AWT component, headless-safe, never shown
        StyleConstants.setComponent(a, c);
        check(StyleConstants.getComponent(a) == c, "component round trip");
        check("component".equals(a.getAttribute(javax.swing.text.AbstractDocument.ElementNameAttribute)),
              "setComponent also sets element name");
        check(a.isDefined(StyleConstants.ComponentAttribute), "component attribute defined");

        Icon icon = new Icon() {
            public void paintIcon(java.awt.Component cc, java.awt.Graphics g, int x, int y) { }
            public int getIconWidth() { return 1; }
            public int getIconHeight() { return 1; }
        };
        SimpleAttributeSet b = new SimpleAttributeSet();
        StyleConstants.setIcon(b, icon);
        check(StyleConstants.getIcon(b) == icon, "icon round trip");
        check("icon".equals(b.getAttribute(javax.swing.text.AbstractDocument.ElementNameAttribute)),
              "setIcon also sets element name");
    }

    public static void main(String[] args) throws Exception {
        testElementNames();
        testKeyToString();
        testAlignmentConstants();
        testDefaults();
        testCharacterRoundTrips();
        testParagraphRoundTrips();
        testComponentAndIcon();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
