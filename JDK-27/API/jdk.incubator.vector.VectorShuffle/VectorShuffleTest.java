/*
 * @test
 * @summary jdk.incubator.vector.VectorShuffle conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main VectorShuffleTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class VectorShuffleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorShuffle");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "length"), "length");
        check(hasMethod(c, "laneSource"), "laneSource");
        check(hasMethod(c, "vectorSpecies"), "vectorSpecies");
        check(hasMethod(c, "iota"), "iota");
        check(hasMethod(c, "fromValues"), "fromValues");
    }

    static void testBlackBox() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_256;
        int n = sp.length();
        VectorShuffle<Integer> iota = VectorShuffle.iota(sp, 0, 1, false);
        check(iota.length() == n, "iota length");
        check(iota.laneSource(0) == 0, "iota laneSource 0");
        check(iota.laneSource(n - 1) == n - 1, "iota laneSource last");
        check(iota.vectorSpecies() == sp, "iota species");
        int[] src = new int[n];
        for (int i = 0; i < n; i++) src[i] = n - 1 - i;
        VectorShuffle<Integer> rev = VectorShuffle.fromValues(sp, src);
        check(rev.laneSource(0) == n - 1, "fromValues reverse first");
        check(rev.laneSource(n - 1) == 0, "fromValues reverse last");
        VectorShuffle<Integer> wrap = iota.rearrange(rev);
        check(wrap.laneSource(0) == n - 1, "rearrange applied");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
