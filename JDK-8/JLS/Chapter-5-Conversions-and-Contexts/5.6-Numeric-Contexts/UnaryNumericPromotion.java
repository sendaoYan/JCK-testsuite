/*
 * @test
 * @summary JLS 5.6.1 (Java SE 8): unary numeric promotion promotes byte/short/
 *          char to int (via widening), and unboxes Byte/Short/Character/Integer
 *          then widens to int; Long/Float/Double are unboxed and left as is.
 *          It applies to array dimensions/indexes, unary +/-, ~, and shift
 *          operands (a long shift distance does not promote the shifted value).
 * @run main UnaryNumericPromotion
 */

public class UnaryNumericPromotion {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        byte b = -1;                       // 0xff
        check(~b == 0, "~byte promotes to int then complements");

        char c = '\u0001';
        check(-c == -1, "unary minus promotes char to int");

        // Array dimension/index promotion.
        byte dim = 3;
        int[] a = new int[dim];
        check(a.length == 3, "dimension expression promotion");
        a[1] = 9;
        char idx = 1;
        check(a[idx] == 9, "index expression promotion");

        // Shift: left operand promoted, long distance does not promote left.
        byte shifted = (byte) 0x80;        // -128
        check(shifted << 4L == -2048, "byte << 4L promotes left operand to int only");

        // Wrapper unary promotion: Integer unboxed to int, then identity.
        Integer boxed = -5;
        check(-boxed == 5, "unary minus on Integer unboxes to int");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
