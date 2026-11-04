/*
 * @test
 * @summary JLS 15.26 (with 5.2) (Java SE 8): assignment conversion narrows a
 *          compile-time constant expression to byte/short/char when the value is
 *          in range, widens primitive values, and boxes primitives.
 * @run main AssignmentConversion
 */

public class AssignmentConversion {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        byte b = 100;
        check(b == 100, "int constant -> byte");
        byte b2 = -128;
        check(b2 == -128, "byte MIN constant");
        byte b3 = 127;
        check(b3 == 127, "byte MAX constant");
        short s = 32000;
        check(s == 32000, "int constant -> short");
        char c = 65;
        check(c == 'A', "int constant -> char");

        final int fx = 10;
        byte bf = fx;
        check(bf == 10, "constant variable -> byte");

        long l = 100;
        check(l == 100L, "int -> long widening");
        float f = 100;
        check(f == 100.0f, "int -> float widening");
        double d = 100;
        check(d == 100.0, "int -> double widening");

        Integer bi = 100;
        check(bi == 100, "int -> Integer boxing");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
