/*
 * @test
 * @summary java.lang.foreign.Arena conformance: white-box structure + black-box behavior.
 * @run main ArenaTest
 */
import java.lang.foreign.*;

public class ArenaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.Arena");
        check(c.isInterface(), "interface");
        check(SegmentAllocator.class.isAssignableFrom(c), "extends SegmentAllocator");
        check(AutoCloseable.class.isAssignableFrom(c), "extends AutoCloseable");
        for (String m : new String[]{"ofAuto", "global", "ofConfined", "ofShared"})
            check(c.getMethod(m).getReturnType() == Arena.class, m + "() returns Arena");
        check(c.getMethod("allocate", long.class, long.class).getReturnType() == MemorySegment.class,
              "allocate(long,long) returns MemorySegment");
        check(c.getMethod("scope").getReturnType() == MemorySegment.Scope.class, "scope() returns Scope");
        check(c.getMethod("close").getReturnType() == void.class, "close() returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testConfined() {
        Arena a = Arena.ofConfined();
        MemorySegment.Scope sc = a.scope();
        check(sc.isAlive(), "confined scope alive before close");
        MemorySegment seg = a.allocate(16, 8);
        check(seg.byteSize() == 16, "allocate honors byteSize");
        check(seg.isNative(), "arena segment is native");
        check(seg.address() != 0, "native segment has non-zero address");
        check(seg.scope().isAlive(), "segment scope alive");
        check(seg.isAccessibleBy(Thread.currentThread()), "confined segment accessible by owner thread");
        a.close();
        check(!sc.isAlive(), "scope dead after close");
        check(seg.scope() == sc, "segment shares arena scope");
    }

    static void testGlobal() {
        Arena g = Arena.global();
        MemorySegment seg = g.allocate(8, 4);
        check(seg.byteSize() == 8, "global allocate size");
        check(g.scope().isAlive(), "global scope always alive");
        try {
            g.close();
            check(false, "global close should throw");
        } catch (UnsupportedOperationException expected) {
            check(true, "global close throws UnsupportedOperationException");
        }
        check(g.scope().isAlive(), "global scope still alive after close attempt");
    }

    static void testShared() {
        Arena s = Arena.ofShared();
        MemorySegment seg = s.allocate(32, 8);
        check(seg.isAccessibleBy(Thread.currentThread()), "shared segment accessible by any thread");
        final boolean[] other = {false};
        Thread t = new Thread(() -> other[0] = seg.isAccessibleBy(Thread.currentThread()));
        t.start();
        try { t.join(); } catch (InterruptedException e) { throw new AssertionError(e); }
        check(other[0], "shared segment accessible by another thread");
        s.close();
    }

    static void testAuto() {
        Arena a = Arena.ofAuto();
        MemorySegment seg = a.allocate(16, 8);
        check(seg.scope().isAlive(), "auto arena segment alive");
        try {
            a.close();
            check(false, "ofAuto close should throw");
        } catch (UnsupportedOperationException expected) {
            check(true, "ofAuto close throws UnsupportedOperationException");
        }
        check(a.scope().isAlive(), "auto arena scope still alive");
    }

    
    static void testBlackBox() throws Exception {
        testConfined();
        testGlobal();
        testShared();
        testAuto();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
