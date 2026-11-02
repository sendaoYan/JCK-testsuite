/*
 * @test
 * @summary jdk.incubator.vector.FloatVector conformance: broadcast and lane.
 * @modules jdk.incubator.vector
 * @run main FloatVectorTest
 */

import jdk.incubator.vector.*;

public class FloatVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        FloatVector v = FloatVector.broadcast(FloatVector.SPECIES_PREFERRED, 2.5f);
        check(v.length() > 0, "length");
        check(v.lane(0) == 2.5f, "lane");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
