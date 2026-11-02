/*
 * @test
 * @summary jdk.incubator.vector.ByteVector conformance: broadcast and lane.
 * @modules jdk.incubator.vector
 * @run main ByteVectorTest
 */

import jdk.incubator.vector.*;

public class ByteVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ByteVector v = ByteVector.broadcast(ByteVector.SPECIES_PREFERRED, (byte)7);
        check(v.length() > 0, "length");
        check(v.lane(0) == (byte)7, "lane");
        check(ByteVector.SPECIES_PREFERRED.length() > 0, "species length");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
