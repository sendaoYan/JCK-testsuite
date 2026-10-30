/*
 * @test
 * @summary JVMS 4.11: a method descriptor may name at most 255 parameters.  A
 *          static method with 256 int parameters is rejected with
 *          ClassFormatError when its class is loaded.
 * @compile ManyArgs.jasm
 * @run main ManyArgsLimitTest
 */

public class ManyArgsLimitTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("ManyArgs");
            throw new AssertionError("too many method parameters not rejected");
        } catch (ClassFormatError e) {
            // expected per JVMS 4.11
        }

        System.out.println("PASS");
    }
}
