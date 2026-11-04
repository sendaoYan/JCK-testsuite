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

    public static void main(String[] args) throws Exception {
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
