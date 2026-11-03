/*
 * @test
 * @summary javax.swing.text.AttributeSet.FontAttribute conformance: white-box structure + black-box behavior.
 * @run main FontAttributeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class FontAttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AttributeSet$FontAttribute");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AttributeSet$FontAttribute");
        Class<?> charAttr = Class.forName("javax.swing.text.AttributeSet$CharacterAttribute");
        Class<?> constants = Class.forName("javax.swing.text.StyleConstants$FontConstants");

        // Empty marker interface.
        check(c.getDeclaredMethods().length == 0, "no methods");
        check(c.getDeclaredFields().length == 0, "no fields");

        // StyleConstants.FontConstants carries both font and character signatures.
        check(c.isAssignableFrom(constants), "FontConstants implements FontAttribute");
        check(charAttr.isAssignableFrom(constants), "FontConstants also implements CharacterAttribute");

        // Font keys are instances of this marker.
        check(c.isInstance(StyleConstants.Bold), "Bold is FontAttribute");
        check(c.isInstance(StyleConstants.Italic), "Italic is FontAttribute");
        check(c.isInstance(StyleConstants.FontFamily), "FontFamily is FontAttribute");
        check(c.isInstance(StyleConstants.FontSize), "FontSize is FontAttribute");
        check(charAttr.isInstance(StyleConstants.Bold), "Bold also CharacterAttribute");
        check(!c.isInstance(StyleConstants.Underline), "Underline is not FontAttribute");

        // A concrete AttributeSet is not itself a marker instance.
        check(!c.isAssignableFrom(SimpleAttributeSet.class), "SimpleAttributeSet not a marker");

        // The marker keys function as attribute keys carrying font values.
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setBold(s, true);
        StyleConstants.setFontFamily(s, "Serif");
        StyleConstants.setFontSize(s, 18);
        check(s.isDefined(StyleConstants.Bold), "Bold key stored");
        check(StyleConstants.isBold(s), "isBold reads via Bold key");
        check("Serif".equals(StyleConstants.getFontFamily(s)), "font family read back");
        check(StyleConstants.getFontSize(s) == 18, "font size read back");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
