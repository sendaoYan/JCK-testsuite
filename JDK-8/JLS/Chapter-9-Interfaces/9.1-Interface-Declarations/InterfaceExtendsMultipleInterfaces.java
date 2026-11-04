/*
 * @test
 * @summary JLS 9.1.3 (Java SE 8): an interface may extend more than one interface;
 *          the extending interface inherits their members.
 * @run main InterfaceExtendsMultipleInterfaces
 */

public class InterfaceExtendsMultipleInterfaces {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface A { int a(); }
    interface B { int b(); }
    interface C extends A, B { int c(); }

    static class Impl implements C {
        public int a() { return 1; }
        public int b() { return 2; }
        public int c() { return 3; }
    }

    public static void main(String[] args) {
        C c = new Impl();
        check(c.a() == 1, "inherited from A");
        check(c.b() == 2, "inherited from B");
        check(c.c() == 3, "declared in C");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
