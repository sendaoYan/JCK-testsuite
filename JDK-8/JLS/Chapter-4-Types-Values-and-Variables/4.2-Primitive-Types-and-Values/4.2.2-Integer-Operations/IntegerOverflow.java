/*
 * @test
 * @summary JLS 4.2.2 (Java SE 8): the integer operators do not indicate
 *          overflow or underflow in any way; results wrap around within the
 *          target type. With a long operand the operation is carried out in
 *          64-bit precision and yields long; otherwise in 32-bit precision and
 *          yields int (after widening via numeric promotion).
 * @run main IntegerOverflow
 */

public class IntegerOverflow {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // 32-bit wrap-around (int addition overflows silently).
        check(Integer.MAX_VALUE + 1 == Integer.MIN_VALUE, "int overflow wraps to MIN_VALUE");
        check(Integer.MIN_VALUE - 1 == Integer.MAX_VALUE, "int underflow wraps to MAX_VALUE");

        // 64-bit wrap-around.
        check(Long.MAX_VALUE + 1L == Long.MIN_VALUE, "long overflow wraps to MIN_VALUE");
        check(Long.MIN_VALUE - 1L == Long.MAX_VALUE, "long underflow wraps to MAX_VALUE");

        // Multiplication wraps to the low 32 bits.
        int big = 1000000;
        check(big * big == -727379968, "int multiplication keeps only low 32 bits");

        // A long operand forces 64-bit precision and a long result.
        long l = big;
        check(l * l == 1000000000000L, "long multiplication is 64-bit exact here");

        // Unary minus on MIN_VALUE stays MIN_VALUE (wraps).
        check(-Integer.MIN_VALUE == Integer.MIN_VALUE, "unary minus of MIN_VALUE wraps");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
