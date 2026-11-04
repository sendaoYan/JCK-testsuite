/*
 * @test
 * @summary JLS 10.4 (Java SE 8): arrays are indexed by int values; byte, short and
 *          char indices are promoted to int; a long index is a compile-time error.
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
        check(a[2] == 300, "store via promoted index");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
