/*
 * @test
 * @summary JLS 9.1/9.2/9.3 (Java SE 8): an interface declares abstract methods and
 *          public static final fields; a class implements the interface.
 * @run main InterfaceBasicTest
 */

public class InterfaceBasicTest {
    interface I {
        int CONST = 5;          // implicitly public static final
        int f();
    }
    static class C implements I {
        public int f() { return CONST; }
    }

    public static void main(String[] args) {
        C c = new C();
        if (c.f() != 5) throw new AssertionError("implemented method");
        if (I.CONST != 5) throw new AssertionError("interface constant");
        if (c.CONST != 5) throw new AssertionError("inherited constant");
        System.out.println("PASS");
    }
}
