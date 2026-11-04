/*
 * @test
 * @summary JLS 5.2 (Java SE 8): in addition to the ordinary conversions, a
 *          constant expression of type byte/short/char/int may be narrowed to
 *          byte/short/char (or narrowed then boxed to Byte/Short/Character)
 *          if the constant value is representable in the target type. The
 *          boundaries are inclusive (e.g. 127 and -128 fit in byte).
 * @run main ConstantNarrowingBoundary
 */

public class ConstantNarrowingBoundary {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // byte boundary: -128 .. 127 inclusive.
        byte minByte = -128;
        byte maxByte = 127;
        check(minByte == -128 && maxByte == 127, "byte constant narrowing at both ends");

        // short boundary: -32768 .. 32767 inclusive.
        short minShort = -32768;
        short maxShort = 32767;
        check(minShort == -32768 && maxShort == 32767, "short constant narrowing at both ends");

        // char boundary: 0 .. 65535 inclusive.
        char minChar = 0;
        char maxChar = 65535;
        check(minChar == 0 && maxChar == 65535, "char constant narrowing at both ends");

        // Constant expression (not just literal) narrowing.
        final int seven = 7;
        byte fromConst = seven + 1;     // constant expression of type int -> byte
        check(fromConst == 8, "constant expression narrowing to byte");

        // Narrowing then boxing to Byte/Short/Character.
        Byte boxedByte = 127;
        Short boxedShort = 32000;
        Character boxedChar = 65;
        check(boxedByte == 127 && boxedShort == 32000 && boxedChar == 'A',
              "constant narrowing followed by boxing");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
