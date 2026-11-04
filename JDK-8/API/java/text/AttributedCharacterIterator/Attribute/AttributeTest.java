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


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testContractDeepening() {
        java.text.AttributedCharacterIterator.Attribute lang =
                java.text.AttributedCharacterIterator.Attribute.LANGUAGE;
        java.text.AttributedCharacterIterator.Attribute read =
                java.text.AttributedCharacterIterator.Attribute.READING;
        check(lang != read, "LANGUAGE and READING are distinct singletons");
        check("java.text.AttributedCharacterIterator$Attribute(language)".equals(lang.toString()),
              "LANGUAGE toString exact");
        check("java.text.AttributedCharacterIterator$Attribute(reading)".equals(read.toString()),
              "READING toString exact");
        check(java.text.AttributedCharacterIterator.Attribute.class.getSuperclass()
                      .equals(Object.class), "Attribute directly extends Object");
        java.text.AttributedString as = new java.text.AttributedString("hi");
        as.addAttribute(lang, java.util.Locale.FRENCH);
        check(java.util.Locale.FRENCH.equals(as.getIterator().getAttribute(lang)),
              "Attribute constants work as AttributedString keys");
    }
    public static void main(String[] args) {
        testContractDeepening();
        testAttribute();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
