/*
 * @test
 * @summary JLS 7.3: classes and interfaces declared in different compilation
 *          units can refer to each other circularly; a compiler must compile all
 *          such classes at the same time.
 * @compile A.java B.java Main.java
 * @run main p.Main
 */
package p;

public class Main {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        a.b = b;
        b.a = a;
        if (a.id != 1 || b.id != 2) throw new AssertionError("circular reference");
        if (a.b != b || b.a != a) throw new AssertionError("linkage");

        System.out.println("PASS");
    }
}
