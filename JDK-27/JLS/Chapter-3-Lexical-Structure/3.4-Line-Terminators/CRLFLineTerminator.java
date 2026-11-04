/*
 * @test
 * @summary JLS 3.4: the two-character sequence CRLF is a single line
 *          terminator, and it terminates an end-of-line (//) comment.
 * @run main CRLFLineTerminator
 */

public class CRLFLineTerminator {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        int x = 1; // this comment ends at the CRLF line terminator
        int y = 2;
        check(x == 1, "x");
        check(y == 2, "y");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
