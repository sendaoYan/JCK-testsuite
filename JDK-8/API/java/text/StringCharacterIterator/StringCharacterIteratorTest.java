/*
 * @test
 * @summary java.text.StringCharacterIterator conformance: traversal, substring constructor,
 *          clone.
 * @run main StringCharacterIteratorTest
 */

import java.text.StringCharacterIterator;

public class StringCharacterIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStringCharacterIterator() {
        StringCharacterIterator it = new StringCharacterIterator("abc");
        check(it.first() == 'a', "first");
        check(it.last() == 'c', "last");
        check(it.getBeginIndex() == 0, "getBeginIndex");
        check(it.getEndIndex() == 3, "getEndIndex");
        it.setIndex(1);
        check(it.current() == 'b', "current");
        check(it.next() == 'c', "next");
        check(it.previous() == 'b', "previous");
        check(it.getIndex() == 1, "getIndex");

        StringCharacterIterator it2 = new StringCharacterIterator("abcde", 1, 3, 1);
        check(it2.first() == 'b', "substring first");
        check(it2.last() == 'c', "substring last");
        check(it2.getBeginIndex() == 1, "substring begin");
        check(it2.getEndIndex() == 3, "substring end");

        it2.setText("xyz");
        check(it2.first() == 'x', "setText first");
        check(it2.last() == 'z', "setText last");
        check(it2.getBeginIndex() == 0 && it2.getEndIndex() == 3, "setText indexes");

        StringCharacterIterator it3 = (StringCharacterIterator) it.clone();
        check(it3.getIndex() == it.getIndex(), "clone preserves index");
        check(it.equals(it3), "clone equals");
        check(it.hashCode() == it3.hashCode(), "clone hashCode");
        check(it3.first() == 'a' && it3.last() == 'c', "clone traversal");
        check(it3.getBeginIndex() == 0 && it3.getEndIndex() == 3, "clone indexes");
    }

    public static void main(String[] args) {
        testStringCharacterIterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
