/*
 * @test
 * @summary JLS 8.1.3 (Java SE 8): a non-static inner class has an enclosing
 *          instance reachable via Outer.this; a static nested class does not.
 * @run main InnerClassTest
 */

public class InnerClassTest {
    private int x = 10;

    class Inner {
        int getX() { return x; }
        int getOuterX() { return InnerClassTest.this.x; }
    }

    static class StaticNested {
        int f() { return 42; }
    }

    public static void main(String[] args) {
        InnerClassTest outer = new InnerClassTest();
        Inner in = outer.new Inner();
        if (in.getX() != 10 || in.getOuterX() != 10) throw new AssertionError("inner enclosing instance");

        StaticNested sn = new StaticNested();
        if (sn.f() != 42) throw new AssertionError("static nested class");

        System.out.println("PASS");
    }
}
