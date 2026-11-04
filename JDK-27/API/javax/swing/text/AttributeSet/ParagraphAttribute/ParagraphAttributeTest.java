/*
 * @test
 * @summary javax.swing.text.AttributeSet.ParagraphAttribute conformance: white-box structure + black-box behavior.
 * @run main ParagraphAttributeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class ParagraphAttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AttributeSet$ParagraphAttribute");
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
        Class<?> c = Class.forName("javax.swing.text.AttributeSet$ParagraphAttribute");
        Class<?> constants = Class.forName("javax.swing.text.StyleConstants$ParagraphConstants");

        // Empty marker interface.
        check(c.getDeclaredMethods().length == 0, "no methods");
        check(c.getDeclaredFields().length == 0, "no fields");

        // StyleConstants.ParagraphConstants carries this type signature.
        check(c.isAssignableFrom(constants), "ParagraphConstants implements ParagraphAttribute");

        // Paragraph keys are instances of this marker.
        check(c.isInstance(StyleConstants.Alignment), "Alignment is ParagraphAttribute");
        check(c.isInstance(StyleConstants.FirstLineIndent), "FirstLineIndent is ParagraphAttribute");
        check(c.isInstance(StyleConstants.LeftIndent), "LeftIndent is ParagraphAttribute");
        check(c.isInstance(StyleConstants.RightIndent), "RightIndent is ParagraphAttribute");
        check(c.isInstance(StyleConstants.LineSpacing), "LineSpacing is ParagraphAttribute");
        check(c.isInstance(StyleConstants.SpaceAbove), "SpaceAbove is ParagraphAttribute");
        check(c.isInstance(StyleConstants.SpaceBelow), "SpaceBelow is ParagraphAttribute");
        check(!c.isInstance(StyleConstants.Bold), "Bold is not ParagraphAttribute");

        // A concrete AttributeSet is not itself a marker instance.
        check(!c.isAssignableFrom(SimpleAttributeSet.class), "SimpleAttributeSet not a marker");

        // The marker keys function as attribute keys carrying paragraph values.
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_RIGHT);
        check(s.isDefined(StyleConstants.Alignment), "Alignment key stored");
        check(StyleConstants.getAlignment(s) == StyleConstants.ALIGN_RIGHT, "getAlignment reads back");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
