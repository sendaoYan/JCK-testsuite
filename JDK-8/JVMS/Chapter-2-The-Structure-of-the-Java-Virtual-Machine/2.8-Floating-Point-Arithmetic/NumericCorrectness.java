/*
 * @test
 * @summary JVMS 2.11/6.3 (Java SE 8): a hot computation must agree with its exact
 *          closed-form result under both the interpreter (-Xint) and full
 *          ahead-of-time compilation (-Xcomp), so interpreter/JIT divergence is
 *          caught as a wrong result.
 * @run main/othervm -Xint NumericCorrectness
 * @run main/othervm -Xcomp NumericCorrectness
 */

public class NumericCorrectness {
    public static void main(String[] args) {
        // Sum 1..1_000_000 == n*(n+1)/2
        long sum = 0;
        for (int i = 1; i <= 1_000_000; i++) sum += i;
        if (sum != 500000500000L)
            throw new AssertionError("sum 1..1e6: " + sum);

        // Sum of squares 1..1000 == n*(n+1)*(2n+1)/6
        long squares = 0;
        for (int i = 1; i <= 1000; i++) squares += (long) i * i;
        if (squares != 333833500L)
            throw new AssertionError("sum of squares 1..1000: " + squares);

        // Sum of powers of two 2^0..2^52 == 2^53 - 1, exactly representable.
        double powers = 0.0;
        for (int i = 0; i <= 52; i++) powers += (double) (1L << i);
        if (powers != 9007199254740991.0)
            throw new AssertionError("sum 2^0..2^52: " + powers);

        // Repeated doubling: 2^100 is an exact power of two.
        double d = 1.0;
        for (int i = 0; i < 100; i++) d *= 2.0;
        if (d != 0x1.0p100)
            throw new AssertionError("2^100: " + d);

        System.out.println("PASS");
    }
}
