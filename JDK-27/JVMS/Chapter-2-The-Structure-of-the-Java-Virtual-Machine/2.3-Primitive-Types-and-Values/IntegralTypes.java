/*
 * @test
 * @summary JVMS 2.3.1: the integral types byte, short, int, long, and char use
 *          two's-complement (except char) with the specified value ranges.
 * @run main IntegralTypes
 */

public class IntegralTypes {
    public static void main(String[] args) {
        byte bMin = -128, bMax = 127;
        short sMin = -32768, sMax = 32767;
        int iMin = Integer.MIN_VALUE, iMax = Integer.MAX_VALUE;
        long lMin = Long.MIN_VALUE, lMax = Long.MAX_VALUE;
        char cMin = 0, cMax = 65535;

        if (bMin != -128 || bMax != 127) throw new AssertionError("byte range");
        if (sMin != -32768 || sMax != 32767) throw new AssertionError("short range");
        if (iMin != -2147483648 || iMax != 2147483647) throw new AssertionError("int range");
        if (lMin != -9223372036854775808L || lMax != 9223372036854775807L)
            throw new AssertionError("long range");
        if (cMin != 0 || cMax != 65535) throw new AssertionError("char range");

        // two's-complement: -1 is the all-ones bit pattern
        if (~0 != -1) throw new AssertionError("two's complement");

        System.out.println("PASS");
    }
}
