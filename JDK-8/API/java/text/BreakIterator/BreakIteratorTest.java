/*
 * @test
 * @summary java.text.BreakIterator conformance: white-box structure + black-box word/sentence/line/character boundaries.
 * @run main BreakIteratorTest
 */

import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.Arrays;
import java.util.Locale;

public class BreakIteratorTest {
    static int passed;
    static final Locale US = Locale.US;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static int[] boundaries(BreakIterator bi, String text) {
        bi.setText(text);
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i = bi.first(); i != BreakIterator.DONE; i = bi.next()) list.add(i);
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    static void checkBoundaries(BreakIterator bi, String text, int... expected) {
        int[] got = boundaries(bi, text);
        check(Arrays.equals(got, expected), "boundaries(" + text + ") = " + Arrays.toString(got)
              + " expected " + Arrays.toString(expected));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.BreakIterator");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(c.getField("DONE").getInt(null) == -1, "DONE=-1");
        check(BreakIterator.getWordInstance() instanceof BreakIterator, "getWordInstance() non-null");
        check(BreakIterator.getLineInstance() instanceof BreakIterator, "getLineInstance() non-null");
        check(BreakIterator.getCharacterInstance() instanceof BreakIterator, "getCharacterInstance() non-null");
        check(BreakIterator.getSentenceInstance() instanceof BreakIterator, "getSentenceInstance() non-null");
        check(BreakIterator.getAvailableLocales().length > 0, "getAvailableLocales non-empty");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCharacterBoundaries() {
        checkBoundaries(BreakIterator.getCharacterInstance(US), "abc", 0, 1, 2, 3);
        checkBoundaries(BreakIterator.getCharacterInstance(US), "a\u0301", 0, 2); // combining accent is one grapheme
    }

    static void testWordBoundaries() {
        checkBoundaries(BreakIterator.getWordInstance(US), "hello world", 0, 5, 6, 11);
    }

    static void testSentenceBoundaries() {
        checkBoundaries(BreakIterator.getSentenceInstance(US), "Hello. World.", 0, 7, 13);
    }

    static void testLineBoundaries() {
        checkBoundaries(BreakIterator.getLineInstance(US), "hello world", 0, 6, 11);
    }

    static void testNavigation() {
        BreakIterator bi = BreakIterator.getWordInstance(US);
        bi.setText("hello world");
        check(bi.first() == 0, "first()=0");
        check(bi.next() == 5, "next()=5");
        check(bi.next() == 6, "next()=6");
        check(bi.next() == 11, "next()=11");
        check(bi.next() == BreakIterator.DONE, "next()=DONE");
        check(bi.last() == 11, "last()=11");
        check(bi.previous() == 6, "previous()=6");
        check(bi.following(3) == 5, "following(3)=5");
        check(bi.current() == 5, "current()=5 after following");
        check(bi.preceding(8) == 6, "preceding(8)=6");
        check(bi.isBoundary(5) && bi.isBoundary(6), "isBoundary at word edges");
        check(!bi.isBoundary(3), "!isBoundary inside word");

        BreakIterator bi2 = BreakIterator.getWordInstance(US);
        bi2.setText("hello world");
        bi2.first();
        check(bi2.next(2) == 6, "next(2) from first = 6");
    }

    static void testTextAndClone() {
        BreakIterator bi = BreakIterator.getCharacterInstance(US);
        bi.setText("abc");
        CharacterIterator ci = bi.getText();
        check(ci.first() == 'a' && ci.last() == 'c', "getText CharacterIterator");

        BreakIterator clone = (BreakIterator) bi.clone();
        check(clone != null, "clone non-null");
        check(Arrays.equals(boundaries(clone, "abc"), new int[]{0, 1, 2, 3}), "clone produces same boundaries");
    }

    // Minimal concrete subclass to exercise the protected BreakIterator() constructor
    // and the non-abstract methods inherited from the abstract class.
    static class MinimalBreakIterator extends BreakIterator {
        public int first() { return BreakIterator.DONE; }
        public int last() { return BreakIterator.DONE; }
        public int next(int n) { return BreakIterator.DONE; }
        public int next() { return BreakIterator.DONE; }
        public int previous() { return BreakIterator.DONE; }
        public int following(int offset) { return BreakIterator.DONE; }
        public int current() { return 0; }
        public CharacterIterator getText() { return null; }
        public void setText(CharacterIterator newText) { }
    }

    static void testBlackBox() {
        MinimalBreakIterator bi = new MinimalBreakIterator();
        check(bi != null, "protected constructor via subclass");
        check(bi.first() == BreakIterator.DONE, "subclass first()");
        check(bi.last() == BreakIterator.DONE, "subclass last()");
        check(bi.next() == BreakIterator.DONE, "subclass next()");
        check(bi.next(2) == BreakIterator.DONE, "subclass next(int)");
        check(bi.previous() == BreakIterator.DONE, "subclass previous()");
        check(bi.following(0) == BreakIterator.DONE, "subclass following(int)");
        check(bi.current() == 0, "subclass current()");
        bi.setText("abc"); // non-abstract setText(String)
        check(bi.getText() == null, "subclass getText()");
        Object clone = bi.clone();
        check(clone != null && clone.getClass() == MinimalBreakIterator.class, "clone of subclass");
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
    // text = "Hello, world. Foo bar.\nBaz qux, 42." (36 chars, boundaries locked by probe on both JDKs)

    static final String BI_TEXT = "Hello, world. Foo bar.\nBaz qux, 42.";

    static String boundarySeq(BreakIterator bi, String text) {
        bi.setText(text);
        StringBuilder sb = new StringBuilder();
        sb.append(bi.first());
        for (int nx = bi.next(); nx != BreakIterator.DONE; nx = bi.next()) sb.append(",").append(nx);
        return sb.toString();
    }

    static String boundarySeqRev(BreakIterator bi, String text) {
        bi.setText(text);
        StringBuilder sb = new StringBuilder();
        sb.append(bi.last());
        for (int pv = bi.previous(); pv != BreakIterator.DONE; pv = bi.previous()) sb.append(",").append(pv);
        return sb.toString();
    }

    static void testWordBoundaryTableContract() {
        check("0,5,6,7,12,13,14,17,18,21,22,23,26,27,30,31,32,34,35"
              .equals(boundarySeq(BreakIterator.getWordInstance(Locale.ENGLISH), BI_TEXT)),
              "word next boundary table");
        check("35,34,32,31,30,27,26,23,22,21,18,17,14,13,12,7,6,5,0"
              .equals(boundarySeqRev(BreakIterator.getWordInstance(Locale.ENGLISH), BI_TEXT)),
              "word previous boundary table");
        BreakIterator wi = BreakIterator.getWordInstance(Locale.ENGLISH);
        wi.setText(BI_TEXT);
        check(wi.following(6) == 7, "word following(6) == 7");
        check(wi.preceding(20) == 18, "word preceding(20) == 18");
        check(wi.current() == 18, "current tracks preceding");
        check(wi.isBoundary(5) && !wi.isBoundary(3), "isBoundary 5 true / 3 false");
        wi.first();
        check(wi.next(2) == 6, "next(2) from first skips two boundaries");
    }

    static void testLineBoundaryTableContract() {
        check("0,7,14,18,23,27,32,35"
              .equals(boundarySeq(BreakIterator.getLineInstance(Locale.ENGLISH), BI_TEXT)),
              "line next boundary table");
        check("35,32,27,23,18,14,7,0"
              .equals(boundarySeqRev(BreakIterator.getLineInstance(Locale.ENGLISH), BI_TEXT)),
              "line previous boundary table");
        BreakIterator li = BreakIterator.getLineInstance(Locale.ENGLISH);
        li.setText(BI_TEXT);
        check(li.following(6) == 7, "line following(6) == 7");
        check(li.preceding(20) == 18, "line preceding(20) == 18");
    }

    static void testSentenceBoundaryTableContract() {
        check("0,14,23,35"
              .equals(boundarySeq(BreakIterator.getSentenceInstance(Locale.ENGLISH), BI_TEXT)),
              "sentence next boundary table");
        check("35,23,14,0"
              .equals(boundarySeqRev(BreakIterator.getSentenceInstance(Locale.ENGLISH), BI_TEXT)),
              "sentence previous boundary table");
        BreakIterator si = BreakIterator.getSentenceInstance(Locale.ENGLISH);
        si.setText(BI_TEXT);
        check(si.following(6) == 14, "sentence following(6) == 14");
        check(si.preceding(20) == 14, "sentence preceding(20) == 14");
        check(si.current() == 14, "sentence current after preceding");
    }

    static void testCharacterBoundaryTableContract() {
        StringBuilder want = new StringBuilder("0");
        for (int i = 1; i <= 35; i++) want.append(",").append(i);
        check(want.toString()
              .equals(boundarySeq(BreakIterator.getCharacterInstance(Locale.ENGLISH), BI_TEXT)),
              "character next: every code point is a boundary");
        StringBuilder wantRev = new StringBuilder("35");
        for (int i = 34; i >= 0; i--) wantRev.append(",").append(i);
        check(wantRev.toString()
              .equals(boundarySeqRev(BreakIterator.getCharacterInstance(Locale.ENGLISH), BI_TEXT)),
              "character previous: full reverse");
        BreakIterator ci = BreakIterator.getCharacterInstance(Locale.ENGLISH);
        ci.setText(BI_TEXT);
        check(ci.following(6) == 7, "character following(6) == 7");
        check(ci.preceding(20) == 19, "character preceding(20) == 19");
    }

    static void testEdgeAndStateContract() {
        BreakIterator wi = BreakIterator.getWordInstance(Locale.ENGLISH);
        wi.setText(BI_TEXT);
        check(BreakIterator.DONE == -1, "DONE == -1");
        check(wi.following(34) == 35, "following(last-1) == end");
        check(wi.following(35) == BreakIterator.DONE, "following(end) == DONE");
        expectX(IllegalArgumentException.class, () -> wi.following(36), "following(end+1) -> IAE");
        expectX(IllegalArgumentException.class, () -> wi.following(-1), "following(-1) -> IAE");
        check(wi.preceding(35) == 34, "preceding(end) == last boundary before end");
        check(wi.preceding(0) == BreakIterator.DONE, "preceding(0) == DONE");
        expectX(IllegalArgumentException.class, () -> wi.preceding(-1), "preceding(-1) -> IAE");
        check(wi.last() == 35 && wi.current() == 35, "last == current == 35");
        check(wi.next() == BreakIterator.DONE, "next after last DONE");
        wi.setText(new java.text.StringCharacterIterator(BI_TEXT));
        check(wi.first() == 0 && wi.next() == 5, "setText(CharacterIterator) accepted");
        check(wi.getText() != null && wi.getText().getBeginIndex() == 0
              && wi.getText().getEndIndex() == 35, "getText round-trips bounds");
        expectX(NullPointerException.class, () -> wi.setText((String) null),
                "setText(String null) -> NPE");
        expectX(NullPointerException.class, () -> wi.setText((CharacterIterator) null),
                "setText(CharacterIterator null) -> NPE");
        BreakIterator cl = (BreakIterator) BreakIterator.getWordInstance(Locale.ENGLISH).clone();
        check(cl != null, "clone supported");
        check(BreakIterator.getAvailableLocales().length > 0, "getAvailableLocales non-empty");
        check(Arrays.asList(BreakIterator.getAvailableLocales()).contains(Locale.ENGLISH),
              "available contains ENGLISH");
    }
    public static void main(String[] args) throws Exception {
        testWordBoundaryTableContract();
        testLineBoundaryTableContract();
        testSentenceBoundaryTableContract();
        testCharacterBoundaryTableContract();
        testEdgeAndStateContract();
        testWhiteBox();
        testCharacterBoundaries();
        testWordBoundaries();
        testSentenceBoundaries();
        testLineBoundaries();
        testNavigation();
        testTextAndClone();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
