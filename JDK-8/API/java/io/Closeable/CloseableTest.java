/*
 * @test
 * @summary java.io.Closeable API conformance: close() and extends AutoCloseable.
 * @run main CloseableTest
 */

import java.io.Closeable;

public class CloseableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCloseable() throws Exception {
        Closeable c = () -> {};
        c.close();
        passed++; // close() succeeded
        check(c instanceof AutoCloseable, "Closeable extends AutoCloseable");
    }

    public static void main(String[] args) throws Exception {
        testCloseable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
