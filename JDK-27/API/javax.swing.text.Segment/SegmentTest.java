/*
 * @test
 * @summary javax.swing.text.Segment conformance: white-box structure + black-box behavior.
 * @run main SegmentTest
 */

import java.lang.reflect.Modifier;
import java.text.CharacterIterator;
import javax.swing.text.Segment;

public class SegmentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            check(type.isInstance(t), msg + " -> " + t.getClass().getName());
            return;
        }
        check(false, msg + " -> no exception");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Segment");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Cloneable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.text.CharacterIterator"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.lang.CharSequence"), "interface 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // CharSequence view over a char[] slice.
        char[] text = "abcdefgh".toCharArray();
        Segment s = new Segment(text, 2, 4);
        check(s.array == text, "array field references source array");
        check(s.offset == 2, "offset field");
        check(s.count == 4, "count field");
        check(s.toString().equals("cdef"), "toString slice");
        check(s.length() == 4, "length == count");
        check(s.getBeginIndex() == 2, "getBeginIndex == offset");
        check(s.getEndIndex() == 6, "getEndIndex == offset+count");
        check(s.charAt(0) == 'c', "charAt 0");
        check(s.charAt(3) == 'f', "charAt 3");
        check(s.subSequence(1, 3).toString().equals("de"), "subSequence(1,3)");

        // CharacterIterator navigation.
        check(s.first() == 'c', "first");
        check(s.getIndex() == 2, "index after first");
        check(s.last() == 'f', "last");
        check(s.getIndex() == 5, "index after last");
        check(s.current() == 'f', "current after last");
        check(s.setIndex(3) == 'd', "setIndex(3) returns 'd'");
        check(s.getIndex() == 3, "index after setIndex");
        check(s.next() == 'e', "next");
        check(s.previous() == 'd', "previous");
        check(s.setIndex(6) == CharacterIterator.DONE, "setIndex(end) == DONE");
        check(s.getIndex() == 6, "index pinned at end");

        // partialReturn flag.
        check(s.isPartialReturn() == false, "partialReturn default false");
        s.setPartialReturn(true);
        check(s.isPartialReturn() == true, "setPartialReturn true roundtrip");
        s.setPartialReturn(false);
        check(s.isPartialReturn() == false, "setPartialReturn false roundtrip");

        // clone is a distinct object with the same view.
        Segment c = (Segment) s.clone();
        check(c != s, "clone distinct object");
        check(c.array == s.array && c.offset == s.offset && c.count == s.count, "clone shares view fields");
        check(c.toString().equals("cdef"), "clone toString");

        // empty segment (default constructor).
        Segment empty = new Segment();
        check(empty.toString().equals(""), "empty toString");
        check(empty.length() == 0, "empty length");
        check(empty.getBeginIndex() == 0, "empty begin index");
        check(empty.getEndIndex() == 0, "empty end index");
        check(empty.first() == CharacterIterator.DONE, "empty first == DONE");
        check(empty.last() == CharacterIterator.DONE, "empty last == DONE");
        check(empty.setIndex(0) == CharacterIterator.DONE, "empty setIndex == DONE");

        // Exceptions.
        expectThrows(() -> s.charAt(-1), StringIndexOutOfBoundsException.class, "charAt(-1)");
        expectThrows(() -> s.charAt(4), StringIndexOutOfBoundsException.class, "charAt(count)");
        expectThrows(() -> s.subSequence(-1, 2), StringIndexOutOfBoundsException.class, "subSequence negative start");
        expectThrows(() -> s.subSequence(2, 5), StringIndexOutOfBoundsException.class, "subSequence end > count");
        expectThrows(() -> s.subSequence(3, 2), StringIndexOutOfBoundsException.class, "subSequence start > end");
        expectThrows(() -> s.setIndex(1), IllegalArgumentException.class, "setIndex below begin");
        expectThrows(() -> s.setIndex(7), IllegalArgumentException.class, "setIndex above end");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
