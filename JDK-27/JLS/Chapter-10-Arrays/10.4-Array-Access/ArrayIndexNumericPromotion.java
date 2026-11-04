/*
 * @test
 * @summary JLS 10.4: arrays must be indexed by int values; short, byte, and char
 *          index values are allowed because they undergo unary numeric promotion
 *          to int. A long index is a compile-time error (see ArrayLongIndex).
 * @run main ArrayIndexNumericPromotion
 */

public class ArrayIndexNumericPromotion {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int[] a = {10, 20, 30, 40, 50};

        byte b = 2;
        short s = 3;
        char c = 4;

        check(a[b] == 30, "byte index promoted to int");
        check(a[s] == 40, "short index promoted to int");
        check(a[c] == 50, "char index promoted to int");

        a[b] = 300;
        a[s] = 400;
        a[c] = 500;
        check(a[2] == 300 && a[3] == 400 && a[4] == 500, "store via promoted index");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
