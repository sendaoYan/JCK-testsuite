/*
 * @test
 * @summary JLS 4.1: there are two kinds of types (primitive and reference) and
 *          two kinds of data values (primitive and reference). There is also a
 *          special null type with no name; the null reference may be assigned
 *          or cast to any reference type.
 * @run main KindsOfTypesAndValues
 */

public class KindsOfTypesAndValues {
    public static void main(String[] args) {
        // Primitive values do not share state with other primitive values.
        int i1 = 3;
        int i2 = i1;
        i2 = 4;
        if (i1 != 3 || i2 != 4) throw new AssertionError("primitive values must be independent");

        // Reference values are pointers to objects; null refers to no object.
        Object o = new Object();
        Object alias = o;
        if (o != alias) throw new AssertionError("references must point to the same object");
        if (o == null) throw new AssertionError("non-null reference");

        // The null reference may be assigned to any reference type.
        String s = null;
        Object n = null;
        int[] arr = null;
        if (s != null || n != null || arr != null) throw new AssertionError("null must be null");

        // The null type has no name: it cannot be declared or cast to.
        // (Represented here by its only value, the null literal, being typeless.)

        System.out.println("PASS");
    }
}
