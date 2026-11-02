/*
 * @test
 * @summary jdk.incubator.vector.DoubleVector conformance: broadcast and lane.
 * @modules jdk.incubator.vector
 * @run main DoubleVectorTest
 */

import jdk.incubator.vector.*;

public class DoubleVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        DoubleVector v = DoubleVector.broadcast(DoubleVector.SPECIES_PREFERRED, 3.5);
        check(v.length() > 0, "length");
        check(v.lane(0) == 3.5, "lane");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
