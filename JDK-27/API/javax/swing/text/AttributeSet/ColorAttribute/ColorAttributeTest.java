/*
 * @test
 * @summary javax.swing.text.AttributeSet.ColorAttribute conformance: white-box structure + black-box behavior.
 * @run main ColorAttributeTest
 */

import java.awt.Color;
import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class ColorAttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AttributeSet$ColorAttribute");
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
        Class<?> c = Class.forName("javax.swing.text.AttributeSet$ColorAttribute");
        Class<?> charAttr = Class.forName("javax.swing.text.AttributeSet$CharacterAttribute");
        Class<?> constants = Class.forName("javax.swing.text.StyleConstants$ColorConstants");

        // Empty marker interface.
        check(c.getDeclaredMethods().length == 0, "no methods");
        check(c.getDeclaredFields().length == 0, "no fields");

        // StyleConstants.ColorConstants carries both color and character signatures.
        check(c.isAssignableFrom(constants), "ColorConstants implements ColorAttribute");
        check(charAttr.isAssignableFrom(constants), "ColorConstants also implements CharacterAttribute");

        // Color keys are instances of this marker.
        check(c.isInstance(StyleConstants.Foreground), "Foreground is ColorAttribute");
        check(c.isInstance(StyleConstants.Background), "Background is ColorAttribute");
        check(charAttr.isInstance(StyleConstants.Foreground), "Foreground also CharacterAttribute");
        check(!c.isInstance(StyleConstants.Bold), "Bold is not ColorAttribute");

        // A concrete AttributeSet is not itself a marker instance.
        check(!c.isAssignableFrom(SimpleAttributeSet.class), "SimpleAttributeSet not a marker");

        // The marker keys function as attribute keys carrying Color values.
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setForeground(s, Color.red);
        check(s.isDefined(StyleConstants.Foreground), "Foreground key stored");
        check(Color.red.equals(StyleConstants.getForeground(s)), "getForeground reads back Color");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
