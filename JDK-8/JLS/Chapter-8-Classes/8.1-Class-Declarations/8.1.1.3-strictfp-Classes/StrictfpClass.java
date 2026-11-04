/*
 * @test
 * @summary JLS 8.1.1.3 (Java SE 8): a class may be declared strictfp; a
 *          strictfp class declares that its floating-point expressions are FP-
 *          strict, and the class compiles and runs normally.
 * @run main StrictfpClass
 */
public strictfp class StrictfpClass {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static strictfp double half(double x) { return x / 2.0; }
    public static void main(String[] args) {
        check(half(10.0) == 5.0, "strictfp class method computes");
        check(0.1 + 0.2 > 0.3, "floating point expression evaluated");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
