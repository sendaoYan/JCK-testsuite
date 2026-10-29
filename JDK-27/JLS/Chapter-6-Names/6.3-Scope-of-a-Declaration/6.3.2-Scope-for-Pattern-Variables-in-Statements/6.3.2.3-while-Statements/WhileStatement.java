/*
 * @test
 * @summary JLS 6.3.2.3: a pattern variable introduced by while (e) S when true
 *          is definitely matched at S.
 * @run main WhileStatement
 */

public class WhileStatement {
    public static void main(String[] args) {
        Object[] objs = { "x", "yy", 5 };
        int i = 0;
        int total = 0;
        while (objs[i] instanceof String s) {
            total += s.length();
            i++;
        }
        if (total != 3) throw new AssertionError("total " + total);
        if (i != 2) throw new AssertionError("i " + i);

        System.out.println("PASS");
    }
}
