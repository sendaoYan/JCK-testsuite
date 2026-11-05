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


    static void testDeepen() {
        VectorSpecies<Integer> sp = IntVector.SPECIES_256;
        VectorMask<Integer> m = VectorMask.fromValues(sp, true, false, true, false, true, false, true, false);
        check(m.toString().equals("Mask[T.T.T.T.]"), "toString exact");
        check(m.toLong() == 0b1010101L, "toLong bit i == lane i");
        check(VectorMask.fromLong(sp, m.toLong()).equals(m), "fromLong round-trip");
        check(m.firstTrue() == 0 && m.lastTrue() == 6, "firstTrue/lastTrue");
        check(sp.maskAll(false).firstTrue() == sp.length(), "allFalse firstTrue == length");
        check(sp.maskAll(false).lastTrue() == -1, "allFalse lastTrue == -1");
        check(m.xor(m).trueCount() == 0, "xor self empty");
        check(m.andNot(m).trueCount() == 0, "andNot self empty");
        check(m.andNot(m.not()).trueCount() == 4, "andNot inverted");
        check(m.eq(m).allTrue(), "eq self allTrue");
        check(m.not().not().equals(m), "double not identity");
        check(m.equals(VectorMask.fromValues(sp, true, false, true, false, true, false, true, false)), "equals value");
        check(m.hashCode() == VectorMask.fromValues(sp, true, false, true, false, true, false, true, false).hashCode(), "hashCode consistent");
        boolean[] bits = m.toArray();
        check(bits.length == sp.length() && bits[0] && !bits[1] && bits[6], "toArray");
        boolean[] out = new boolean[sp.length()];
        m.intoArray(out, 0);
        for (int i = 0; i < bits.length; i++) check(out[i] == bits[i], "intoArray lane " + i);
        check(m.compress().toString().equals("Mask[TTTT....]"), "compress exact");
check(m.indexInRange(3, 8).toString().equals("Mask[T.T.T...]"), "indexInRange set && 3+i<8");
        check(VectorMask.fromValues(sp, false, true, true, true, true, true, true, true).indexInRange(1, 4)
                .toString().equals("Mask[.TT.....]"), "indexInRange bound excluded");
        check(m.cast(DoubleVector.SPECIES_512).toString().equals(m.toString()), "cast same lane count");
        boolean iae = false;
        try { m.cast(IntVector.SPECIES_64); } catch (IllegalArgumentException e) {
            iae = e.getMessage().equals("VectorMask length and species length differ");
        }
        check(iae, "cast shorter species IAE exact message");
        check(((IntVector) m.toVector()).lane(0) == -1 && ((IntVector) m.toVector()).lane(1) == 0, "toVector -1/0 lanes");
        check(m.or(m.not()).allTrue(), "or not allTrue");
        check(m.and(m.not()).trueCount() == 0, "and not empty");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
