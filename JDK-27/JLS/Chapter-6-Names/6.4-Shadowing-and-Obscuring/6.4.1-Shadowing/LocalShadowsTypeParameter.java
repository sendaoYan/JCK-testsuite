/*
 * @test
 * @summary JLS 6.4.1 (Java SE 27): a parameter or local variable may shadow a
 *          type parameter; a type parameter is not protected from shadowing the
 *          way a formal parameter or local variable is.
 * @run main LocalShadowsTypeParameter
 */

public class LocalShadowsTypeParameter {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static class C<T> {
        int m(int T) {     // parameter named T shadows the class type parameter
            int T2 = T;    // T here is the int parameter, not the type variable
            return T2;
        }
    }

    public static void main(String[] args) {
        C<String> c = new C<String>();
        check(c.m(9) == 9, "parameter shadows type parameter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
