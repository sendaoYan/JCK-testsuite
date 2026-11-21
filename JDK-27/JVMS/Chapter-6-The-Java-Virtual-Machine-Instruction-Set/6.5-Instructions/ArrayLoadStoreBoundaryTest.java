/*
 * @test
 * @summary JVMS 6.5 per-opcode array load/store boundary matrix: baload/saload
 *          sign-extend, caload zero-extends, iastore/lastore/fastore/dastore
 *          round-trip MIN/MAX and special values, and out-of-bounds access
 *          throws ArrayIndexOutOfBoundsException.
 * @run main ArrayLoadStoreBoundaryTest
 */

public class ArrayLoadStoreBoundaryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // baload sign-extends: byte 0xFF loads as int -1
        byte[] b = new byte[] { -1, (byte) 0xFF, 127, -128 };
        check(b[0] == -1, "baload -1");
        check(b[1] == -1, "baload 0xFF sign-extends to -1");
        check(b[2] == 127, "baload 127");
        check(b[3] == -128, "baload -128");

        // caload zero-extends: char 0xFFFF loads as int 65535
        char[] c = new char[] { (char) 0xFFFF, '\u0000', 'A' };
        check(c[0] == 65535, "caload 0xFFFF zero-extends to 65535");
        check(c[1] == 0, "caload 0");
        check(c[2] == 65, "caload 'A'");

        // saload sign-extends: short 0x8000 loads as int -32768
        short[] s = new short[] { -1, (short) 0x8000, 32767 };
        check(s[0] == -1, "saload -1");
        check(s[1] == -32768, "saload 0x8000 sign-extends to -32768");
        check(s[2] == 32767, "saload 32767");

        // iastore/lastore round-trip MIN/MAX
        int[] ia = new int[2];
        ia[0] = Integer.MAX_VALUE;
        ia[1] = Integer.MIN_VALUE;
        check(ia[0] == Integer.MAX_VALUE, "iastore MAX");
        check(ia[1] == Integer.MIN_VALUE, "iastore MIN");

        long[] la = new long[2];
        la[0] = Long.MAX_VALUE;
        la[1] = Long.MIN_VALUE;
        check(la[0] == Long.MAX_VALUE, "lastore MAX");
        check(la[1] == Long.MIN_VALUE, "lastore MIN");

        // fastore/dastore round-trip special values
        float[] fa = new float[] { Float.MAX_VALUE, Float.NaN, -0.0f };
        check(fa[0] == Float.MAX_VALUE, "fastore MAX");
        check(Float.isNaN(fa[1]), "fastore NaN");
        check(Float.floatToRawIntBits(fa[2]) == Float.floatToRawIntBits(-0.0f),
              "fastore -0.0 preserves sign bit");

        double[] da = new double[] { Double.MAX_VALUE, Double.NaN, -0.0d };
        check(da[0] == Double.MAX_VALUE, "dastore MAX");
        check(Double.isNaN(da[1]), "dastore NaN");
        check(Double.doubleToRawLongBits(da[2]) == Double.doubleToRawLongBits(-0.0d),
              "dastore -0.0 preserves sign bit");

        // out-of-bounds load/store throws ArrayIndexOutOfBoundsException
        try {
            int x = b[4];
            throw new AssertionError("baload OOB did not throw");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(true, "baload OOB");
        }
        try {
            int x = c[-1];
            throw new AssertionError("caload OOB did not throw");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(true, "caload OOB");
        }
        try {
            ia[2] = 1;
            throw new AssertionError("iastore OOB did not throw");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(true, "iastore OOB");
        }
        try {
            la[-1] = 1L;
            throw new AssertionError("lastore OOB did not throw");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(true, "lastore OOB");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
