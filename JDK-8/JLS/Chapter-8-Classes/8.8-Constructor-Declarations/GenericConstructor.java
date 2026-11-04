/*
 * @test
 * @summary JLS 8.8.4 (Java SE 8): a constructor is generic if it declares type
 *          variables, and the type arguments are inferred at each invocation.
 * @run main GenericConstructor
 */

public class GenericConstructor {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    Object val;
    <T> GenericConstructor(T t) { val = t; }

    public static void main(String[] args) {
        GenericConstructor g = new GenericConstructor("hello");
        check("hello".equals(g.val), "string type argument");
        GenericConstructor i = new GenericConstructor(42);
        check(Integer.valueOf(42).equals(i.val), "integer type argument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
