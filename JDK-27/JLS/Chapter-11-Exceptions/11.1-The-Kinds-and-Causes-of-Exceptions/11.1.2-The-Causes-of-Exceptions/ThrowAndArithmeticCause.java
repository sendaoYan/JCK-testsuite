/*
 * @test
 * @summary JLS 11.1.2: an exception is thrown by a throw statement or by an
 *          abnormal condition synchronously detected by the JVM (here, integer
 *          division by zero).
 * @run main ThrowAndArithmeticCause
 */

public class ThrowAndArithmeticCause {
    public static void main(String[] args) {
        try {
            throw new IllegalArgumentException("explicit");
        } catch (IllegalArgumentException e) {
            if (!"explicit".equals(e.getMessage())) throw new AssertionError("throw cause");
        }

        try {
            int x = 1 / 0;
            throw new AssertionError("no division by zero exception");
        } catch (ArithmeticException e) {
            // expected: JVM-detected abnormal condition
        }
        System.out.println("PASS");
    }
}
