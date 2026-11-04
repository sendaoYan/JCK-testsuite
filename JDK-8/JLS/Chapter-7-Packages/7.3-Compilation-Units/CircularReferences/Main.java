/*
 * @test
 * @summary JLS 7.3 (Java SE 8): classes and interfaces declared in different
 *          compilation units can refer to each other circularly; a compiler must
 *          compile all such classes at the same time.
 * @compile A.java B.java Main.java
 * @run main p.Main
 */
package p;

public class Main {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        a.b = b;
        b.a = a;
        check(a.id == 1, "A field");
        check(b.id == 2, "B field");
        check(a.b == b, "A->B linkage");
        check(b.a == a, "B->A linkage");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
