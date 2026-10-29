/*
 * @test
 * @summary JLS 6.3.3: a pattern variable declared by a case pattern is in scope
 *          in the associated guard (when clause), and a variable introduced by
 *          the guard when true is in scope in the rule body.
 * @run main CaseLabelGuard
 */

public class CaseLabelGuard {
    static String classify(Object o) {
        return switch (o) {
            case String s when s.length() > 2 -> "long";
            case String s -> "short";
            case null -> "null";
            default -> "other";
        };
    }
    public static void main(String[] args) {
        if (!classify("abc").equals("long")) throw new AssertionError("long");
        if (!classify("ab").equals("short")) throw new AssertionError("short");
        if (!classify(null).equals("null")) throw new AssertionError("null");
        if (!classify(42).equals("other")) throw new AssertionError("other");

        System.out.println("PASS");
    }
}
