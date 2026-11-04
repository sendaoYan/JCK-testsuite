/*
 * @test
 * @summary java.util.OptionalInt API conformance: of/empty, isPresent, getAsInt, and orElse.
 * @run main OptionalIntTest
 */

import java.util.OptionalInt;

public class OptionalIntTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOptionalInt() {
        OptionalInt present = OptionalInt.of(5);
        check(present.isPresent() && present.getAsInt() == 5, "of/isPresent/getAsInt");
        check(present.orElse(9) == 5, "present orElse");
        OptionalInt empty = OptionalInt.empty();
        check(!empty.isPresent(), "empty isPresent");
        check(empty.orElse(9) == 9, "empty orElse");
    }

    public static void main(String[] args) {
        testOptionalInt();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
