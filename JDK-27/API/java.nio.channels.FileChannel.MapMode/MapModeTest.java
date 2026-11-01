/*
 * @test
 * @summary java.nio.channels.FileChannel.MapMode conformance: constants.
 * @run main MapModeTest
 */

import java.nio.channels.FileChannel;

public class MapModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMapMode() {
        check(FileChannel.MapMode.READ_ONLY != null, "READ_ONLY");
        check(FileChannel.MapMode.READ_WRITE != null, "READ_WRITE");
        check(FileChannel.MapMode.PRIVATE != null, "PRIVATE");
        check(FileChannel.MapMode.READ_ONLY != FileChannel.MapMode.READ_WRITE, "distinct modes");
        check(FileChannel.MapMode.READ_ONLY.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testMapMode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
