/*
 * @test
 * @summary JLS 18.2 (Java SE 8): a declared type-parameter bound (e.g. T extends
 *          Number) constrains the inferred type argument.
 * @run main BoundTypeParameterInference
 */

public class BoundTypeParameterInference {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static <T extends Number> T first(T a, T b) { return a; }

    public static void main(String[] args) {
        Integer i = first(1, 2);
        check(i == 1, "T bounded by Number inferred as Integer");

        Double d = first(1.5, 2.5);
        check(d == 1.5, "T bounded by Number inferred as Double");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
