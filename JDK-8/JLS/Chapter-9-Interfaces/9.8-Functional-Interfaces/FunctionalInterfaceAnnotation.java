/*
 * @test
 * @summary JLS 9.8 (Java SE 8): the FunctionalInterface annotation marks a functional
 *          interface, and a lambda expression or method reference can instantiate it.
 * @run main FunctionalInterfaceAnnotation
 */

public class FunctionalInterfaceAnnotation {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @FunctionalInterface
    interface F { int apply(int x); }

    public static void main(String[] args) {
        check(F.class.isAnnotationPresent(FunctionalInterface.class),
              "@FunctionalInterface is present");
        F inc = x -> x + 1;
        check(inc.apply(41) == 42, "lambda over functional interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
