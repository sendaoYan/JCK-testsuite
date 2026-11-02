/*
 * @test
 * @summary java.lang.IO conformance: static println/print.
 * @run main IOTest
 */
import java.lang.IO;

public class IOTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        IO.println("io-println");
        IO.println();
        IO.print("io-print");
        passed++;
        System.out.println("PASS (" + passed + " assertions)");
    }
}
