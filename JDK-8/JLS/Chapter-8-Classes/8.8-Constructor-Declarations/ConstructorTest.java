/*
 * @test
 * @summary JLS 8.8 (Java SE 8): a default constructor is provided when none is
 *          declared; constructors may be overloaded and may invoke another
 *          constructor via this(...) or a superclass constructor via super(...).
 * @run main ConstructorTest
 */

public class ConstructorTest {
    static class A {
        int x;
        A(int v) { x = v; }
        A() { this(42); }
    }
    static class B extends A {
        int y;
        B() { super(7); y = 8; }
    }
    static class C {
        int z;
    }

    public static void main(String[] args) {
        A a = new A();
        if (a.x != 42) throw new AssertionError("this() invocation");
        B b = new B();
        if (b.x != 7 || b.y != 8) throw new AssertionError("super() invocation");
        C c = new C();   // default constructor
        if (c.z != 0) throw new AssertionError("default constructor + default field value");
        System.out.println("PASS");
    }
}
