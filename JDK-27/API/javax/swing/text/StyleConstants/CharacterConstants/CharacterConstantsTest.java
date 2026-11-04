/*
 * @test
 * @summary javax.swing.text.StyleConstants.CharacterConstants conformance: white-box structure + black-box behavior.
 * @run main CharacterConstantsTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class CharacterConstantsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants$CharacterConstants");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyleConstants"), "superclass");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.AttributeSet$CharacterAttribute"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants$CharacterConstants");
        Class<?> marker = Class.forName("javax.swing.text.AttributeSet$CharacterAttribute");

        // Key objects are created internally; the class has no public constructor.
        check(c.getConstructors().length == 0, "no public constructor");
        check(marker.isAssignableFrom(c), "implements CharacterAttribute");

        // Character-level keys are instances of CharacterConstants.
        check(c.isInstance(StyleConstants.Underline), "Underline");
        check(c.isInstance(StyleConstants.StrikeThrough), "StrikeThrough");
        check(c.isInstance(StyleConstants.Superscript), "Superscript");
        check(c.isInstance(StyleConstants.Subscript), "Subscript");
        check(c.isInstance(StyleConstants.ComponentAttribute), "ComponentAttribute");
        check(c.isInstance(StyleConstants.IconAttribute), "IconAttribute");
        check(c.isInstance(StyleConstants.BidiLevel), "BidiLevel");
        check(!c.isInstance(StyleConstants.Bold), "Bold is not CharacterConstants");

        // These key objects drive StyleConstants read/write.
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setUnderline(s, true);
        StyleConstants.setStrikeThrough(s, true);
        check(StyleConstants.isUnderline(s), "underline via key");
        check(StyleConstants.isStrikeThrough(s), "strikethrough via key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
