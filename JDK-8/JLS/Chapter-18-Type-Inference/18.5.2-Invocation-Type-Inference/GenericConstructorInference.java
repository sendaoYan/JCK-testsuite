/*
 * @test
 * @summary JLS 18.5.2 (Java SE 8): a generic constructor invoked with the diamond
 *          operator has its class type argument inferred from the target type and
 *          its own type argument inferred from the constructor arguments.
 * @run main GenericConstructorInference
 */

public class GenericConstructorInference {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Pair<T> {
        final T first;
        final T second;
        <U extends T> Pair(U a, U b) {
            first = a;
            second = b;
        }
    }

    public static void main(String[] args) {
        Pair<Number> p = new Pair<>(1, 2.0);
        check(p.first.intValue() == 1 && p.second.doubleValue() == 2.0,
              "generic constructor + diamond infers T and U");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
