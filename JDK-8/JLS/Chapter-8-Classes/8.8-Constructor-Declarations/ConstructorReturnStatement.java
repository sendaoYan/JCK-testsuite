/*
 * @test
 * @summary JLS 8.8.7 (Java SE 8): a return statement with no expression is permitted
 *          in a constructor body and terminates the constructor normally.
 * @run main ConstructorReturnStatement
 */

public class ConstructorReturnStatement {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    int x;
    ConstructorReturnStatement() {
        return;   // legal: return with no expression
    }

    public static void main(String[] args) {
        ConstructorReturnStatement o = new ConstructorReturnStatement();
        check(o.x == 0, "constructor completed normally via return");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
