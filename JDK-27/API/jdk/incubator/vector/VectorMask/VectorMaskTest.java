/*
 * @test
 * @summary jdk.incubator.vector.VectorMask conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main VectorMaskTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class VectorMaskTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorMask");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "length"), "length");
        check(hasMethod(c, "trueCount"), "trueCount");
        check(hasMethod(c, "allTrue"), "allTrue");
        check(hasMethod(c, "anyTrue"), "anyTrue");
        check(hasMethod(c, "fromArray"), "fromArray");
        check(hasMethod(c, "fromValues"), "fromValues");
    }

    static void testBlackBox() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_256;
        int n = sp.length();
        VectorMask<Integer> all = sp.maskAll(true);
        check(all.length() == n, "maskAll length");
        check(all.allTrue(), "maskAll allTrue");
        check(all.trueCount() == n, "maskAll trueCount");
        check(all.not().trueCount() == 0, "not trueCount zero");
        check(all.and(all).allTrue(), "and self allTrue");
        boolean[] bits = new boolean[n];
        bits[0] = true;
        VectorMask<Integer> one = VectorMask.fromArray(sp, bits, 0);
        check(one.trueCount() == 1, "fromArray trueCount 1");
        check(one.laneIsSet(0) && !one.laneIsSet(n - 1), "laneIsSet pattern");
        check(one.anyTrue(), "anyTrue");
        check(((IntVector) one.toVector()).lane(0) == -1, "toVector lane0 all-ones");
        VectorMask<Integer> merged = one.or(all);
        check(merged.trueCount() == n, "or fills mask");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
