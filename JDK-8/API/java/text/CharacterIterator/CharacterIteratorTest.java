/*
 * @test
 * @summary java.text.CharacterIterator conformance: traversal via StringCharacterIterator.
 * @run main CharacterIteratorTest
 */

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class CharacterIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharacterIterator() {
        CharacterIterator it = new StringCharacterIterator("abc");
        check(it.first() == 'a', "first");
        check(it.getIndex() == 0, "getIndex after first");
        check(it.current() == 'a', "current after first");
        check(it.last() == 'c', "last");
        check(it.getIndex() == 2, "getIndex after last");
        check(it.current() == 'c', "current after last");
        check(it.next() == CharacterIterator.DONE, "next past end");
        check(it.getIndex() == 3, "getIndex past end");
        check(it.current() == CharacterIterator.DONE, "current DONE at end");
        it.setIndex(1);
        check(it.current() == 'b', "current");
        check(it.next() == 'c', "next");
        check(it.previous() == 'b', "previous");
        check(it.getIndex() == 1, "getIndex after previous");
        check(it.getBeginIndex() == 0, "getBeginIndex");
        check(it.getEndIndex() == 3, "getEndIndex");
        check(CharacterIterator.DONE == (char) 0xFFFF, "DONE == \\uFFFF");
    }

    public static void main(String[] args) {
        testCharacterIterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
