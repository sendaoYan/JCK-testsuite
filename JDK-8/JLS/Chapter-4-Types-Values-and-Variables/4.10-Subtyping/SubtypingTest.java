/*
 * @test
 * @summary JLS 4.10 (Java SE 8): subtyping is reflexive and transitive, and
 *          array types are covariant in their component type.
 * @run main SubtypingTest
 */

public class SubtypingTest {
    static class A {}
    static class B extends A {}
    interface I {}
    static class C extends B implements I {}

    public static void main(String[] args) {
        A a = new B();
        Object o = new C();
        I i = new C();
        if (o == null || a == null || i == null) throw new AssertionError("subtype assignment");

        if (!(new C() instanceof A)) throw new AssertionError("C <: B <: A");
        if (!(new C() instanceof I)) throw new AssertionError("C <: I");

        // Array covariance.
        C[] cs = new C[1];
        B[] bs = cs;
        A[] as = cs;
        Object[] os = cs;
        if (!(cs instanceof B[]) || !(cs instanceof A[]) || !(cs instanceof Object[]))
            throw new AssertionError("array covariance");

        System.out.println("PASS");
    }
}
