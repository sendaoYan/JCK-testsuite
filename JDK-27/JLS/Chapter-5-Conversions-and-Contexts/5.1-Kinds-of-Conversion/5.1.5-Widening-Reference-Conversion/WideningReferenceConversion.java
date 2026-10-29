/*
 * @test
 * @summary JLS 5.1.5: a widening reference conversion exists from any reference
 *          type S to any reference type T if S is a subtype of T. It never
 *          requires a run-time action and never throws.
 * @run main WideningReferenceConversion
 */

interface I {}
class A {}
class B extends A implements I {}

public class WideningReferenceConversion {
    public static void main(String[] args) {
        B b = new B();

        A a = b;                 // B -> A (subclass to superclass)
        Object o = b;            // B -> Object
        I i = b;                 // B -> I (implemented interface)

        // Arrays are covariant: String[] -> Object[].
        Object[] oa = new String[0];

        // Widening reference conversion requires no action and never throws.
        if (!(a instanceof B)) throw new AssertionError("B -> A");
        if (!(o instanceof B)) throw new AssertionError("B -> Object");
        if (!(i instanceof B)) throw new AssertionError("B -> I");
        if (!(oa instanceof String[])) throw new AssertionError("String[] -> Object[]");

        System.out.println("PASS");
    }
}
