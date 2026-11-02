/*
 * @test
 * @summary jdk.incubator.vector.Float16 conformance: half-precision values.
 * @modules jdk.incubator.vector
 * @run main Float16Test
 */

import jdk.incubator.vector.*;

public class Float16Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Float16 h = Float16.valueOf(1.0f);
        check(h.floatValue() == 1.0f, "floatValue");
        check(Float16.isFinite(h), "isFinite");
        check(!Float16.isNaN(h), "isNaN false");
        check(Float16.SIZE == 16, "SIZE");
        check(Float16.BYTES == 2, "BYTES");
        check(Float16.valueOf("1.0").floatValue() == 1.0f, "valueOf string");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
