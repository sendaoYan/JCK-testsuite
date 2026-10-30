/*
 * @test
 * @summary JLS 5.1.2 (Java SE 8): widening primitive conversions preserve the
 *          exact numeric value when the target type is wide enough.
 * @run main WideningPrimitive
 */

public class WideningPrimitive {
    public static void main(String[] args) {
        byte b = 100;
        short s = b;              // byte -> short
        int i = s;                // short -> int
        long l = i;               // int -> long
        float f = l;              // long -> float
        double d = f;             // float -> double
        if (s != 100 || i != 100 || l != 100L) throw new AssertionError("integral widening");

        char c = 'A';
        int ci = c;               // char -> int is widening
        if (ci != 65) throw new AssertionError("char -> int");

        int big = 123456789;
        double dd = big;          // int -> double is exact below 2^53
        if ((int) dd != big) throw new AssertionError("int -> double exact");

        System.out.println("PASS");
    }
}
