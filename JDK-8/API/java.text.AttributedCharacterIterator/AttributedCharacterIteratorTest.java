/*
 * @test
 * @summary java.text.AttributedCharacterIterator conformance: runs/attributes via
 *          AttributedString.
 * @run main AttributedCharacterIteratorTest
 */

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class AttributedCharacterIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIterator() {
        AttributedString as = new AttributedString("hello world");
        as.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, "en");
        AttributedCharacterIterator it = as.getIterator();
        check(it.getBeginIndex() == 0, "beginIndex");
        check(it.getEndIndex() == 11, "endIndex");
        check(it.getRunStart() == 0, "runStart");
        check(it.getRunLimit() == 11, "runLimit");
        check("en".equals(it.getAttribute(AttributedCharacterIterator.Attribute.LANGUAGE)),
              "getAttribute");
        check(it.getAttributes().containsKey(AttributedCharacterIterator.Attribute.LANGUAGE),
              "getAttributes");
        check(it.getAllAttributeKeys().contains(AttributedCharacterIterator.Attribute.LANGUAGE),
              "getAllAttributeKeys");
        check(it.getRunStart(AttributedCharacterIterator.Attribute.LANGUAGE) == 0,
              "getRunStart(Attribute)");
        check(it.getRunLimit(AttributedCharacterIterator.Attribute.LANGUAGE) == 11,
              "getRunLimit(Attribute)");
    }

    public static void main(String[] args) {
        testIterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
