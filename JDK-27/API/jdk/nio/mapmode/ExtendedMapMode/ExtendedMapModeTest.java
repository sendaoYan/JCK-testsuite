/*
 * @test
 * @summary jdk.nio.mapmode.ExtendedMapMode conformance: extended map mode.
 * @run main ExtendedMapModeTest
 */

public class ExtendedMapModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(jdk.nio.mapmode.ExtendedMapMode.READ_ONLY_SYNC != null, "READ_ONLY_SYNC");
        check(jdk.nio.mapmode.ExtendedMapMode.READ_WRITE_SYNC != null, "READ_WRITE_SYNC");
        check(jdk.nio.mapmode.ExtendedMapMode.READ_ONLY_SYNC != java.nio.channels.FileChannel.MapMode.READ_ONLY, "distinct from READ_ONLY");
        check(jdk.nio.mapmode.ExtendedMapMode.READ_WRITE_SYNC != java.nio.channels.FileChannel.MapMode.READ_WRITE, "distinct from READ_WRITE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
