/*
 * @test
 * @summary JLS 8.6 (Java SE 8): instance initializers are executed in the order they
 *          appear in the class body, interleaved with instance field initializers.
 * @run main MultipleInstanceInitializersTextualOrder
 */

public class MultipleInstanceInitializersTextualOrder {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    String log = "";
    { log += "a"; }
    String after = log;
    { log += "b"; }

    public static void main(String[] args) {
        MultipleInstanceInitializersTextualOrder o = new MultipleInstanceInitializersTextualOrder();
        check(o.log.equals("ab"), "initializers run in textual order");
        check(o.after.equals("a"), "field initializer runs between initializers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
