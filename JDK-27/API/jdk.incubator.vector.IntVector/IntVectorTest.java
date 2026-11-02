/*
 * @test
 * @summary jdk.incubator.vector.IntVector conformance: broadcast and lane.
 * @modules jdk.incubator.vector
 * @run main IntVectorTest
 */

import jdk.incubator.vector.*;

public class IntVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        IntVector v = IntVector.broadcast(IntVector.SPECIES_PREFERRED, 42);
        check(v.length() > 0, "length");
        check(v.lane(0) == 42, "lane");
        check(IntVector.SPECIES_PREFERRED.elementType() == int.class, "element type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
