/*
 * @test
 * @summary javax.swing.text.StyleConstants.FontConstants conformance: white-box structure + black-box behavior.
 * @run main FontConstantsTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class FontConstantsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants$FontConstants");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyleConstants"), "superclass");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.AttributeSet$FontAttribute"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.text.AttributeSet$CharacterAttribute"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants$FontConstants");
        Class<?> fontMarker = Class.forName("javax.swing.text.AttributeSet$FontAttribute");
        Class<?> charMarker = Class.forName("javax.swing.text.AttributeSet$CharacterAttribute");

        check(c.getConstructors().length == 0, "no public constructor");
        check(fontMarker.isAssignableFrom(c), "implements FontAttribute");
        check(charMarker.isAssignableFrom(c), "implements CharacterAttribute");

        // Font keys are instances of FontConstants.
        check(c.isInstance(StyleConstants.Bold), "Bold");
        check(c.isInstance(StyleConstants.Italic), "Italic");
        check(c.isInstance(StyleConstants.FontFamily), "FontFamily");
        check(c.isInstance(StyleConstants.FontSize), "FontSize");
        check(!c.isInstance(StyleConstants.Underline), "Underline is not FontConstants");

        // Font values round-trip through these keys.
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setBold(s, true);
        StyleConstants.setItalic(s, true);
        StyleConstants.setFontFamily(s, "Serif");
        StyleConstants.setFontSize(s, 16);
        check(StyleConstants.isBold(s), "bold via key");
        check(StyleConstants.isItalic(s), "italic via key");
        check("Serif".equals(StyleConstants.getFontFamily(s)), "font family via key");
        check(StyleConstants.getFontSize(s) == 16, "font size via key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
