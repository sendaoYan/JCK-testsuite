/*
 * @test
 * @summary JVMS 3.3: arithmetic on int, long, float, and double compiles to the
 *          type-specialized iadd/ladd/fadd/dadd family; int overflow wraps.
 * @run main CompilingArithmetic
 */

public class CompilingArithmetic {
    public static void main(String[] args) {
        int i = 7;
        i += 5;
        if (i != 12) throw new AssertionError("int add");

        long l = 1L << 40;
        if (l != 1099511627776L) throw new AssertionError("long shift/add");

        float f = 1.5f * 2.0f;
        if (f != 3.0f) throw new AssertionError("float mul");

        double d = 10.0 / 4.0;
        if (d != 2.5) throw new AssertionError("double div");

        // int arithmetic wraps modulo 2^32 (iadd semantics)
        int max = Integer.MAX_VALUE;
        if (max + 1 != Integer.MIN_VALUE) throw new AssertionError("int overflow wrap");

        System.out.println("PASS");
    }
}
