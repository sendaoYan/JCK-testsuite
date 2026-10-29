/*
 * @test
 * @summary JLS 6.3.1.1: for a && b, a pattern variable introduced by a when true
 *          is definitely matched at b, so it is in scope in b.
 * @run main ConditionalAnd
 */

public class ConditionalAnd {
    public static void main(String[] args) {
        Object o = "hello";
        if (o instanceof String s && s.length() == 5) {
            if (!s.equals("hello")) throw new AssertionError("s in scope");
        } else {
            throw new AssertionError("should be a 5-char String");
        }

        System.out.println("PASS");
    }
}
