/*
 * @test
 * @summary JLS 5.1.3 (Java SE 8): narrowing primitive conversions require a cast
 *          and truncate toward zero or to the low-order bits of the value.
 * @run main NarrowingPrimitive
 */

public class NarrowingPrimitive {
    public static void main(String[] args) {
        if ((int) 3.99 != 3) throw new AssertionError("double -> int truncates toward zero");
        if ((int) -3.99 != -3) throw new AssertionError("negative truncation toward zero");
        if ((long) 1.5 != 1L) throw new AssertionError("double -> long");

        if ((byte) 300 != 44) throw new AssertionError("int -> byte takes low 8 bits (300 & 0xFF = 44)");
        if ((short) 65536 != 0) throw new AssertionError("int -> short wraps");
        if ((char) 66 != 'B') throw new AssertionError("int -> char");

        System.out.println("PASS");
    }
}
