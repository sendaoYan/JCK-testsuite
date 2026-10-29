/*
 * @test
 * @summary JLS 4.3.4: two reference types are the same run-time type if they
 *          are both class (or both interface) types defined by the same class
 *          loader with the same binary name, or both array types whose
 *          component types are the same run-time type.
 * @run main WhenReferenceTypesAreTheSame
 */

class A {}
interface I {}

public class WhenReferenceTypesAreTheSame {
    public static void main(String[] args) {
        // Same compile-time type, hence same run-time type.
        if (A.class != A.class) throw new AssertionError("A == A");
        if (String.class != String.class) throw new AssertionError("String == String");

        // Two references of the same type share the same run-time Class object.
        A a1 = new A();
        A a2 = new A();
        if (a1.getClass() != a2.getClass()) throw new AssertionError("same run-time class");

        // Interfaces are also reference types with a Class object.
        if (!I.class.isInterface()) throw new AssertionError("I must be an interface");

        // Array types: same component type -> same run-time type.
        if (int[].class != int[].class) throw new AssertionError("int[] == int[]");
        if (a1.getClass().equals(int[].class)) throw new AssertionError("A != int[]");
        String[] sa = new String[0];
        Object[] oa = new String[0];
        if (sa.getClass() != oa.getClass()) {
            throw new AssertionError("String[] and Object[] must be the same run-time type");
        }

        // Different types are distinct run-time types.
        if (A.class.equals(String.class)) throw new AssertionError("A != String");

        System.out.println("PASS");
    }
}
