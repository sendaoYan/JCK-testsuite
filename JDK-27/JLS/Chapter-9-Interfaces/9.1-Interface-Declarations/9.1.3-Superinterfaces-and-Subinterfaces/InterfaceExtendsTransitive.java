/*
 * @test
 * @summary JLS 9.1.3: the superinterface relationship is the transitive closure
 *          of the direct superinterface relationship.
 * @run main InterfaceExtendsTransitive
 */

interface A { int a(); }
interface B extends A { int b(); }
interface C extends B { int c(); }

public class InterfaceExtendsTransitive {
    static class Impl implements C {
        public int a() { return 1; }
        public int b() { return 2; }
        public int c() { return 3; }
    }

    public static void main(String[] args) {
        C c = new Impl();
        A a = c;   // C is a subtype of A transitively
        if (a.a() != 1 || c.b() != 2 || c.c() != 3) throw new AssertionError("transitive extends");
        System.out.println("PASS");
    }
}
