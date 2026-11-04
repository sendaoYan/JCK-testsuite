/*
 * @test
 * @summary java.lang.Process API conformance: read output and observe exit status.
 * @run main ProcessTest
 */

import java.io.InputStream;

public class ProcessTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testProcess() throws Exception {
        Process p = new ProcessBuilder("sh", "-c", "echo hello").start();
        InputStream in = p.getInputStream();
        byte[] buf = new byte[64];
        int n = in.read(buf);
        String out = new String(buf, 0, n).trim();
        check(p.waitFor() == 0, "exit 0");
        check("hello".equals(out), "output hello");
        check(p.exitValue() == 0, "exitValue");
    }

    public static void main(String[] args) throws Exception {
        testProcess();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
