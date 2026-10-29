/*
 * @test
 * @summary JLS 5.1.6: a narrowing reference conversion treats an expression of
 *          reference type S as a different reference type T where S is not a
 *          subtype of T; it may require a run-time test (ClassCastException).
 * @run main NarrowingReferenceConversion
 */

interface I {}
class A {}
class B extends A implements I {}

public class NarrowingReferenceConversion {
    public static void main(String[] args) {
        // Object -> String (S is Object, T is String; Object is not <: String).
        Object o = "hello";
        String s = (String) o;      // narrowing reference conversion
        if (!s.equals("hello")) throw new AssertionError("Object -> String");

        // Object -> A (downcast).
        Object oa = new B();
        A a = (A) oa;
        if (!(a instanceof B)) throw new AssertionError("Object -> A");

        // Object -> int[] (S is Object, T is array type; allowed case 2).
        Object oarr = new int[3];
        int[] ia = (int[]) oarr;
        if (ia.length != 3) throw new AssertionError("Object -> int[]");

        System.out.println("PASS");
    }
}
