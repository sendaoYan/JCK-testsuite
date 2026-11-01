/*
 * @test
 * @summary java.text.Bidi conformance: base level/runs/reorderVisually.
 * @run main BidiTest
 */

import java.text.Bidi;

public class BidiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBidi() {
        Bidi b = new Bidi("abc", Bidi.DIRECTION_LEFT_TO_RIGHT);
        check(b.baseIsLeftToRight(), "baseIsLeftToRight");
        check(!b.isRightToLeft(), "not right-to-left");
        check(!b.isMixed(), "not mixed");
        check(b.isLeftToRight(), "isLeftToRight");
        check(b.getLength() == 3, "getLength");
        check(b.getBaseLevel() == 0, "getBaseLevel");
        check(b.getRunCount() == 1, "getRunCount");
        check(b.getRunLevel(0) == 0, "getRunLevel");
        check(b.getRunStart(0) == 0, "getRunStart");
        check(b.getRunLimit(0) == 3, "getRunLimit");
        check(b.getLevelAt(0) == 0, "getLevelAt");

        Bidi line = b.createLineBidi(0, 3);
        check(line.getLength() == 3, "createLineBidi length");

        byte[] levels = {0, 0, 0};
        Object[] chars = {Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2)};
        Bidi.reorderVisually(levels, 0, chars, 0, 3);
        check(((Integer) chars[0]).intValue() == 0, "reorderVisually keeps LTR order");

        check(b.toString() != null, "toString");
        check(Bidi.requiresBidi(new char[]{'a', 'b'}, 0, 2) == false, "requiresBidi false");
        check(Bidi.requiresBidi(new char[]{'א', 'ב'}, 0, 2) == true,
              "requiresBidi true");
    }

    public static void main(String[] args) {
        testBidi();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
