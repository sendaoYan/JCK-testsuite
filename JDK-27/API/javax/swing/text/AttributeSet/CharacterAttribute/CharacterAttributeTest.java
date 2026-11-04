/*
 * @test
 * @summary javax.swing.text.AttributeSet.CharacterAttribute conformance: white-box structure + black-box behavior.
 * @run main CharacterAttributeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class CharacterAttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AttributeSet$CharacterAttribute");
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
        Class<?> c = Class.forName("javax.swing.text.AttributeSet$CharacterAttribute");
        Class<?> constants = Class.forName("javax.swing.text.StyleConstants$CharacterConstants");

        // Empty marker interface: no methods, no fields.
        check(c.getDeclaredMethods().length == 0, "no methods");
        check(c.getDeclaredFields().length == 0, "no fields");

        // StyleConstants.CharacterConstants carries this type signature.
        check(c.isAssignableFrom(constants), "CharacterConstants implements CharacterAttribute");

        // Character-level keys are instances of this marker.
        check(c.isInstance(StyleConstants.Underline), "Underline is CharacterAttribute");
        check(c.isInstance(StyleConstants.StrikeThrough), "StrikeThrough is CharacterAttribute");
        check(c.isInstance(StyleConstants.Superscript), "Superscript is CharacterAttribute");
        check(c.isInstance(StyleConstants.Subscript), "Subscript is CharacterAttribute");
        check(c.isInstance(StyleConstants.ComponentAttribute), "ComponentAttribute is CharacterAttribute");
        check(c.isInstance(StyleConstants.IconAttribute), "IconAttribute is CharacterAttribute");
        check(!c.isInstance(StyleConstants.Alignment), "Alignment is not CharacterAttribute");

        // A concrete AttributeSet is not itself a marker instance.
        check(!c.isAssignableFrom(SimpleAttributeSet.class), "SimpleAttributeSet not a marker");

        // The marker keys function as attribute keys in a real set.
        SimpleAttributeSet s = new SimpleAttributeSet();
        s.addAttribute(StyleConstants.Underline, Boolean.TRUE);
        check(s.isDefined(StyleConstants.Underline), "Underline key stored");
        check(Boolean.TRUE.equals(s.getAttribute(StyleConstants.Underline)), "Underline value read back");
        check(StyleConstants.isUnderline(s), "isUnderline reads via Underline key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
