/*
 * @test
 * @summary JLS 10.6 (Java SE 8): variable initializers are executed left-to-right;
 *          the n'th initializer specifies the value of the n-1'th component.
 * @run main ArrayInitializerEvaluationOrder
 */

public class ArrayInitializerEvaluationOrder {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static StringBuilder order = new StringBuilder();
    static int init(int v) {
        order.append(v);
        return v;
    }

    public static void main(String[] args) {
        int[] a = new int[] { init(10), init(20), init(30) };
        check(order.toString().equals("102030"), "initializers executed left-to-right: " + order);
        check(a[0] == 10 && a[1] == 20 && a[2] == 30, "nth initializer -> n-1th component");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
