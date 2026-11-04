/*
 * @test
 * @summary JLS 10.8 (Java SE 8): the Class object of every array has direct
 *          superclass Object and implements Cloneable and java.io.Serializable.
 * @run main ArrayImplementsCloneableSerializable
 */

public class ArrayImplementsCloneableSerializable {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        for (Class<?> c : new Class<?>[] { int[].class, String[].class, int[][].class }) {
            check(c.isArray(), c.getName() + " is an array");
            check(c.getSuperclass() == Object.class, c.getName() + " superclass is Object");
            check(Cloneable.class.isAssignableFrom(c), c.getName() + " implements Cloneable");
            check(java.io.Serializable.class.isAssignableFrom(c), c.getName() + " implements Serializable");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
