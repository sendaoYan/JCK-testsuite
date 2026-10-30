/*
 * @test
 * @summary JLS 8.8.4: a constructor is generic if it declares type variables, and
 *          the type arguments are inferred at each invocation.
 * @run main GenericConstructor
 */

public class GenericConstructor {
    Object val;
    <T> GenericConstructor(T t) { val = t; }

    public static void main(String[] args) {
        GenericConstructor g = new GenericConstructor("hello");
        if (!"hello".equals(g.val)) throw new AssertionError("string");
        GenericConstructor i = new GenericConstructor(42);
        if (!Integer.valueOf(42).equals(i.val)) throw new AssertionError("integer");
        System.out.println("PASS");
    }
}
