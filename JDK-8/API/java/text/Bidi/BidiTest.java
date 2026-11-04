/*
 * @test
 * @summary java.text.Bidi conformance: white-box structure + black-box direction/run/reorder behavior.
 * @run main BidiTest
 */

import java.text.AttributedString;
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
        Bidi b = new Bidi("\u05D0\u05D1", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT); // Hebrew alef bet
        check(b.isRightToLeft() && !b.isLeftToRight(), "isRightToLeft");
        check(!b.baseIsLeftToRight(), "baseIsLeftToRight false");
        check(b.getBaseLevel() == 1, "getBaseLevel 1");
        check(b.getRunLevel(0) == 1, "getRunLevel 1");
        check(b.getRunCount() == 1, "getRunCount 1");
        check(!b.isMixed(), "not mixed");
    }

    static void testMixedRuns() {
        Bidi b = new Bidi("a\u05D0b", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT); // a alef b
        check(b.isMixed(), "isMixed true");
        check(b.getRunCount() == 3, "getRunCount 3");
        check(b.getLevelAt(0) == 0 && b.getLevelAt(1) == 1 && b.getLevelAt(2) == 0, "getLevelAt per char");
        check(b.getRunStart(0) == 0 && b.getRunLimit(0) == 1, "run0 start/limit");
    }

    static void testRequiresBidi() {
        check(!Bidi.requiresBidi(new char[]{'a', 'b', 'c'}, 0, 3), "requiresBidi ascii false");
        check(Bidi.requiresBidi(new char[]{'\u05D0', '\u05D1'}, 0, 2), "requiresBidi Hebrew true");
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

    static void testBlackBox() {
        // char[] constructor with explicit range and no embeddings
        char[] text = "abc".toCharArray();
        Bidi cb = new Bidi(text, 0, null, 0, 3, Bidi.DIRECTION_LEFT_TO_RIGHT);
        check(cb.isLeftToRight() && !cb.isRightToLeft(), "Bidi(char[],...) is LTR");
        check(cb.getLength() == 3, "Bidi(char[],...) length 3");
        check(cb.getRunCount() == 1, "Bidi(char[],...) single run");
        check(cb.getBaseLevel() == 0, "Bidi(char[],...) base level 0");

        // char[] constructor with explicit embedding levels
        Bidi eb = new Bidi(text, 0, new byte[]{0, 0, 0}, 0, 3, Bidi.DIRECTION_LEFT_TO_RIGHT);
        check(eb.isLeftToRight(), "Bidi(char[],embeddings) LTR");
        check(eb.getLevelAt(0) == 0 && eb.getLevelAt(2) == 0, "Bidi(char[],embeddings) levels");

        // AttributedCharacterIterator constructor (plain LTR text)
        Bidi ab = new Bidi(new AttributedString("abc").getIterator());
        check(ab.isLeftToRight() && !ab.isRightToLeft(), "Bidi(AttributedCharacterIterator) LTR");
        check(ab.getLength() == 3, "Bidi(AttributedCharacterIterator) length");
        check(ab.getRunCount() == 1, "Bidi(AttributedCharacterIterator) single run");

        // AttributedCharacterIterator constructor on mixed text
        Bidi mb = new Bidi(new AttributedString("a\u05D0b").getIterator()); // a alef b
        check(mb.isMixed(), "Bidi(AttributedCharacterIterator) mixed");
        check(mb.getRunCount() == 3, "Bidi(AttributedCharacterIterator) 3 runs");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================
    // samples: pure LTR, pure RTL, mixed "abc <alef bet> def", numeric "123 <alef bet> 456"

    static void testRunStructureTableContract() {
        Bidi ltr = new Bidi("abc", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        check(ltr.getBaseLevel() == 0 && ltr.getRunCount() == 1 && ltr.getLength() == 3,
              "pure LTR: base 0, single run");
        check(ltr.getRunStart(0) == 0 && ltr.getRunLimit(0) == 3 && ltr.getRunLevel(0) == 0,
              "pure LTR run [0,3) level 0");
        Bidi rtl = new Bidi("\u05D0\u05D1\u05D2", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        check(rtl.getBaseLevel() == 1 && rtl.getRunCount() == 1 && rtl.getLength() == 3,
              "pure RTL text gets base level 1 even under DEFAULT_LTR");
        check(rtl.getRunStart(0) == 0 && rtl.getRunLimit(0) == 3 && rtl.getRunLevel(0) == 1,
              "pure RTL run [0,3) level 1");
        check(rtl.isRightToLeft() && ltr.isLeftToRight(), "classification predicates");
        Bidi mixed = new Bidi("abc \u05D0\u05D1 def", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        check(mixed.getBaseLevel() == 0 && mixed.getRunCount() == 3 && mixed.isMixed(),
              "mixed: base 0, three runs, isMixed");
        check(mixed.getRunStart(0) == 0 && mixed.getRunLimit(0) == 4 && mixed.getRunLevel(0) == 0,
              "mixed run0 [0,4) level 0");
        check(mixed.getRunStart(1) == 4 && mixed.getRunLimit(1) == 6 && mixed.getRunLevel(1) == 1,
              "mixed run1 [4,6) level 1");
        check(mixed.getRunStart(2) == 6 && mixed.getRunLimit(2) == 10 && mixed.getRunLevel(2) == 0,
              "mixed run2 [6,10) level 0");
    }

    static void testFlagMatrixContract() {
        String mixed = "abc \u05D0\u05D1 def";
        Bidi defLtr = new Bidi(mixed, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        check(defLtr.getRunCount() == 3 && defLtr.getRunLimit(1) == 6 && defLtr.getRunLevel(1) == 1,
              "DEFAULT_LEFT_TO_RIGHT on neutral+RTL: same runs as LTR");
        Bidi forcedLtr = new Bidi(mixed, Bidi.DIRECTION_LEFT_TO_RIGHT);
        check(forcedLtr.getBaseLevel() == 0 && forcedLtr.getRunCount() == 3
              && forcedLtr.getRunStart(1) == 4 && forcedLtr.getRunLevel(1) == 1,
              "LEFT_TO_RIGHT flag: base 0, embedded RTL still level 1");
        Bidi forcedRtl = new Bidi(mixed, Bidi.DIRECTION_RIGHT_TO_LEFT);
        check(forcedRtl.getBaseLevel() == 1 && forcedRtl.getRunCount() == 3,
              "RIGHT_TO_LEFT flag: base 1");
        check(forcedRtl.getRunStart(0) == 0 && forcedRtl.getRunLimit(0) == 3
              && forcedRtl.getRunLevel(0) == 2,
              "RTL base: LTR text embedded at level 2 (odd+1)");
        check(forcedRtl.getRunStart(1) == 3 && forcedRtl.getRunLimit(1) == 7
              && forcedRtl.getRunLevel(1) == 1, "RTL base run1 [3,7) level 1");
        check(forcedRtl.getRunStart(2) == 7 && forcedRtl.getRunLimit(2) == 10
              && forcedRtl.getRunLevel(2) == 2, "RTL base run2 [7,10) level 2");
        Bidi num = new Bidi("123 \u05D0\u05D1 456", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        check(num.getBaseLevel() == 1 && num.getRunCount() == 3,
              "leading digits in RTL paragraph resolve to base 1");
        check(num.getRunStart(0) == 0 && num.getRunLimit(0) == 3 && num.getRunLevel(0) == 2,
              "numeric run0 [0,3) level 2");
        check(num.getRunStart(1) == 3 && num.getRunLimit(1) == 7 && num.getRunLevel(1) == 1,
              "numeric run1 [3,7) level 1");
        check(num.getRunStart(2) == 7 && num.getRunLimit(2) == 10 && num.getRunLevel(2) == 2,
              "numeric run2 [7,10) level 2");
    }

    static void testLevelAtTableContract() {
        Bidi mixed = new Bidi("abc \u05D0\u05D1 def", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        String levels = "";
        for (int i = 0; i <= mixed.getLength(); i++) levels += mixed.getLevelAt(i);
        check("00001100000".equals(levels),
              "getLevelAt table 0..len (index 10 = base level)");
    }

    static void testConstructorAndLineContract() {
        String mixed = "abc \u05D0\u05D1 def";
        char[] cs = mixed.toCharArray();
        Bidi bc = new Bidi(cs, 0, null, 0, cs.length, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        check(bc.getRunCount() == 3 && bc.getBaseLevel() == 0,
              "char[] ctor yields same resolution as String ctor");
        Bidi b = new Bidi(mixed, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        Bidi line = b.createLineBidi(0, 4);
        check(line.getRunCount() == 1 && line.getBaseLevel() == 0,
              "createLineBidi(0,4): pure LTR slice, one run");
        expectX(IllegalArgumentException.class, () -> b.createLineBidi(-1, 2),
                "createLineBidi(negative) -> IAE");
        expectX(IllegalArgumentException.class, () -> b.createLineBidi(0, 99),
                "createLineBidi(beyond length) -> IAE");
        expectX(IllegalArgumentException.class, () -> b.createLineBidi(5, 2),
                "createLineBidi(start>end) -> IAE");
        expectX(IllegalArgumentException.class, () -> new Bidi((String) null, Bidi.DIRECTION_LEFT_TO_RIGHT),
                "Bidi(null, flag) -> IAE");
        check(new Bidi("abc", 99).getBaseLevel() == 0,
              "undefined flag values do not throw (masked)");
        check(new Bidi("abc", -2).getBaseLevel() == 0, "negative flag -2 tolerated");
        check(!Bidi.requiresBidi("abc".toCharArray(), 0, 3),
              "requiresBidi false for pure LTR");
        check(Bidi.requiresBidi(mixed.toCharArray(), 0, mixed.length()),
              "requiresBidi true for mixed");
        expectX(NullPointerException.class, () -> Bidi.requiresBidi(null, 0, 1),
                "requiresBidi(null) -> NPE");
        expectX(IllegalArgumentException.class, () -> Bidi.requiresBidi("abc".toCharArray(), 5, 1),
                "requiresBidi bad range -> IAE");
        Bidi viaAttr = new Bidi(new java.text.AttributedString("a\u05D0b").getIterator());
        check(viaAttr.isMixed() && viaAttr.getRunCount() >= 2,
              "AttributedCharacterIterator ctor resolves embedded RTL");
    }
    public static void main(String[] args) throws Exception {
        testRunStructureTableContract();
        testFlagMatrixContract();
        testLevelAtTableContract();
        testConstructorAndLineContract();
        testWhiteBox();
        testLeftToRight();
        testRightToLeft();
        testMixedRuns();
        testRequiresBidi();
        testReorderVisually();
        testCreateLineBidi();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
