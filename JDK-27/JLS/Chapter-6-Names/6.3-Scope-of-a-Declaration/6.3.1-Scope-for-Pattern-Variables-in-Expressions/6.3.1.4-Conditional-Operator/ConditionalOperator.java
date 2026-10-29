/*
 * @test
 * @summary JLS 6.3.1.4: in a ? b : c, a pattern variable introduced by a when
 *          true is definitely matched at b, and when false at c.
 * @run main ConditionalOperator
 */

public class ConditionalOperator {
    public static void main(String[] args) {
        Object o = "hi";
        int n = (o instanceof String s) ? s.length() : 0;
        if (n != 2) throw new AssertionError("s in scope in true branch");

        System.out.println("PASS");
    }
}
