/*
 * @test
 * @summary JLS 5.1.2 (Java SE 8): widening primitive conversions lose precision
 *          beyond the target's significand.  int->float is exact only up to 2^24,
 *          long->float up to 2^24 and long->double up to 2^53; beyond that the
 *          value rounds to the nearest representable float/double, ties to even.
 * @run main WideningPrecisionLossTest
 */

public class WideningPrecisionLossTest {
    static float  intToFloat(int x)    { return (float) x; }
    static float  longToFloat(long x)  { return (float) x; }
    static double longToDouble(long x) { return (double) x; }
    static double intToDouble(int x)   { return (double) x; }

    public static void main(String[] args) {
        // int -> float: exact up to 2^24; 2^24+1 ties-to-even down to 2^24,
        // 2^24+3 ties-to-even up to 2^24+4.
        if (intToFloat(16777216) != 0x1.0p24f) throw new AssertionError("2^24 exact");
        if (intToFloat(16777217) != 0x1.0p24f) throw new AssertionError("2^24+1 rounds down");
        if (intToFloat(16777219) != 16777220.0f) throw new AssertionError("2^24+3 rounds up");

        // int -> float: Integer.MAX_VALUE (2^31-1) rounds up to 2^31.
        if (intToFloat(Integer.MAX_VALUE) != 0x1.0p31f) throw new AssertionError("int MAX -> 2^31");

        // long -> float: Long.MAX_VALUE (2^63-1) rounds up to 2^63.
        if (longToFloat(Long.MAX_VALUE) != 0x1.0p63f) throw new AssertionError("long MAX -> 2^63");

        // long -> double: exact up to 2^53; 2^53+1 rounds down to 2^53,
        // Long.MAX_VALUE rounds up to 2^63.
        if (longToDouble(9007199254740992L) != 0x1.0p53) throw new AssertionError("2^53 exact");
        if (longToDouble(9007199254740993L) != 0x1.0p53) throw new AssertionError("2^53+1 rounds down");
        if (longToDouble(Long.MAX_VALUE) != 0x1.0p63) throw new AssertionError("long MAX -> 2^63");

        // int -> double is always exact.
        if (intToDouble(Integer.MAX_VALUE) != 2147483647.0) throw new AssertionError("int MAX -> double exact");
        if (intToDouble(Integer.MIN_VALUE) != -2147483648.0) throw new AssertionError("int MIN -> double exact");

        System.out.println("PASS");
    }
}
