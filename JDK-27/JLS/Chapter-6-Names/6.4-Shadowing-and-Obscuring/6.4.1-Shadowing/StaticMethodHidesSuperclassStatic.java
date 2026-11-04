/*
 * @test
 * @summary JLS 6.4.1 / 8.4.8.2 (Java SE 27): a static method declared in a
 *          subclass hides a static method with the same signature in a
 *          superclass; the hidden method remains reachable through the
 *          superclass type name.
 * @run main StaticMethodHidesSuperclassStatic
 */

public class StaticMethodHidesSuperclassStatic {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static class Base { static int m() { return 1; } }
    static class Sub extends Base { static int m() { return 2; } }

    public static void main(String[] args) {
        check(Sub.m() == 2, "subclass static method hides superclass static method");
        check(Base.m() == 1, "hidden method reachable through superclass type name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
