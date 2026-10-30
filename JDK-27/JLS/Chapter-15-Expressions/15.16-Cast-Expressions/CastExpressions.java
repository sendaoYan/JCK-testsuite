/*
 * @test
 * @summary JLS 15.16: a cast converts a value; primitive narrowing truncates
 *          toward zero, and a reference cast performs a run-time check.
 * @run main CastExpressions
 */

public class CastExpressions {
    public static void main(String[] args) {
        int i = (int) 3.99;            // narrowing truncates toward zero
        if (i != 3) throw new AssertionError("double -> int: " + i);

        long l = (long) 42;            // widening
        if (l != 42L) throw new AssertionError("int -> long");

        Object o = "str";
        String s = (String) o;         // reference downcast
        if (!"str".equals(s)) throw new AssertionError("downcast");

        Object u = (Object) s;         // upcast is a no-op
        if (u != s) throw new AssertionError("upcast");

        System.out.println("PASS");
    }
}
