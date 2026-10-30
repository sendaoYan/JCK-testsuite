/*
 * @test
 * @summary JLS 16.2.15: a variable assigned in a try block or in the finally block
 *          is definitely assigned after the try statement.
 * @run main TryFinallyDA
 */

public class TryFinallyDA {
    public static void main(String[] args) {
        int x;
        try { x = 1; } finally { }
        if (x != 1) throw new AssertionError("try x == " + x);

        int y;
        try { } finally { y = 7; }   // assigned in finally
        if (y != 7) throw new AssertionError("finally y == " + y);

        System.out.println("PASS");
    }
}
