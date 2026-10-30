/*
 * @test
 * @summary JLS 12.2.2: given the same name, a class loader returns the same Class
 *          object.
 * @run main SameLoaderSameClass
 */

public class SameLoaderSameClass {
    public static void main(String[] args) throws Exception {
        Class<?> c1 = Class.forName("java.lang.String");
        Class<?> c2 = String.class;
        if (c1 != c2) throw new AssertionError("same loader should return same Class");
        System.out.println("PASS");
    }
}
