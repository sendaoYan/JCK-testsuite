/*
 * @test
 * @summary javax.swing.text.StyleConstants.ColorConstants conformance: white-box structure + black-box behavior.
 * @run main ColorConstantsTest
 */

import java.awt.Color;
import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class ColorConstantsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants$ColorConstants");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.AttributeSet$ColorAttribute"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.text.AttributeSet$CharacterAttribute"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants$ColorConstants");
        Class<?> colorMarker = Class.forName("javax.swing.text.AttributeSet$ColorAttribute");
        Class<?> charMarker = Class.forName("javax.swing.text.AttributeSet$CharacterAttribute");

        check(c.getConstructors().length == 0, "no public constructor");
        check(colorMarker.isAssignableFrom(c), "implements ColorAttribute");
        check(charMarker.isAssignableFrom(c), "implements CharacterAttribute");

        // Color keys are instances of ColorConstants.
        check(c.isInstance(StyleConstants.Foreground), "Foreground");
        check(c.isInstance(StyleConstants.Background), "Background");
        check(!c.isInstance(StyleConstants.Bold), "Bold is not ColorConstants");

        // Color values round-trip through these keys.
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setForeground(s, Color.red);
        StyleConstants.setBackground(s, Color.blue);
        check(Color.red.equals(StyleConstants.getForeground(s)), "foreground via key");
        check(Color.blue.equals(StyleConstants.getBackground(s)), "background via key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
