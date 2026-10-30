/*
 * @test
 * @summary JLS 5.1.7/5.1.8 (Java SE 8): boxing wraps a primitive in its wrapper;
 *          unboxing unwraps it; boxing small int values (-128..127) is cached.
 * @run main BoxingUnboxing
 */

public class BoxingUnboxing {
    public static void main(String[] args) {
        Integer i = 5;            // boxing
        int j = i;                // unboxing
        if (j != 5) throw new AssertionError("unboxing");

        Boolean b = true;
        if (!b) throw new AssertionError("boolean unboxing");

        Integer a = 127, b2 = 127;
        if (a != b2) throw new AssertionError("Integer -128..127 cached per 5.1.7");

        Integer n = 100, m = 100;
        if (n != m) throw new AssertionError("Integer cache");

        System.out.println("PASS");
    }
}
