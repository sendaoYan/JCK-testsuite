/*
 * @test
 * @summary java.util.OptionalDouble API conformance: of/empty, isPresent, getAsDouble, and
 *          orElse.
 * @run main OptionalDoubleTest
 */

import java.util.OptionalDouble;

public class OptionalDoubleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOptionalDouble() {
        OptionalDouble present = OptionalDouble.of(2.5);
        check(present.isPresent() && present.getAsDouble() == 2.5, "of/isPresent/getAsDouble");
        check(present.orElse(9.0) == 2.5, "present orElse");
        OptionalDouble empty = OptionalDouble.empty();
        check(!empty.isPresent(), "empty isPresent");
        check(empty.orElse(9.0) == 9.0, "empty orElse");
    }

    public static void main(String[] args) {
        testOptionalDouble();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
