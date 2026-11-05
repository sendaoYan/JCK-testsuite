/*
 * @test
 * @summary jdk.nio.mapmode.ExtendedMapMode conformance: extended map mode.
 * @run main ExtendedMapModeTest
 */

public class ExtendedMapModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }


    static void testDeepen() {
        java.nio.channels.FileChannel.MapMode ro = jdk.nio.mapmode.ExtendedMapMode.READ_ONLY_SYNC;
        java.nio.channels.FileChannel.MapMode rw = jdk.nio.mapmode.ExtendedMapMode.READ_WRITE_SYNC;
        check(ro.toString().equals("READ_ONLY_SYNC"), "READ_ONLY_SYNC toString");
        check(rw.toString().equals("READ_WRITE_SYNC"), "READ_WRITE_SYNC toString");
        check(ro != rw, "two constants distinct");
        check(ro != java.nio.channels.FileChannel.MapMode.PRIVATE && rw != java.nio.channels.FileChannel.MapMode.PRIVATE, "distinct from PRIVATE");
        check(ro instanceof java.nio.channels.FileChannel.MapMode && rw instanceof java.nio.channels.FileChannel.MapMode, "both MapMode instances");
        check(ro.equals(ro) && !ro.equals(rw), "identity equals semantics");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        check(jdk.nio.mapmode.ExtendedMapMode.READ_ONLY_SYNC != null, "READ_ONLY_SYNC");
        check(jdk.nio.mapmode.ExtendedMapMode.READ_WRITE_SYNC != null, "READ_WRITE_SYNC");
        check(jdk.nio.mapmode.ExtendedMapMode.READ_ONLY_SYNC != java.nio.channels.FileChannel.MapMode.READ_ONLY, "distinct from READ_ONLY");
        check(jdk.nio.mapmode.ExtendedMapMode.READ_WRITE_SYNC != java.nio.channels.FileChannel.MapMode.READ_WRITE, "distinct from READ_WRITE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
