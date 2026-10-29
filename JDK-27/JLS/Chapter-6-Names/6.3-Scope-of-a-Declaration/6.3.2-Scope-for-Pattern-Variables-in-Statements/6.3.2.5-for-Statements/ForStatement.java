/*
 * @test
 * @summary JLS 6.3.2.5: in a basic for statement, a pattern variable introduced
 *          by the condition when true is matched in the increment part and the
 *          contained statement.
 * @run main ForStatement
 */

public class ForStatement {
    public static void main(String[] args) {
        Object[] objs = { "a", "bb", 9 };
        int total = 0;
        for (int i = 0; i < objs.length && objs[i] instanceof String s; i++) {
            total += s.length();
        }
        if (total != 3) throw new AssertionError("total " + total);

        System.out.println("PASS");
    }
}
