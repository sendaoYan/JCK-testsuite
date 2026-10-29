/*
 * @test
 * @summary JLS 6.3.2.2: a pattern variable introduced by if (e) S when true is
 *          definitely matched at S, and by if (e) S else T is matched in the
 *          corresponding branch.
 * @run main IfStatement
 */

public class IfStatement {
    static int firstLen(Object o) {
        if (o instanceof String s) {
            return s.length();
        }
        return -1;
    }
    static int secondLen(Object o) {
        if (!(o instanceof String s)) {
            return -1;
        } else {
            return s.length();
        }
    }
    public static void main(String[] args) {
        if (firstLen("abc") != 3) throw new AssertionError("then branch");
        if (firstLen(1) != -1) throw new AssertionError("no else branch");
        if (secondLen("xy") != 2) throw new AssertionError("else branch");
        if (secondLen(1) != -1) throw new AssertionError("negated then branch");

        System.out.println("PASS");
    }
}
