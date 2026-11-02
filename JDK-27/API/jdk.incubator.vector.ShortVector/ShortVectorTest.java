/*
 * @test
 * @summary jdk.incubator.vector.ShortVector conformance: broadcast and lane.
 * @modules jdk.incubator.vector
 * @run main ShortVectorTest
 */

import jdk.incubator.vector.*;

public class ShortVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ShortVector v = ShortVector.broadcast(ShortVector.SPECIES_PREFERRED, (short)5);
        check(v.length() > 0, "length");
        check(v.lane(0) == (short)5, "lane");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
