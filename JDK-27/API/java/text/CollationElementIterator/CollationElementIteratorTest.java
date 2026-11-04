/*
 * @test
 * @summary java.text.CollationElementIterator conformance: white-box structure + black-box next/previous/reset/offset/order extraction.
 * @run main CollationElementIteratorTest
 */

import java.text.CollationElementIterator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.List;

public class CollationElementIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.CollationElementIterator");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "final concrete class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getConstructors().length == 0, "no public constructors");
        check(c.getField("NULLORDER").getInt(null) == -1, "NULLORDER=-1");
        check(c.getMethod("primaryOrder", int.class) != null, "primaryOrder(int)");
        check(c.getMethod("secondaryOrder", int.class) != null, "secondaryOrder(int)");
        check(c.getMethod("tertiaryOrder", int.class) != null, "tertiaryOrder(int)");
        check(c.getMethod("next") != null, "next()");
        check(c.getMethod("previous") != null, "previous()");
        check(c.getMethod("getOffset") != null, "getOffset()");
        check(c.getMethod("setText", String.class) != null, "setText(String)");
        check(c.getMethod("setText", java.text.CharacterIterator.class) != null, "setText(CharacterIterator)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RuleBasedCollator col = new RuleBasedCollator("< a < b < c");
        CollationElementIterator it = col.getCollationElementIterator("ab");
        check(it.getOffset() == 0, "initial offset");
        int e1 = it.next();
        check(e1 != CollationElementIterator.NULLORDER, "first element");
        check(it.getOffset() > 0, "offset advanced after next");
        int e2 = it.next();
        check(e2 != CollationElementIterator.NULLORDER, "second element");
        check(it.next() == CollationElementIterator.NULLORDER, "end NULLORDER");

        int prev = it.previous();
        check(prev == e2, "previous() returns the second element");
        check(prev != CollationElementIterator.NULLORDER, "previous() non-null element");

        it.reset();
        check(it.getOffset() == 0, "reset offset");
        it.setOffset(0);
        check(it.getOffset() == 0, "setOffset");

        it.setText("cd");
        check(it.getOffset() == 0, "setText(String) offset");
        it.setText(new java.text.StringCharacterIterator("cd"));
        check(it.getOffset() == 0, "setText(CharacterIterator) offset");

        check(CollationElementIterator.primaryOrder(e1) >= 0, "primaryOrder");
        check(CollationElementIterator.secondaryOrder(e1) >= 0, "secondaryOrder");
        check(CollationElementIterator.tertiaryOrder(e1) >= 0, "tertiaryOrder");
        check(it.getMaxExpansion(e1) >= 0, "getMaxExpansion");

        // full forward walk, then full backward walk via previous()
        CollationElementIterator it2 = col.getCollationElementIterator("abc");
        List<Integer> fwd = new ArrayList<>();
        for (int e = it2.next(); e != CollationElementIterator.NULLORDER; e = it2.next()) fwd.add(e);
        check(fwd.size() == 3, "abc has 3 collation elements forward");
        it2.reset();
        while (it2.next() != CollationElementIterator.NULLORDER) { }
        List<Integer> rev = new ArrayList<>();
        for (int e = it2.previous(); e != CollationElementIterator.NULLORDER; e = it2.previous()) rev.add(e);
        check(rev.size() == 3, "abc has 3 collation elements backward");
        check(rev.get(0).equals(fwd.get(2)) && rev.get(2).equals(fwd.get(0)), "previous() reverses order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
