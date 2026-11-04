/*
 * @test
 * @summary JVMS 2.3.4: the boolean type has the values true and false; on the
 *          operand stack and in local variables it is encoded as an int (0/1)
 *          and in a boolean array as bytes, so iconst/bastore/baload round-trip
 *          a boolean value correctly.
 * @compile BooleanInternalRepresentation.jasm
 * @run main BooleanInternalRepresentationTest
 */

public class BooleanInternalRepresentationTest {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("BooleanInternalRepresentation");

        check(Boolean.FALSE.equals(c.getMethod("falseInt").invoke(null)),
                "iconst_0 encodes false");
        check(Boolean.TRUE.equals(c.getMethod("trueInt").invoke(null)),
                "iconst_1 encodes true");
        check(Boolean.TRUE.equals(
                c.getMethod("arrayRoundTrip", boolean.class).invoke(null, true)),
                "true survives a boolean-array round-trip");
        check(Boolean.FALSE.equals(
                c.getMethod("arrayRoundTrip", boolean.class).invoke(null, false)),
                "false survives a boolean-array round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
