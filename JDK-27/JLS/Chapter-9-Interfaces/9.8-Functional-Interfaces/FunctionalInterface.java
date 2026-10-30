/*
 * @test
 * @summary JLS 9.8: a functional interface has exactly one abstract method and
 *          is a valid target type for a lambda expression.
 * @run main FunctionalInterface
 */

public class FunctionalInterface {
    interface Binary { int apply(int a, int b); }

    public static void main(String[] args) {
        Binary add = (a, b) -> a + b;
        Binary mul = (a, b) -> a * b;
        if (add.apply(2, 3) != 5 || mul.apply(2, 3) != 6)
            throw new AssertionError("functional interface lambda");
        System.out.println("PASS");
    }
}
