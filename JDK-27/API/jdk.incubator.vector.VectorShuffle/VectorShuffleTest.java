/*
 * @test
 * @summary jdk.incubator.vector.VectorShuffle conformance: shuffle iota.
 * @modules jdk.incubator.vector
 * @run main VectorShuffleTest
 */

import jdk.incubator.vector.*;

public class VectorShuffleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        VectorShuffle<Integer> s = VectorShuffle.iota(IntVector.SPECIES_PREFERRED, 0, 1, false);
        check(s.length() > 0, "length");
        check(s.laneSource(0) == 0, "laneSource");
        check(s.vectorSpecies() == IntVector.SPECIES_PREFERRED, "species");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
