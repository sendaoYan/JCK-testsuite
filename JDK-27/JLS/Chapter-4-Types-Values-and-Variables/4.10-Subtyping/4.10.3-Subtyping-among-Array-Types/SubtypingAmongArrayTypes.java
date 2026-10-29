/*
 * @test
 * @summary JLS 4.10.3: array types are covariant: S[] >1 T[] iff S >1 T.
 *          Object, Cloneable, and java.io.Serializable are direct supertypes
 *          of every array type (Object[] and primitive arrays alike).
 * @run main SubtypingAmongArrayTypes
 */

public class SubtypingAmongArrayTypes {
    public static void main(String[] args) {
        // String[] <: Object[] (reference array covariance).
        Object[] oa = new String[0];
        if (!(oa instanceof String[])) throw new AssertionError("String[] <: Object[]");

        // Object[] <: Object.
        Object o = oa;
        if (o == null) throw new AssertionError("Object[] <: Object");

        // Object[] <: Cloneable and Serializable.
        if (!(oa instanceof Cloneable)) throw new AssertionError("Object[] <: Cloneable");
        if (!(oa instanceof java.io.Serializable)) throw new AssertionError("Object[] <: Serializable");

        // Primitive arrays: Object, Cloneable, Serializable are direct supertypes.
        int[] ia = new int[0];
        if (!(ia instanceof Object)) throw new AssertionError("int[] <: Object");
        if (!(ia instanceof Cloneable)) throw new AssertionError("int[] <: Cloneable");
        if (!(ia instanceof java.io.Serializable)) throw new AssertionError("int[] <: Serializable");

        // int[] is NOT a subtype of Object[] (primitive vs reference).
        // (Verified as a compile-time error in a separate @compile/fail test.)

        System.out.println("PASS");
    }
}
