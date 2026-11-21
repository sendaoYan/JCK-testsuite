/*
 * @test
 * @summary JVMS 6.5 per-opcode shift boundary matrix: ishl/ishr/iushr mask the
 *          shift count to its low 5 bits and lshl/lshr/lushr to its low 6 bits;
 *          ishr/lshr sign-extend while iushr/lushr zero-extend; negative counts
 *          use those same low bits. Counts are runtime parameters so javac emits
 *          the shift opcodes instead of constant-folding the result.
 * @run main ShiftMaskBoundaryTest
 */

public class ShiftMaskBoundaryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int ishl(int value, int count) { return value << count; }
    static int ishr(int value, int count) { return value >> count; }
    static int iushr(int value, int count) { return value >>> count; }
    static long lshl(long value, int count) { return value << count; }
    static long lshr(long value, int count) { return value >> count; }
    static long lushr(long value, int count) { return value >>> count; }

    public static void main(String[] args) {
        // ishl: count masked to 5 bits
        check(ishl(1, 0) == 1, "ishl 1<<0");
        check(ishl(1, 31) == Integer.MIN_VALUE, "ishl 1<<31 -> MIN");
        check(ishl(1, 32) == 1, "ishl count 32 masked to 0");
        check(ishl(1, 33) == 2, "ishl count 33 masked to 1");
        check(ishl(1, -1) == Integer.MIN_VALUE, "ishl negative count -1 masked to 31");

        // ishr: arithmetic, sign-extending
        check(ishr(-8, 1) == -4, "ishr sign-extends");
        check(ishr(-1, 31) == -1, "ishr -1>>31 stays -1");
        check(ishr(-1, 32) == -1, "ishr count 32 masked to 0");

        // iushr: logical, zero-extending
        check(iushr(-8, 1) == 0x7FFFFFFC, "iushr zero-extends");
        check(iushr(-1, 31) == 1, "iushr -1>>>31 -> 1");
        check(iushr(-1, 32) == -1, "iushr count 32 masked to 0");

        // lshl: count masked to 6 bits
        check(lshl(1L, 0) == 1L, "lshl 1<<0");
        check(lshl(1L, 63) == Long.MIN_VALUE, "lshl 1<<63 -> MIN");
        check(lshl(1L, 64) == 1L, "lshl count 64 masked to 0");
        check(lshl(1L, 65) == 2L, "lshl count 65 masked to 1");
        check(lshl(1L, -1) == Long.MIN_VALUE, "lshl negative count -1 masked to 63");

        // lshr: arithmetic, sign-extending
        check(lshr(-8L, 1) == -4L, "lshr sign-extends");
        check(lshr(-1L, 63) == -1L, "lshr -1>>63 stays -1");
        check(lshr(-1L, 64) == -1L, "lshr count 64 masked to 0");

        // lushr: logical, zero-extending
        check(lushr(-8L, 1) == 0x7FFFFFFFFFFFFFFCL, "lushr zero-extends");
        check(lushr(-1L, 63) == 1L, "lushr -1>>>63 -> 1");
        check(lushr(-1L, 64) == -1L, "lushr count 64 masked to 0");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
