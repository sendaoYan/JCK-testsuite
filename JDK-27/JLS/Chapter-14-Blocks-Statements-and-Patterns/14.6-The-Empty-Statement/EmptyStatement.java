/*
 * @test
 * @summary JLS 14.6: an empty statement does nothing and always completes
 *          normally.
 * @run main EmptyStatement
 */

public class EmptyStatement {
    public static void main(String[] args) {
        ; ; ;               // each empty statement completes normally
        int x = 0;
        ;
        x = 5;              // reachable after the empty statement
        if (x != 5) throw new AssertionError("statement after empty not reached");
        System.out.println("PASS");
    }
}
