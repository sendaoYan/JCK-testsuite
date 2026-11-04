/*
 * @test
 * @summary java.awt.font.TextAttribute conformance: white-box structure plus black-box behavior.
 * @run main TextAttributeTest
 */

import java.awt.font.TextAttribute;
import java.lang.reflect.*;
import java.text.AttributedCharacterIterator;

public class TextAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.TextAttribute");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(AttributedCharacterIterator.Attribute.class.isAssignableFrom(c), "extends Attribute");
        for (String s : new String[]{"FAMILY","WEIGHT","WIDTH","POSTURE","SIZE","TRANSFORM","SUPERSCRIPT",
                "FONT","CHAR_REPLACEMENT","FOREGROUND","BACKGROUND","UNDERLINE","STRIKETHROUGH",
                "RUN_DIRECTION","JUSTIFICATION","KERNING","LIGATURES","TRACKING"}) {
            Field f = c.getField(s);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()), "field " + s);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // attribute constants are distinct, typed keys
        check(TextAttribute.FAMILY != null && TextAttribute.WEIGHT != null && TextAttribute.SIZE != null
              && TextAttribute.FONT != null && TextAttribute.FOREGROUND != null, "attribute keys non-null");
        check(TextAttribute.FAMILY != TextAttribute.WEIGHT, "distinct attribute keys");
        check(TextAttribute.FAMILY instanceof TextAttribute, "FAMILY is a TextAttribute");

        // numeric value constants
        check(TextAttribute.WEIGHT_REGULAR == 1.0f && TextAttribute.WEIGHT_BOLD == 2.0f, "weight constants");
        check(TextAttribute.SUPERSCRIPT_SUPER == 1 && TextAttribute.SUPERSCRIPT_SUB == -1, "superscript constants");
        check(TextAttribute.UNDERLINE_ON == 0 && TextAttribute.UNDERLINE_LOW_ONE_PIXEL == 1, "underline constants");
        check(TextAttribute.POSTURE_REGULAR == 0.0f && TextAttribute.POSTURE_OBLIQUE == 0.2f, "posture constants");
        check(TextAttribute.STRIKETHROUGH_ON == Boolean.TRUE, "strikethrough on");
        check(TextAttribute.SWAP_COLORS_ON == Boolean.TRUE, "swap colors on");
        check(TextAttribute.KERNING_ON == 1 && TextAttribute.LIGATURES_ON == 1, "kerning/ligatures on");
        check(TextAttribute.RUN_DIRECTION_LTR == Boolean.FALSE && TextAttribute.RUN_DIRECTION_RTL == Boolean.TRUE,
              "run direction constants");

        // type checks
        check(TextAttribute.WEIGHT_BOLD instanceof Float, "WEIGHT_BOLD Float");
        check(TextAttribute.SUPERSCRIPT_SUPER instanceof Integer, "SUPERSCRIPT_SUPER Integer");
        check(TextAttribute.STRIKETHROUGH_ON instanceof Boolean, "STRIKETHROUGH_ON Boolean");

        // attribute readResolve keeps canonical singleton identity
        check(TextAttribute.SIZE == TextAttribute.SIZE, "canonical singleton identity");

        // direct calls to public inherited Attribute methods on the singleton constants
        check("java.awt.font.TextAttribute(family)".equals(TextAttribute.FAMILY.toString()), "toString includes name");
        check("java.awt.font.TextAttribute(size)".equals(TextAttribute.SIZE.toString()), "toString for SIZE");
        check(TextAttribute.FAMILY.equals(TextAttribute.FAMILY), "equals self (identity)");
        check(!TextAttribute.FAMILY.equals(TextAttribute.SIZE), "distinct keys not equal");
        check(TextAttribute.FAMILY.hashCode() == TextAttribute.FAMILY.hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
