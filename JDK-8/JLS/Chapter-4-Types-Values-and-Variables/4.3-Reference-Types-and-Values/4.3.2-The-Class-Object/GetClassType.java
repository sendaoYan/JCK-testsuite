/*
 * @test
 * @summary JLS 4.3.2 (Java SE 8): the type of a getClass() method invocation
 *          expression is Class<? extends |T|>, where T is the type searched for
 *          getClass; the returned Class object reflects the run-time class.
 * @run main GetClassType
 */

public class GetClassType {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Number n = new Integer(5);

        // Compile-time type of n.getClass() is Class<? extends Number>.
        Class<? extends Number> c = n.getClass();
        check(c == Integer.class, "runtime class of an Integer-valued Number");

        // getClass returns the run-time class, not the declared type.
        Object o = "abc";
        check(o.getClass() == String.class, "runtime class of an Object-valued String");

        // Class object identity: same runtime class yields the same Class.
        check("x".getClass() == "y".getClass(), "equal run-time classes share one Class object");
        check(new int[0].getClass() == int[].class, "array runtime class");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
