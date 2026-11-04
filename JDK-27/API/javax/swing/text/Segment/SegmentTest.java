/*
 * @test
 * @summary javax.swing.text.Segment conformance: char-array view / CharacterIterator / CharSequence / clone (headless-safe).
 * @run main SegmentTest
 */

import javax.swing.text.Segment;

public class SegmentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface Throwing { void run() throws Throwable; }
    static void expectEx(Class<? extends Throwable> ex, Throwing r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t, t);
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception thrown");
    }

    static void testDefaultConstructor() {
        Segment s = new Segment();
        check(s.array == null, "default array null");
        check(s.offset == 0, "default offset 0");
        check(s.count == 0, "default count 0");
        check(s.length() == 0, "default length 0");
        check(s.getBeginIndex() == 0, "default begin 0");
        check(s.getEndIndex() == 0, "default end 0");
        check("".equals(s.toString()), "default toString empty");
        check(s.isPartialReturn() == false, "default partialReturn false");
        // empty iterator: all navigation returns DONE
        check(s.first() == Segment.DONE, "empty first DONE");
        check(s.last() == Segment.DONE, "empty last DONE");
        check(s.current() == Segment.DONE, "empty current DONE");
        check(s.next() == Segment.DONE, "empty next DONE");
        check(s.previous() == Segment.DONE, "empty previous DONE");
        check(s.setIndex(0) == Segment.DONE, "empty setIndex DONE");
    }

    static void testArrayConstructor() {
        char[] chars = "abcdefgh".toCharArray();
        Segment s = new Segment(chars, 2, 4); // "cdef"
        check(s.array == chars, "array shared reference");
        check(s.offset == 2, "offset 2");
        check(s.count == 4, "count 4");
        check(s.length() == 4, "length 4");
        check(s.getBeginIndex() == 2, "begin 2");
        check(s.getEndIndex() == 6, "end 6");
        check("cdef".equals(s.toString()), "toString cdef");
    }

    static void testCharSequence() {
        char[] chars = "abcdefgh".toCharArray();
        Segment s = new Segment(chars, 2, 4); // "cdef"
        check(s.charAt(0) == 'c', "charAt 0");
        check(s.charAt(3) == 'f', "charAt 3");
        expectEx(StringIndexOutOfBoundsException.class, () -> s.charAt(-1), "charAt -1");
        expectEx(StringIndexOutOfBoundsException.class, () -> s.charAt(4), "charAt 4");

        CharSequence sub = s.subSequence(1, 3); // "de"
        check("de".equals(sub.toString()), "subSequence toString");
        check(sub instanceof Segment, "subSequence is Segment");
        Segment subSeg = (Segment) sub;
        check(subSeg.array == chars, "subSequence shares array");
        check(subSeg.offset == 3, "subSequence offset 3");
        check(subSeg.count == 2, "subSequence count 2");
        check(subSeg.length() == 2, "subSequence length");

        expectEx(StringIndexOutOfBoundsException.class, () -> s.subSequence(-1, 2), "subSequence start<0");
        expectEx(StringIndexOutOfBoundsException.class, () -> s.subSequence(0, 5), "subSequence end>count");
        expectEx(StringIndexOutOfBoundsException.class, () -> s.subSequence(2, 1), "subSequence start>end");
    }

    static void testCharacterIterator() {
        char[] chars = "abcdefgh".toCharArray();
        Segment s = new Segment(chars, 2, 4); // "cdef"
        check(s.first() == 'c', "first c");
        check(s.getIndex() == 2, "index after first");
        check(s.current() == 'c', "current c");
        check(s.next() == 'd', "next d");
        check(s.getIndex() == 3, "index d");
        check(s.next() == 'e', "next e");
        check(s.next() == 'f', "next f");
        check(s.getIndex() == 5, "index f");
        check(s.next() == Segment.DONE, "next past end DONE");
        check(s.getIndex() == 6, "index pinned at end");
        check(s.next() == Segment.DONE, "next again DONE");
        check(s.previous() == 'f', "previous f");
        check(s.getIndex() == 5, "index f after previous");
        check(s.last() == 'f', "last f");
        check(s.current() == 'f', "current f after last");
        check(s.previous() == 'e', "previous e");
        check(s.previous() == 'd', "previous d");
        check(s.previous() == 'c', "previous c");
        check(s.previous() == Segment.DONE, "previous at begin DONE");
        check(s.getIndex() == 2, "index stays at begin");

        check(s.setIndex(2) == 'c', "setIndex begin c");
        check(s.setIndex(5) == 'f', "setIndex 5 f");
        check(s.setIndex(6) == Segment.DONE, "setIndex end DONE");
        check(s.getIndex() == 6, "index after setIndex end");
        expectEx(IllegalArgumentException.class, () -> s.setIndex(1), "setIndex < begin IAE");
        expectEx(IllegalArgumentException.class, () -> s.setIndex(7), "setIndex > end IAE");
    }

    static void testPartialReturn() {
        Segment s = new Segment();
        check(s.isPartialReturn() == false, "partialReturn default false");
        s.setPartialReturn(true);
        check(s.isPartialReturn() == true, "partialReturn true");
        s.setPartialReturn(false);
        check(s.isPartialReturn() == false, "partialReturn false again");
    }

    static void testClone() {
        char[] chars = "abcdefgh".toCharArray();
        Segment s = new Segment(chars, 2, 4);
        Object o = s.clone();
        check(o != null, "clone non-null");
        check(o instanceof Segment, "clone is Segment");
        Segment c = (Segment) o;
        check(c != s, "clone distinct object");
        check(c.array == s.array, "clone shares array");
        check(c.offset == 2 && c.count == 4, "clone same view");
        check("cdef".equals(c.toString()), "clone toString");
        c.count = 0;
        check(s.count == 4, "original count unaffected by clone mutation");
        check(c.length() == 0, "clone count mutated");
    }

    public static void main(String[] args) throws Exception {
        testDefaultConstructor();
        testArrayConstructor();
        testCharSequence();
        testCharacterIterator();
        testPartialReturn();
        testClone();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
