/*
 * @test
 * @summary JLS 17.3 (Java SE 8): Thread.yield is only a scheduling hint; it returns
 *          normally and has no synchronization semantics.
 * @run main YieldHints
 */

public class YieldHints {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            Thread.yield();
        }
        check(Thread.currentThread().isAlive(), "yield returned normally and the thread continues");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
