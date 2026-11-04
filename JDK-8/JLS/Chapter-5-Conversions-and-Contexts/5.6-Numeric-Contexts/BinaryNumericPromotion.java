/*
 * @test
 * @summary JLS 5.6.2 (Java SE 8): binary numeric promotion first unboxes
 *          reference operands, then promotes: if either is double the other is
 *          converted to double; else float; else long; else both to int.
 * @run main BinaryNumericPromotion
 */

public class BinaryNumericPromotion {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // double dominates.
        int i = 1;
        double d = 2.5;
        check(i + d == 3.5, "int + double promotes int to double");

        // float dominates over long.
        long l = 2L;
        float f = 1.5f;
        check(l + f == 3.5f, "long + float promotes long to float");

        // long dominates over int.
        int j = 3;
        long m = 4L;
        check(j + m == 7L, "int + long promotes int to long");

        // Otherwise both become int: byte + short -> int + int.
        byte b = 1;
        short s = 2;
        check(b + s == 3, "byte + short promotes both to int");

        // Unboxing happens first: Integer + Double -> int + double -> double.
        Integer bi = 1;
        Double bd = 2.5;
        check(bi + bd == 3.5, "Integer + Double unboxes then promotes to double");

        // char and byte both promote to int.
        char c = 'G';                       // 71
        byte mask = 0x1f;
        check((c & mask) == 7, "char & byte promotes both to int");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
