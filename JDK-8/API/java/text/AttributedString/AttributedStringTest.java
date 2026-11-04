/*
 * @test
 * @summary java.text.AttributedString conformance: constructors/addAttribute/iterators.
 * @run main AttributedStringTest
 */

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

public class AttributedStringTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAttributedString() {
        AttributedString as = new AttributedString("abc");
        check(as.getIterator() != null, "iterator");
        check(as.getIterator().getEndIndex() == 3, "iterator endIndex");

        Map<AttributedCharacterIterator.Attribute, Object> m = new HashMap<>();
        m.put(AttributedCharacterIterator.Attribute.LANGUAGE, "en");
        AttributedString as2 = new AttributedString("abc", m);
        check("en".equals(as2.getIterator().getAttribute(
                AttributedCharacterIterator.Attribute.LANGUAGE)), "map constructor");

        as.addAttribute(AttributedCharacterIterator.Attribute.READING, "r");
        check("r".equals(as.getIterator().getAttribute(
                AttributedCharacterIterator.Attribute.READING)), "addAttribute");

        AttributedString as3 = new AttributedString("hello");
        as3.addAttribute(AttributedCharacterIterator.Attribute.LANGUAGE, "en", 1, 3);
        AttributedCharacterIterator it3 = as3.getIterator();
        it3.setIndex(1);
        check("en".equals(it3.getAttribute(AttributedCharacterIterator.Attribute.LANGUAGE)),
              "range attribute at 1");

        AttributedString as4 = new AttributedString("x");
        as4.addAttributes(m, 0, 1);
        check("en".equals(as4.getIterator().getAttribute(
                AttributedCharacterIterator.Attribute.LANGUAGE)), "addAttributes");

        AttributedCharacterIterator sub = as.getIterator(
                new AttributedCharacterIterator.Attribute[] {
                    AttributedCharacterIterator.Attribute.LANGUAGE}, 0, 3);
        check(sub.getEndIndex() == 3, "subiterator end");
    }

    public static void main(String[] args) {
        testAttributedString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
