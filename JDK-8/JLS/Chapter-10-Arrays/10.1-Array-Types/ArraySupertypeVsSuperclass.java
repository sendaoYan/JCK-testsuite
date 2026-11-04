/*
 * @test
 * @summary JLS 10.1 (Java SE 8): the supertype relation for array types is not the
 *          superclass relation; the direct superclass of Integer[] is Object while
 *          the direct supertype of Integer[] is Number[].
 * @run main ArraySupertypeVsSuperclass
 */

public class ArraySupertypeVsSuperclass {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Integer[].class.getSuperclass() == Object.class, "Integer[] superclass is Object");
        check(int[].class.getSuperclass() == Object.class, "int[] superclass is Object");

        Number[] na = new Integer[3];
        check(na instanceof Integer[], "Integer[] is instanceof Integer[]");

        Object o = new int[1];
        check(o != null, "int[] is an Object");
        check(!(Object[].class.isAssignableFrom(int[].class)), "int[] not assignable to Object[]");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
