/*
 * @test
 * @summary JLS 10.1: the supertype relation for array types is not the superclass
 *          relation. The direct superclass of Integer[] is Object (per the Class
 *          object), while the direct supertype of Integer[] is Number[].
 * @run main ArraySupertypeVsSuperclass
 */

public class ArraySupertypeVsSuperclass {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // Superclass relation: the direct superclass of every array type is Object.
        check(Integer[].class.getSuperclass() == Object.class,
              "Integer[] direct superclass is Object");
        check(int[].class.getSuperclass() == Object.class,
              "int[] direct superclass is Object");
        check(String[][].class.getSuperclass() == Object.class,
              "String[][] direct superclass is Object");

        // Supertype relation: Integer[] is a subtype of Number[] and Object[].
        Number[] na = new Integer[3];
        check(na instanceof Integer[], "Integer[] is instanceof Integer[]");
        Object[] oa = new Integer[2];
        check(oa instanceof Object[], "Integer[] is instanceof Object[]");

        // Object is a supertype of all array types, including primitive arrays.
        Object o = new int[1];
        check(o != null, "int[] is an Object");

        // int[] is NOT a subtype of Object[] (primitive component types differ).
        check(!(Object[].class.isAssignableFrom(int[].class)),
              "int[] is not assignable to Object[]");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
