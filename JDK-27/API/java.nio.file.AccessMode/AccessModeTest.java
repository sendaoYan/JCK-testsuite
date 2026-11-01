/*
 * @test
 * @summary java.nio.file.AccessMode conformance: enum constants.
 * @run main AccessModeTest
 */

import java.nio.file.AccessMode;

public class AccessModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAccessMode() {
        check(AccessMode.valueOf("READ") == AccessMode.READ, "valueOf READ");
        check(AccessMode.valueOf("WRITE") == AccessMode.WRITE, "valueOf WRITE");
        check(AccessMode.valueOf("EXECUTE") == AccessMode.EXECUTE, "valueOf EXECUTE");
        check(AccessMode.values().length >= 3, "at least 3 values");
        check(AccessMode.class.isEnum(), "is enum");
    }

    public static void main(String[] args) {
        testAccessMode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
