/*
 * @test
 * @summary JLS 14.22: for conditional compilation, the body of if (false) is not
 *          treated as unreachable (unlike while (false)).
 * @run main IfFalseConditionalCompilation
 */

public class IfFalseConditionalCompilation {
    static final boolean DEBUG = false;

    public static void main(String[] args) {
        if (DEBUG) { int x = 3; }   // legal: not unreachable
        if (false) { int x = 3; }   // legal: not unreachable
        System.out.println("PASS");
    }
}
