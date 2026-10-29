/*
 * @test
 * @summary JLS 5.3: strict invocation contexts allow identity, widening
 *          primitive, and widening reference conversions; loose contexts also
 *          allow boxing and unboxing conversions. Unlike assignment contexts,
 *          invocation contexts do NOT include implicit narrowing of integer
 *          constant expressions (Example 5.3).
 * @run main InvocationContexts
 */

public class InvocationContexts {
    static int wide(long x) { return (int) x; }
    static int boxed(Integer x) { return x; }

    public static void main(String[] args) {
        // Widening primitive conversion in a strict invocation context.
        if (wide(5) != 5) throw new AssertionError("int -> long");

        // Boxing conversion in a loose invocation context (Integer parameter).
        if (boxed(7) != 7) throw new AssertionError("int -> Integer (boxing)");

        System.out.println("PASS");
    }
}
