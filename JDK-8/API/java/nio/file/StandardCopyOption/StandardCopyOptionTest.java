/*
 * @test
 * @summary java.nio.file.StandardCopyOption conformance: enum constants.
 * @run main StandardCopyOptionTest
 */

import java.nio.file.StandardCopyOption;

public class StandardCopyOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStandardCopyOption() {
        check(StandardCopyOption.valueOf("REPLACE_EXISTING") == StandardCopyOption.REPLACE_EXISTING, "valueOf REPLACE_EXISTING");
        check(StandardCopyOption.valueOf("COPY_ATTRIBUTES") == StandardCopyOption.COPY_ATTRIBUTES, "valueOf COPY_ATTRIBUTES");
        check(StandardCopyOption.valueOf("ATOMIC_MOVE") == StandardCopyOption.ATOMIC_MOVE, "valueOf ATOMIC_MOVE");
        check(StandardCopyOption.values().length >= 3, "at least 3 values");
        check(StandardCopyOption.class.isEnum(), "is enum");
    }

    public static void main(String[] args) {
        testStandardCopyOption();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
