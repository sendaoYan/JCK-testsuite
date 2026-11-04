/*
 * @test
 * @summary java.text.AttributedCharacterIterator.Attribute conformance: white-box structure
 *          + black-box behavior.
 * @run main AttributeTest
 */

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.AttributedCharacterIterator;

public class AttributeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.AttributedCharacterIterator$Attribute");
        check(Modifier.isPublic(c.getModifiers()), "public static nested class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getEnclosingClass() == AttributedCharacterIterator.class, "nested in AttributedCharacterIterator");

        for (String f : new String[] {"LANGUAGE", "READING", "INPUT_METHOD_SEGMENT"}) {
            Field field = c.getField(f);
            check(Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers()), f + " is public static final");
            check(field.getType() == c, f + " typed as Attribute");
        }

        Constructor<?> ctor = c.getDeclaredConstructor(String.class);
        check(Modifier.isProtected(ctor.getModifiers()), "single protected Attribute(String)");

        Method eq = c.getMethod("equals", Object.class);
        check(Modifier.isFinal(eq.getModifiers()), "equals is final");
        Method hc = c.getMethod("hashCode");
        check(Modifier.isFinal(hc.getModifiers()), "hashCode is final");
        Method getName = c.getDeclaredMethod("getName");
        check(Modifier.isProtected(getName.getModifiers()), "getName is protected");
        Method rr = c.getDeclaredMethod("readResolve");
        check(Modifier.isProtected(rr.getModifiers()), "readResolve is protected");
        check(c.getDeclaredMethod("toString") != null, "toString present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AttributedCharacterIterator.Attribute lang =
                AttributedCharacterIterator.Attribute.LANGUAGE;
        AttributedCharacterIterator.Attribute reading =
                AttributedCharacterIterator.Attribute.READING;
        AttributedCharacterIterator.Attribute ims =
                AttributedCharacterIterator.Attribute.INPUT_METHOD_SEGMENT;

        // 1. three constants non-null and mutually distinct
        check(lang != null && reading != null && ims != null, "all three constants non-null");
        check(!lang.equals(reading) && !lang.equals(ims) && !reading.equals(ims),
                "constants are mutually distinct");

        // 2. equals/hashCode reflexive, symmetric, and rejects non-Attribute
        check(lang.equals(lang), "equals reflexive");
        check(lang.equals(AttributedCharacterIterator.Attribute.LANGUAGE), "same constant equal");
        check(lang.hashCode() == AttributedCharacterIterator.Attribute.LANGUAGE.hashCode(),
                "hashCode consistent");
        check(!lang.equals(null), "equals(null) false");
        check(!lang.equals("language"), "equals(other type) false");

        // 3. toString renders the attribute name
        check(lang.toString().contains("language"), "LANGUAGE toString contains name");
        check(reading.toString().contains("reading"), "READING toString contains name");
        check(ims.toString().contains("input_method_segment"), "INPUT_METHOD_SEGMENT toString contains name");

        // 4. protected ctor + getName form a custom attribute subclass
        @SuppressWarnings("serial")
        class Custom extends AttributedCharacterIterator.Attribute {
            Custom(String name) { super(name); }
            @Override protected Object readResolve() throws java.io.InvalidObjectException {
                return this;
            }
        }
        Custom custom = new Custom("weight");
        check(custom.toString().contains("weight"), "custom attribute name via toString");
        check(!custom.equals(lang), "custom attribute distinct from constants");
        Custom custom2 = new Custom("weight");
        check(custom.equals(custom), "custom attribute equal to itself (identity)");
        check(!custom.equals(custom2), "distinct same-name instances not equal (identity equals)");

        // 5. attribute serves as a Map key and as an AttributedString run selector
        java.util.Map<AttributedCharacterIterator.Attribute, Object> m = new java.util.HashMap<>();
        m.put(lang, "en");
        m.put(reading, "r");
        check("en".equals(m.get(AttributedCharacterIterator.Attribute.LANGUAGE)),
                "constant works as Map key across equal instances");
        java.text.AttributedString as = new java.text.AttributedString("hi");
        as.addAttribute(lang, "en");
        check("en".equals(as.getIterator().getAttribute(AttributedCharacterIterator.Attribute.LANGUAGE)),
                "attribute selects a run in AttributedString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
