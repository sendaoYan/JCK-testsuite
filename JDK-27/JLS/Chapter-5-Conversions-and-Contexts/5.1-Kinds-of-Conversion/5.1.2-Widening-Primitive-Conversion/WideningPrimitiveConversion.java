/*
 * @test
 * @summary JLS 5.1.2: the 19 widening primitive conversions (byte->short/int/
 *          long/float/double; short->int/long/float/double; char->int/long/
 *          float/double; int->long/float/double; long->float/double;
 *          float->double). Some preserve value exactly; int/float, long/float,
 *          long/double may lose precision (round to nearest).
 * @run main WideningPrimitiveConversion
 */

public class WideningPrimitiveConversion {
    public static void main(String[] args) {
        byte b = 100;
        short s = b;         // byte -> short
        int i = s;           // short -> int
        long l = i;          // int -> long
        float f = l;         // long -> float
        double d = f;        // float -> double
        if (d != 100.0) throw new AssertionError("byte widened to double");

        char c = 'A';
        int ci = c;          // char -> int
        long cl = c;         // char -> long
        double cd = c;       // char -> double
        if (ci != 65 || cl != 65L || cd != 65.0) throw new AssertionError("char widening");

        // int -> double is exact; int -> float / long -> float / long -> double
        // may lose precision (round to nearest).
        int big = 1234567890;
        float approx = big;
        // Example 5.1.2-1: the difference big - (int)approx is -46.
        if (big - (int) approx != -46) {
            throw new AssertionError("int->float precision loss, got " + (big - (int) approx));
        }

        // float -> double is exact for these values.
        float fv = 1.5f;
        double dv = fv;
        if (dv != 1.5) throw new AssertionError("float -> double exact");

        System.out.println("PASS");
    }
}
