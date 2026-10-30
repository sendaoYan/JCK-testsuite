/*
 * @test
 * @summary JLS 14.9 / 14.9.1 / 14.9.2: the if statement requires a boolean or
 *          Boolean expression and unboxes a Boolean result before choosing a
 *          branch.
 * @run main IfStatement
 */

public class IfStatement {
    public static void main(String[] args) {
        int x = 0;

        // if-then with a false condition completes normally without executing.
        if (false) x = 1;
        if (x != 0) throw new AssertionError("if-then false ran then-branch");

        // A Boolean expression is unboxed.
        Boolean b = Boolean.TRUE;
        if (b) x = 5;
        if (x != 5) throw new AssertionError("Boolean unboxing: x == " + x);

        // if-then-else executes exactly one branch.
        int y = 0;
        if (true) y = 1; else y = 2;
        if (y != 1) throw new AssertionError("if-then-else then: y == " + y);
        if (false) y = 3; else y = 4;
        if (y != 4) throw new AssertionError("if-then-else else: y == " + y);

        System.out.println("PASS");
    }
}
