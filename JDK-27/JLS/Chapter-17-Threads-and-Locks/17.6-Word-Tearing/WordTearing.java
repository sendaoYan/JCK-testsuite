/*
 * @test
 * @summary JLS 17.6: every field and array element is distinct; threads updating
 *          adjacent elements of a byte array must not interfere, so no word
 *          tearing occurs.
 * @run main WordTearing
 */

public class WordTearing {
    static final int LENGTH = 8;
    static final int ITERS = 200_000;
    static final byte[] counts = new byte[LENGTH];
    static final boolean[] failed = new boolean[LENGTH];

    public static void main(String[] args) throws Exception {
        Thread[] threads = new Thread[LENGTH];
        for (int id = 0; id < LENGTH; id++) {
            final int i = id;
            threads[id] = new Thread(() -> {
                byte v = 0;
                for (int n = 0; n < ITERS; n++) {
                    byte v2 = counts[i];
                    if (v != v2) {          // another write tore our element
                        failed[i] = true;
                        return;
                    }
                    v++;
                    counts[i] = v;
                }
            });
            threads[id].start();
        }

        for (Thread t : threads) t.join();

        for (int i = 0; i < LENGTH; i++) {
            if (failed[i]) throw new AssertionError("word tearing at counts[" + i + "]");
        }

        System.out.println("PASS");
    }
}
