/*
 * @test
 * @summary java.util.OptionalLong API conformance: of/empty, isPresent, getAsLong, and orElse.
 * @run main OptionalLongTest
 */

import java.util.OptionalLong;

public class OptionalLongTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOptionalLong() {
        OptionalLong present = OptionalLong.of(10L);
        check(present.isPresent() && present.getAsLong() == 10L, "of/isPresent/getAsLong");
        check(present.orElse(99L) == 10L, "present orElse");
        OptionalLong empty = OptionalLong.empty();
        check(!empty.isPresent(), "empty isPresent");
        check(empty.orElse(99L) == 99L, "empty orElse");
    }

    public static void main(String[] args) {
        testOptionalLong();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
