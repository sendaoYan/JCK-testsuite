/*
 * @test
 * @summary jdk.incubator.vector.LongVector conformance: broadcast and lane.
 * @modules jdk.incubator.vector
 * @run main LongVectorTest
 */

import jdk.incubator.vector.*;

public class LongVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        LongVector v = LongVector.broadcast(LongVector.SPECIES_PREFERRED, 99L);
        check(v.length() > 0, "length");
        check(v.lane(0) == 99L, "lane");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
