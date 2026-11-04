/*
 * @test
 * @summary javax.swing.text.StyleConstants.ParagraphConstants conformance: white-box structure + black-box behavior.
 * @run main ParagraphConstantsTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class ParagraphConstantsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants$ParagraphConstants");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.AttributeSet$ParagraphAttribute"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleConstants$ParagraphConstants");
        Class<?> marker = Class.forName("javax.swing.text.AttributeSet$ParagraphAttribute");

        check(c.getConstructors().length == 0, "no public constructor");
        check(marker.isAssignableFrom(c), "implements ParagraphAttribute");

        // Paragraph keys are instances of ParagraphConstants.
        check(c.isInstance(StyleConstants.Alignment), "Alignment");
        check(c.isInstance(StyleConstants.FirstLineIndent), "FirstLineIndent");
        check(c.isInstance(StyleConstants.LeftIndent), "LeftIndent");
        check(c.isInstance(StyleConstants.RightIndent), "RightIndent");
        check(c.isInstance(StyleConstants.LineSpacing), "LineSpacing");
        check(c.isInstance(StyleConstants.SpaceAbove), "SpaceAbove");
        check(c.isInstance(StyleConstants.SpaceBelow), "SpaceBelow");
        check(!c.isInstance(StyleConstants.Bold), "Bold is not ParagraphConstants");

        // Paragraph values round-trip through these keys.
        SimpleAttributeSet s = new SimpleAttributeSet();
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLeftIndent(s, 8.0f);
        check(StyleConstants.getAlignment(s) == StyleConstants.ALIGN_CENTER, "alignment via key");
        check(StyleConstants.getLeftIndent(s) == 8.0f, "left indent via key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
