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
    // ---- Part 3: deep contract (lifecycle, thread confinement, allocation validation) ----
    static void testLifecycle() {
        // access after close -> IllegalStateException (measured), even for reads
        Arena a = Arena.ofConfined();
        MemorySegment seg = a.allocate(8, 8);
        seg.set(ValueLayout.JAVA_LONG, 0, 5L);
        a.close();
        try {
            seg.get(ValueLayout.JAVA_LONG, 0);
            check(false, "get after close must throw");
        } catch (IllegalStateException expected) {
            check(true, "get after close -> IllegalStateException");
        }
        try {
            seg.set(ValueLayout.JAVA_LONG, 0, 6L);
            check(false, "set after close must throw");
        } catch (IllegalStateException expected) {
            check(true, "set after close -> IllegalStateException");
        }
        try {
            a.close();
            check(false, "second close must throw");
        } catch (IllegalStateException expected) {
            check(true, "second close -> IllegalStateException");
        }

        // try-with-resources closes exactly once; access after manual close inside fails
        try (Arena c = Arena.ofConfined()) {
            MemorySegment s = c.allocate(8, 8);
            c.close();                       // manual close inside the block
            try {
                s.get(ValueLayout.JAVA_BYTE, 0);
                check(false, "access after manual close must throw");
            } catch (IllegalStateException expected) {
                check(true, "ISE after manual close inside try-with-resources");
            }
        } catch (IllegalStateException expected) {
            check(true, "implicit double close of try-with-resources -> IllegalStateException");
        }
    }

    static void testConfinement() throws Exception {
        // confined segments reject other threads with WrongThreadException (measured)
        Arena a = Arena.ofConfined();
        MemorySegment seg = a.allocate(8, 8);
        final Throwable[] hit = new Throwable[1];
        Thread t = new Thread(() -> {
            try { seg.get(ValueLayout.JAVA_LONG, 0); }
            catch (Throwable x) { hit[0] = x; }
        });
        t.start();
        t.join();
        check(hit[0] instanceof WrongThreadException,
              "confined cross-thread access -> WrongThreadException, got " + hit[0]);
        a.close();

        // shared arena: value written by a worker is visible to the main thread
        try (Arena sh = Arena.ofShared()) {
            MemorySegment sv = sh.allocate(8, 8);
            Thread w = new Thread(() -> sv.set(ValueLayout.JAVA_LONG, 0, 4242L));
            w.start();
            w.join();
            check(sv.get(ValueLayout.JAVA_LONG, 0) == 4242L,
                  "shared arena: cross-thread write is visible");
        }
    }

    static void testAllocationContract() {
        try (Arena a = Arena.ofConfined()) {
            // alignment is honored exactly: address % alignment == 0 across the ladder
            for (long align : new long[]{1, 2, 4, 8, 16, 64}) {
                MemorySegment s = a.allocate(8, align);
                check(s.address() % align == 0, "allocate(8," + align + ") honors alignment");
                check(s.byteSize() == 8, "allocate(8," + align + ") honors size");
                check(s.maxByteAlignment() >= align,
                      "maxByteAlignment(" + align + ") >= requested alignment");
                long mba = s.maxByteAlignment();
                check((mba & (mba - 1)) == 0, "maxByteAlignment is a power of two");
            }
            // negative arguments
            try { a.allocate(-1, 1); check(false, "negative size rejected"); }
            catch (IllegalArgumentException expected) { check(true, "allocate(-1,1) -> IAE"); }
            try { a.allocate(1, 0); check(false, "zero alignment rejected"); }
            catch (IllegalArgumentException expected) { check(true, "allocate(1,0) -> IAE"); }
            try { a.allocate(1, 3); check(false, "non-power-of-two alignment rejected"); }
            catch (IllegalArgumentException expected) { check(true, "allocate(1,3) -> IAE"); }
            // zero size is legal and yields a zero-length (but live) segment
            MemorySegment zero = a.allocate(0, 1);
            check(zero.byteSize() == 0, "allocate(0,1) yields zero size");
            // fresh native memory is zero-initialized
            MemorySegment fresh = a.allocate(16, 8);
            boolean allZero = true;
            for (long i = 0; i < 16; i++) allZero &= fresh.get(ValueLayout.JAVA_BYTE, i) == 0;
            check(allZero, "freshly allocated segment is zero-initialized");
            // SegmentAllocator default methods on an Arena
            check(a.allocate(ValueLayout.JAVA_INT).byteSize() == 4, "allocate(layout INT) size 4");
            check(a.allocate(ValueLayout.JAVA_INT, 3).byteSize() == 12, "allocate(INT,3) size 12");
        }
    }

    static void testDeepBlackBox() throws Exception {
        // Arena instances implement both of its interfaces (measured instanceof)
        Arena g = Arena.global();
        check(g instanceof AutoCloseable, "Arena instance is AutoCloseable");
        check(g instanceof SegmentAllocator, "Arena instance is SegmentAllocator");
        check(g.allocate(8, 8).address() % 8 == 0, "global arena honors alignment too");
        testLifecycle();
        testConfinement();
        testAllocationContract();
    }

public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
