/*
 * @test
 * @summary java.text.CharacterIterator conformance: white-box structure + black-box behavior.
 * @run main CharacterIteratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class CharacterIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.CharacterIterator");
        check(c.isInterface(), "is an interface");
        check(Cloneable.class.isAssignableFrom(c), "extends Cloneable");
        for (String m : new String[] {"first", "last", "current", "next", "previous",
                "getBeginIndex", "getEndIndex", "getIndex", "clone"})
            check(c.getDeclaredMethod(m) != null, "declares " + m);
        for (String m : new String[] {"first", "last", "current", "next", "previous",
                "getBeginIndex", "getEndIndex", "getIndex", "clone"})
            check(Modifier.isAbstract(c.getDeclaredMethod(m).getModifiers()), m + " abstract");
        check(c.getDeclaredMethod("setIndex", int.class) != null, "declares setIndex(int)");
        check(Modifier.isAbstract(c.getDeclaredMethod("setIndex", int.class).getModifiers()),
                "setIndex(int) abstract");
        check(c.getField("DONE").getType() == char.class, "DONE is a char constant");
        check(c.getField("DONE").getChar(null) == (char) 0xFFFF, "DONE == \\uFFFF");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. first/last/current and index bookkeeping
        CharacterIterator it = new StringCharacterIterator("abc");
        check(it.first() == 'a', "first returns first char");
        check(it.getIndex() == 0, "index 0 after first");
        check(it.current() == 'a', "current after first");
        check(it.last() == 'c', "last returns last char");
        check(it.getIndex() == 2, "index 2 after last");
        check(it.current() == 'c', "current after last");

        // 2. next/previous navigation and DONE at the boundaries
        check(it.next() == CharacterIterator.DONE, "next past end returns DONE");
        check(it.getIndex() == 3, "index at end after next");
        check(it.current() == CharacterIterator.DONE, "current is DONE at end");
        check(it.previous() == 'c', "previous returns from end");
        check(it.getIndex() == 2, "index back to last char");
        it.setIndex(0);
        check(it.previous() == CharacterIterator.DONE, "previous before begin returns DONE");
        check(it.getIndex() == 0, "index clamped at begin");

        // 3. setIndex navigation
        it.setIndex(1);
        check(it.current() == 'b', "setIndex(1) current");
        check(it.next() == 'c', "next after setIndex");
        check(it.previous() == 'b', "previous returns prior");
        check(it.getIndex() == 1, "index after previous");

        // 4. getBeginIndex/getEndIndex
        check(it.getBeginIndex() == 0, "getBeginIndex");
        check(it.getEndIndex() == 3, "getEndIndex");
        check(CharacterIterator.DONE == (char) 0xFFFF, "DONE constant value");

        // 5. clone via the interface contract
        CharacterIterator clone = (CharacterIterator) it.clone();
        check(clone != it, "clone is a distinct object");
        check(clone.getIndex() == it.getIndex(), "clone preserves index");
        check(clone.first() == 'a', "clone first");
        check(clone.current() == 'a', "clone current after first");
        check(clone.last() == 'c', "clone traverses same text");
        check(clone.getBeginIndex() == 0 && clone.getEndIndex() == 3, "clone preserves bounds");

        // 6. sub-range iterator via the concrete class (begin/end honored)
        CharacterIterator sub = new StringCharacterIterator("xyz", 1, 3, 1);
        check(sub.getBeginIndex() == 1 && sub.getEndIndex() == 3, "sub-range bounds");
        check(sub.first() == 'y', "sub-range first");
        check(sub.getIndex() == 1, "sub-range first index is begin");
        check(sub.last() == 'z', "sub-range last");

        // 7. empty iterator
        CharacterIterator empty = new StringCharacterIterator("");
        check(empty.getBeginIndex() == 0 && empty.getEndIndex() == 0, "empty bounds");
        check(empty.first() == CharacterIterator.DONE, "empty first returns DONE");
        check(empty.current() == CharacterIterator.DONE, "empty current returns DONE");

        // 8. setIndex validation via concrete class
        StringCharacterIterator sci = new StringCharacterIterator("abc");
        expectThrows(() -> sci.setIndex(-1), IllegalArgumentException.class,
                "setIndex below begin");
        expectThrows(() -> sci.setIndex(4), IllegalArgumentException.class,
                "setIndex beyond end");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
