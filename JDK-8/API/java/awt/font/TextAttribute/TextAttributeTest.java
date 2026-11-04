/*
 * @test
 * @summary java.awt.font.TextAttribute conformance: attribute keys and values
 *          (headless-safe): class shape, key constant identity and toString names,
 *          documented value-constant classes and exact values, serialization
 *          resolving to the same singleton keys, constructor visibility.
 * @run main TextAttributeTest
 */

import java.awt.font.TextAttribute;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.AttributedCharacterIterator;

public class TextAttributeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object r = ois.readObject();
        ois.close();
        return r;
    }

    static void checkKey(String fieldName, String toStringName) throws Exception {
        Field f = TextAttribute.class.getField(fieldName);
        check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()), fieldName + " static final");
        check(f.getType() == TextAttribute.class, fieldName + " typed TextAttribute");
        Object v = f.get(null);
        check(v.toString().equals("java.awt.font.TextAttribute(" + toStringName + ")"),
                fieldName + " toString: " + v);
        check(roundTrip(v) == v, fieldName + " serializes to the same singleton");
    }

    static void checkValue(String fieldName, Object expected, Class<?> type) throws Exception {
        Field f = TextAttribute.class.getField(fieldName);
        check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()), fieldName + " static final");
        check(f.getType() == type, fieldName + " typed " + type.getSimpleName());
        check(f.get(null).equals(expected), fieldName + " value " + expected);
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.font.TextAttribute");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.text.AttributedCharacterIterator$Attribute"), "superclass java.text.AttributedCharacterIterator$Attribute");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Modifier.isFinal(c.getModifiers()), "final class (the protected ctor cannot be used externally anyway)");

        // Keys: identity, names, and singleton serialization.
        checkKey("FAMILY", "family");
        checkKey("WEIGHT", "weight");
        checkKey("WIDTH", "width");
        checkKey("POSTURE", "posture");
        checkKey("SIZE", "size");
        checkKey("TRANSFORM", "transform");
        checkKey("SUPERSCRIPT", "superscript");
        checkKey("FONT", "font");
        checkKey("CHAR_REPLACEMENT", "char_replacement");
        checkKey("FOREGROUND", "foreground");
        checkKey("BACKGROUND", "background");
        checkKey("UNDERLINE", "underline");
        checkKey("STRIKETHROUGH", "strikethrough");
        checkKey("RUN_DIRECTION", "run_direction");
        checkKey("BIDI_EMBEDDING", "bidi_embedding");
        checkKey("NUMERIC_SHAPING", "numeric_shaping");
        checkKey("KERNING", "kerning");
        checkKey("LIGATURES", "ligatures");
        checkKey("INPUT_METHOD_HIGHLIGHT", "input method highlight");
        checkKey("INPUT_METHOD_UNDERLINE", "input method underline");
        checkKey("SWAP_COLORS", "swap_colors");
        checkKey("TRACKING", "tracking");
        checkKey("JUSTIFICATION", "justification");

        // Distinct keys are distinct instances.
        check(TextAttribute.SIZE != TextAttribute.WEIGHT, "SIZE and WEIGHT are distinct keys");
        check(!TextAttribute.SIZE.equals(TextAttribute.WEIGHT), "keys unequal");
        check(TextAttribute.SIZE.equals(TextAttribute.SIZE), "key equals itself");
        check(TextAttribute.SIZE.hashCode() == TextAttribute.SIZE.hashCode(), "hashCode stable");

        // Value constants: exact documented values and value classes.
        checkValue("WEIGHT_EXTRA_LIGHT", 0.5f, Float.class);
        checkValue("WEIGHT_LIGHT", 0.75f, Float.class);
        checkValue("WEIGHT_DEMILIGHT", 0.875f, Float.class);
        checkValue("WEIGHT_REGULAR", 1.0f, Float.class);
        checkValue("WEIGHT_SEMIBOLD", 1.25f, Float.class);
        checkValue("WEIGHT_MEDIUM", 1.5f, Float.class);
        checkValue("WEIGHT_DEMIBOLD", 1.75f, Float.class);
        checkValue("WEIGHT_BOLD", 2.0f, Float.class);
        checkValue("WEIGHT_HEAVY", 2.25f, Float.class);
        checkValue("WEIGHT_EXTRABOLD", 2.5f, Float.class);
        checkValue("WEIGHT_ULTRABOLD", 2.75f, Float.class);
        checkValue("WIDTH_CONDENSED", 0.75f, Float.class);
        checkValue("WIDTH_SEMI_CONDENSED", 0.875f, Float.class);
        checkValue("WIDTH_REGULAR", 1.0f, Float.class);
        checkValue("WIDTH_SEMI_EXTENDED", 1.25f, Float.class);
        checkValue("WIDTH_EXTENDED", 1.5f, Float.class);
        checkValue("POSTURE_REGULAR", 0.0f, Float.class);
        checkValue("POSTURE_OBLIQUE", 0.2f, Float.class);
        checkValue("SUPERSCRIPT_SUPER", 1, Integer.class);
        checkValue("SUPERSCRIPT_SUB", -1, Integer.class);
        checkValue("UNDERLINE_ON", 0, Integer.class);
        checkValue("UNDERLINE_LOW_ONE_PIXEL", 1, Integer.class);
        checkValue("UNDERLINE_LOW_TWO_PIXEL", 2, Integer.class);
        checkValue("UNDERLINE_LOW_DOTTED", 3, Integer.class);
        checkValue("UNDERLINE_LOW_GRAY", 4, Integer.class);
        checkValue("UNDERLINE_LOW_DASHED", 5, Integer.class);
        checkValue("KERNING_ON", 1, Integer.class);
        checkValue("LIGATURES_ON", 1, Integer.class);
        checkValue("TRACKING_TIGHT", -0.04f, Float.class);
        checkValue("TRACKING_LOOSE", 0.04f, Float.class);
        checkValue("SWAP_COLORS_ON", Boolean.TRUE, Boolean.class);
        checkValue("STRIKETHROUGH_ON", Boolean.TRUE, Boolean.class);
        checkValue("RUN_DIRECTION_LTR", Boolean.FALSE, Boolean.class);
        checkValue("RUN_DIRECTION_RTL", Boolean.TRUE, Boolean.class);
        checkValue("JUSTIFICATION_FULL", 1.0f, Float.class);
        checkValue("JUSTIFICATION_NONE", 0.0f, Float.class);

        // Keys are Attribute instances usable in attributed text APIs.
        check(TextAttribute.SIZE instanceof AttributedCharacterIterator.Attribute,
                "keys are AttributedCharacterIterator.Attribute instances");

        // Constructor is protected: subclasses may create their own keys,
        // callers cannot instantiate random TextAttributes.
        Constructor<?> ctor = c.getDeclaredConstructor(String.class);
        check(Modifier.isProtected(ctor.getModifiers()), "TextAttribute(String) is protected");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
