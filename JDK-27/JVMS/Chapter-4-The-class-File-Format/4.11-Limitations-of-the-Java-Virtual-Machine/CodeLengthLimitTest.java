/*
 * @test
 * @summary JVMS 4.11: the length of a method's code array must not exceed
 *          65535 bytes.  A method whose code is longer (65536 nop instructions
 *          plus a return) is rejected with ClassFormatError when loaded.
 * @compile BigCode.jasm
 * @run main CodeLengthLimitTest
 */

public class CodeLengthLimitTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("BigCode");
            throw new AssertionError("oversized code array not rejected");
        } catch (ClassFormatError e) {
            // expected per JVMS 4.11
        }

        System.out.println("PASS");
    }
}
