/*
 * @test
 * @summary JLS 17.4.4 / 17.4.5 (Java SE 8): a write to a volatile field
 *          happens-before any subsequent read of that field, so a plain write
 *          performed before the volatile write is visible to a reader that sees
 *          the volatile write.
 * @run main HappensBeforeVolatile
 */

public class HappensBeforeVolatile {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int data = 0;
    static volatile boolean ready = false;

    public static void main(String[] args) throws Exception {
        final int[] seen = {-1};

        Thread reader = new Thread(() -> {
            while (!ready) { Thread.yield(); }
            seen[0] = data;
        });

        reader.start();
        data = 42;
        ready = true;
        reader.join(5000);

        check(!reader.isAlive(), "reader terminated");
        check(seen[0] == 42, "volatile write happens-before read makes data visible");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
