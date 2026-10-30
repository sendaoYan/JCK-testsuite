/*
 * @test
 * @summary JLS 9.2: an interface with no direct superinterfaces implicitly
 *          declares public abstract member methods corresponding to the public
 *          instance methods of Object.
 * @run main InterfaceImplicitObjectMethods
 */

public class InterfaceImplicitObjectMethods {
    interface I {}   // implicitly declares equals, hashCode, toString

    public static void main(String[] args) {
        I i = new I() {};
        // The implicit members corresponding to Object methods are available.
        if (!i.equals(i)) throw new AssertionError("equals");
        if (i.hashCode() != i.hashCode()) throw new AssertionError("hashCode");
        if (i.toString() == null) throw new AssertionError("toString");
        System.out.println("PASS");
    }
}
