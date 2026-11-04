/*
 * @test
 * @summary java.text.CollationElementIterator conformance: next/reset/offset/order extractors.
 * @run main CollationElementIteratorTest
 */

import java.text.CollationElementIterator;
import java.text.RuleBasedCollator;

public class CollationElementIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCollationElementIterator() throws Exception {
        RuleBasedCollator col = new RuleBasedCollator("< a < b < c");
        CollationElementIterator it = col.getCollationElementIterator("ab");
        check(it.getOffset() == 0, "initial offset");
        int e1 = it.next();
        check(e1 != CollationElementIterator.NULLORDER, "first element");
        check(it.getOffset() > 0, "offset advanced after next");
        int e2 = it.next();
        check(e2 != CollationElementIterator.NULLORDER, "second element");
        check(it.next() == CollationElementIterator.NULLORDER, "end NULLORDER");

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
    }

    public static void main(String[] args) throws Exception {
        testCollationElementIterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
