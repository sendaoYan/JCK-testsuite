/*
 * @test
 * @summary java.lang.foreign.MemorySegment.Scope conformance: white-box structure + black-box behavior.
 * @run main ScopeTest
 */
import java.lang.foreign.*;

public class ScopeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.MemorySegment$Scope");
        check(c.isInterface(), "interface");
        check(c.getMethod("isAlive").getReturnType() == boolean.class, "isAlive returns boolean");
        check(c.getMethod("equals", Object.class).getReturnType() == boolean.class, "equals returns boolean");
        check(c.getMethod("hashCode").getReturnType() == int.class, "hashCode returns int");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Arena a = Arena.ofConfined();
        MemorySegment.Scope sc = a.scope();
        check(sc.isAlive(), "scope alive");
        check(sc.equals(sc), "scope equals itself");
        check(sc.hashCode() == sc.hashCode(), "scope hashCode stable");

        MemorySegment seg = a.allocate(8, 8);
        check(seg.scope().equals(sc), "segment shares the arena scope");
        check(seg.scope().isAlive(), "segment scope alive");

        Arena b = Arena.ofConfined();
        check(!sc.equals(b.scope()), "different arenas have different scopes");

        a.close();
        check(!sc.isAlive(), "scope dead after close");
        b.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
