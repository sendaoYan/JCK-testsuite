/*
 * @test
 * @summary JLS 8.4.4: a method is generic if it declares one or more type
 *          variables; the type arguments are inferred at each invocation.
 * @run main GenericMethod
 */

public class GenericMethod {
    static <T> T pick(T a, T b, boolean first) { return first ? a : b; }

    public static void main(String[] args) {
        String s = pick("a", "b", true);
        Integer i = pick(1, 2, false);
        if (!"a".equals(s)) throw new AssertionError("string pick");
        if (i != 2) throw new AssertionError("integer pick");
        System.out.println("PASS");
    }
}
