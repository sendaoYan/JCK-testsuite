/*
 * @test
 * @summary JLS 12.4.1 (Java SE 8, Example 12.4.1-2): a reference to a static field
 *          causes initialization only of the class that actually declares it.
 * @run main OnlyDeclaringClassInitialized
 */

public class OnlyDeclaringClassInitialized {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static StringBuilder log = new StringBuilder();

    static class Super { static int taxi = 1729; }
    static class Sub extends Super { static { log.append("Sub "); } }

    public static void main(String[] args) {
        int v = Sub.taxi;
        check(v == 1729, "taxi value");
        check("".equals(log.toString()), "Sub should not be initialized: " + log);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
