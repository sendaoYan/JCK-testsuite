/*
 * @test
 * @summary JLS 9.8 (Java SE 8): an interface with a single abstract method is a
 *          functional interface and can be implemented by a lambda expression.
 * @run main FunctionalInterfaceTest
 */

public class FunctionalInterfaceTest {
    interface IntOp { int apply(int a, int b); }

    public static void main(String[] args) {
        IntOp add = (a, b) -> a + b;
        IntOp mul = (a, b) -> a * b;
        if (add.apply(3, 4) != 7) throw new AssertionError("lambda add");
        if (mul.apply(3, 4) != 12) throw new AssertionError("lambda mul");
        System.out.println("PASS");
    }
}
