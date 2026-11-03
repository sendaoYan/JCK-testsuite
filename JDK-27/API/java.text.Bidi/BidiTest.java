/*
 * @test
 * @summary java.text.Bidi conformance: white-box structure + black-box direction/run/reorder behavior.
 * @run main BidiTest
 */

import java.text.Bidi;

public class BidiTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.Bidi");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "final concrete class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getField("DIRECTION_LEFT_TO_RIGHT").getInt(null) == 0, "DIRECTION_LEFT_TO_RIGHT=0");
        check(c.getField("DIRECTION_RIGHT_TO_LEFT").getInt(null) == 1, "DIRECTION_RIGHT_TO_LEFT=1");
        check(c.getField("DIRECTION_DEFAULT_LEFT_TO_RIGHT").getInt(null) == -2, "DIRECTION_DEFAULT_LEFT_TO_RIGHT=-2");
        check(c.getField("DIRECTION_DEFAULT_RIGHT_TO_LEFT").getInt(null) == -1, "DIRECTION_DEFAULT_RIGHT_TO_LEFT=-1");
        check(c.getConstructor(String.class, int.class) != null, "ctor(String,int)");
        check(c.getConstructor(java.text.AttributedCharacterIterator.class) != null, "ctor(AttributedCharacterIterator)");
        check(c.getMethod("reorderVisually", byte[].class, int.class, Object[].class, int.class, int.class) != null,
              "reorderVisually");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testLeftToRight() {
        Bidi b = new Bidi("abc", Bidi.DIRECTION_LEFT_TO_RIGHT);
        check(b.isLeftToRight() && !b.isRightToLeft(), "isLeftToRight");
        check(b.baseIsLeftToRight(), "baseIsLeftToRight true");
        check(b.getLength() == 3, "getLength 3");
        check(b.getBaseLevel() == 0, "getBaseLevel 0");
        check(b.getRunCount() == 1, "getRunCount 1");
        check(b.getRunLevel(0) == 0, "getRunLevel 0");
        check(b.getRunStart(0) == 0 && b.getRunLimit(0) == 3, "run start/limit");
        check(!b.isMixed(), "not mixed");
        check(b.toString() != null && b.toString().length() > 0, "toString non-empty");
    }

    static void testRightToLeft() {
        Bidi b = new Bidi("אב", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT); // Hebrew alef bet
        check(b.isRightToLeft() && !b.isLeftToRight(), "isRightToLeft");
        check(!b.baseIsLeftToRight(), "baseIsLeftToRight false");
        check(b.getBaseLevel() == 1, "getBaseLevel 1");
        check(b.getRunLevel(0) == 1, "getRunLevel 1");
        check(b.getRunCount() == 1, "getRunCount 1");
        check(!b.isMixed(), "not mixed");
    }

    static void testMixedRuns() {
        Bidi b = new Bidi("aאb", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT); // a alef b
        check(b.isMixed(), "isMixed true");
        check(b.getRunCount() == 3, "getRunCount 3");
        check(b.getLevelAt(0) == 0 && b.getLevelAt(1) == 1 && b.getLevelAt(2) == 0, "getLevelAt per char");
        check(b.getRunStart(0) == 0 && b.getRunLimit(0) == 1, "run0 start/limit");
    }

    static void testRequiresBidi() {
        check(!Bidi.requiresBidi(new char[]{'a', 'b', 'c'}, 0, 3), "requiresBidi ascii false");
        check(Bidi.requiresBidi(new char[]{'א', 'ב'}, 0, 2), "requiresBidi Hebrew true");
    }

    static void testReorderVisually() {
        Object[] rtl = {"a", "b", "c"};
        Bidi.reorderVisually(new byte[]{1, 1, 1}, 0, rtl, 0, 3);
        check("c".equals(rtl[0]) && "a".equals(rtl[2]), "reorderVisually RTL reverses");

        Object[] ltr = {"a", "b", "c"};
        Bidi.reorderVisually(new byte[]{0, 0, 0}, 0, ltr, 0, 3);
        check("a".equals(ltr[0]) && "c".equals(ltr[2]), "reorderVisually LTR unchanged");
    }

    static void testCreateLineBidi() {
        Bidi full = new Bidi("abc def", Bidi.DIRECTION_LEFT_TO_RIGHT);
        Bidi line = full.createLineBidi(0, 3);
        check(line.getLength() == 3, "line length 3");
        check(line.getRunCount() == 1, "line run count 1");
        check(line.isLeftToRight(), "line is LTR");
        check(line.getRunLevel(0) == 0, "line run level 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testLeftToRight();
        testRightToLeft();
        testMixedRuns();
        testRequiresBidi();
        testReorderVisually();
        testCreateLineBidi();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
