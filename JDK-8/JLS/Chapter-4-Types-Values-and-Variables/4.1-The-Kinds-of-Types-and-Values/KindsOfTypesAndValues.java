/*
 * @test
 * @summary JLS 4.1 (Java SE 8): there are two kinds of types (primitive and
 *          reference) and two kinds of data values. There is also a special
 *          null type with no name whose only value is the null reference; the
 *          null reference may be assigned or cast to any reference type.
 *          Primitive values do not share state; reference values point to
 *          objects that may be shared.
 * @run main KindsOfTypesAndValues
 */

public class KindsOfTypesAndValues {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // Primitive values do not share state with other primitive values.
        int i1 = 3;
        int i2 = i1;
        i2 = 4;
        check(i1 == 3 && i2 == 4, "primitive values must be independent copies");

        // Reference values are pointers; two references may alias one object.
        Object o = new Object();
        Object alias = o;
        check(o == alias, "references to the same object compare ==");
        check(o != null, "a non-null reference is not the null reference");

        // The null reference may be assigned to any reference type.
        String s = null;
        Object n = null;
        int[] arr = null;
        Runnable r = null;
        check(s == null && n == null && arr == null && r == null,
              "null is assignable to every reference type");

        // The null type has no name, so it cannot be declared or cast to; only
        // the null literal denotes it. (No negative assertion is expressible
        // without producing an illegal program, which @compile/fail covers.)

        System.out.println("PASS (" + passed + " assertions)");
    }
}
