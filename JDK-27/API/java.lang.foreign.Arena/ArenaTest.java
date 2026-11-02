/*
 * @test
 * @summary java.lang.foreign.Arena conformance: ofConfined/ofShared/ofAuto/global factories and allocate.
 * @run main ArenaTest
 */
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class ArenaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(Arena.class.isInterface(), "is interface");
        try (Arena c = Arena.ofConfined()) {
            check(c.scope().isAlive(), "confined alive");
            MemorySegment s = c.allocate(16, 8);
            check(s.byteSize() == 16, "allocate(16,8) size");
            check(s.scope().isAlive(), "segment alive");
            check(c.allocate(4).byteSize() == 4, "allocate(long) size");
        }
        try (Arena sh = Arena.ofShared()) {
            check(sh.scope().isAlive(), "shared alive");
        }
        Arena auto = Arena.ofAuto();
        check(auto.allocate(8).byteSize() == 8, "ofAuto allocate");
        check(Arena.global().scope().isAlive(), "global alive");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
