/*
 * @test
 * @summary JLS 9.6.4.4: the Override annotation marks a method that overrides or
 *          implements a supertype method; it has no effect on execution.
 * @run main OverrideAnnotation
 */

public class OverrideAnnotation {
    static class Base {
        int value() { return 1; }
    }

    static class Sub extends Base {
        @Override
        int value() { return 2; }
    }

    public static void main(String[] args) {
        if (new Sub().value() != 2) throw new AssertionError("override");
        System.out.println("PASS");
    }
}
