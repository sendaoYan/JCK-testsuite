/*
 * @test
 * @summary JLS 6.3.2.4: do S while (e) introduces a pattern variable iff it is
 *          introduced by e when false and S has no reachable break for the do
 *          statement; the variable is then in scope after the do statement.
 * @run main DoStatement
 */

public class DoStatement {
    static int len(Object o) {
        do {
            // s is not in scope here: the condition is evaluated after the body.
        } while (!(o instanceof String s));
        // After the loop, s is in scope (the do statement introduced it).
        return s.length();
    }
    public static void main(String[] args) {
        if (len("do") != 2) throw new AssertionError("len");
        System.out.println("PASS");
    }
}
