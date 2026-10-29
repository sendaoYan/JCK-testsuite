/*
 * @test
 * @summary JLS 5.6: numeric promotion. In an arithmetic/array context the
 *          promoted type is int if no double/float/long is present; a byte,
 *          short, or char operand is widened to int. A double or float or long
 *          operand promotes the others accordingly. Unboxing is applied first.
 * @run main NumericContexts
 */

public class NumericContexts {
    public static void main(String[] args) {
        // Unary numeric promotion: ~byte promotes to int.
        byte b = -1;                     // 0xff
        int complement = ~b;             // promotes byte to int
        if (complement != 0) throw new AssertionError("~byte == 0");

        // Shift promotion: left operand promoted, shift distance not.
        byte shift = (byte) 0x80;       // -128
        int shifted = shift << 4;        // promotes to int; -128 << 4 == -2048
        if (shifted != -2048) throw new AssertionError("byte << 4 promoted to int");

        // Array dimension/index expression promotion.
        byte dim = 2;
        int[] a = new int[dim];          // byte promoted to int for dimension
        if (a.length != 2) throw new AssertionError("dimension promotion");

        // Binary numeric promotion: char & byte promotes to int & int.
        byte mask = 0x1f;
        char c = 'G';                    // 71
        int control = c & mask;          // 71 & 31 == 7
        if (control != 7) throw new AssertionError("char & byte -> int, got " + control);

        // int * float promotes to float * float.
        int i = 2;
        float f = 1.5f;
        float r = i * f;
        if (r != 3.0f) throw new AssertionError("int * float -> float");

        // Unboxing is applied before promotion.
        Integer boxed = 5;
        long l = boxed + 1L;             // unbox Integer -> int, then promote to long
        if (l != 6L) throw new AssertionError("unbox + promote to long");

        System.out.println("PASS");
    }
}
