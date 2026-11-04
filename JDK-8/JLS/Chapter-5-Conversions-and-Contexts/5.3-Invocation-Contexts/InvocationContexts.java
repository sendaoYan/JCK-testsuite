/*
 * @test
 * @summary JLS 5.3 (Java SE 8): strict invocation contexts allow identity,
 *          widening primitive, and widening reference conversions; loose
 *          contexts also allow boxing and unboxing conversions (each optionally
 *          followed by a widening conversion). null is assignable to any
 *          reference parameter.
 * @run main InvocationContexts
 */

public class InvocationContexts {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int wide(long x) { return (int) x; }
    static int widenRef(Object o) { return o == null ? -1 : 1; }
    static int boxed(Integer x) { return x; }
    static long unboxWiden(Integer x) { return x + 1L; }   // unbox then widen to long

    public static void main(String[] args) {
        check(wide(5) == 5, "int -> long widening primitive in strict context");
        check(widenRef("s") == 1, "String -> Object widening reference in strict context");
        check(boxed(7) == 7, "int -> Integer boxing in loose context");
        check(unboxWiden(Integer.valueOf(2)) == 3L, "Integer -> int -> long unbox+widening");
        check(widenRef(null) == -1, "null assignable to reference parameter");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
