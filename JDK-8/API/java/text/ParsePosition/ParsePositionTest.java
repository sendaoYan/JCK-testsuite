/*
 * @test
 * @summary java.text.ParsePosition conformance: index/errorIndex/equals/hashCode.
 * @run main ParsePositionTest
 */

import java.text.ParsePosition;

public class ParsePositionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testParsePosition() {
        ParsePosition pp = new ParsePosition(5);
        check(pp.getIndex() == 5, "getIndex");
        check(pp.getErrorIndex() == -1, "default errorIndex");
        pp.setIndex(7);
        check(pp.getIndex() == 7, "setIndex");
        pp.setErrorIndex(2);
        check(pp.getErrorIndex() == 2, "setErrorIndex");

        ParsePosition pp2 = new ParsePosition(7);
        pp2.setErrorIndex(2);
        check(pp.equals(pp2), "equals");
        check(pp.hashCode() == pp2.hashCode(), "hashCode");
        check(!pp.equals(new ParsePosition(7)), "not equals differing errorIndex");
        check(pp.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testParsePosition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
