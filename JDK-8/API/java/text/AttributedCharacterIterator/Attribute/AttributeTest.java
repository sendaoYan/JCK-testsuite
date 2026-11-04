/*
 * @test
 * @summary java.text.AttributedCharacterIterator.Attribute conformance: constants and
 *          equals/hashCode.
 * @run main AttributeTest
 */

import java.text.AttributedCharacterIterator;

public class AttributeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAttribute() {
        AttributedCharacterIterator.Attribute lang =
                AttributedCharacterIterator.Attribute.LANGUAGE;
        check(lang != null, "LANGUAGE");
        check(lang.equals(AttributedCharacterIterator.Attribute.LANGUAGE), "equals");
        check(lang.hashCode() == AttributedCharacterIterator.Attribute.LANGUAGE.hashCode(),
              "hashCode");
        check(lang.toString() != null, "toString");
        check(AttributedCharacterIterator.Attribute.READING != null, "READING constant");
        check(AttributedCharacterIterator.Attribute.INPUT_METHOD_SEGMENT != null,
              "INPUT_METHOD_SEGMENT constant");
        check(!lang.equals(AttributedCharacterIterator.Attribute.READING), "distinct attributes");
    }

    public static void main(String[] args) {
        testAttribute();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
