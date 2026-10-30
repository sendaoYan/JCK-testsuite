/*
 * @test
 * @summary JLS 14.20.2 (Java SE 8): a return in a finally block overrides a value
 *          being returned from the try block, but a plain assignment in finally
 *          does not change a value already selected for return.
 * @run main FinallyReturnTest
 */

public class FinallyReturnTest {
    static int f() {
        try {
            return 1;
        } finally {
            return 2;   // overrides the try's return
        }
    }

    static int g() {
        int r;
        try {
            r = 10;
            throw new RuntimeException();
        } catch (RuntimeException e) {
            r = 20;
            return r;
        } finally {
            r = 30;      // does not change the value already selected for return
        }
    }

    public static void main(String[] args) {
        if (f() != 2) throw new AssertionError("finally return overrides");
        if (g() != 20) throw new AssertionError("finally assignment does not override returned value");
        System.out.println("PASS");
    }
}
