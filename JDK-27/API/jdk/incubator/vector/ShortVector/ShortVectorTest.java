/*
 * @test
 * @summary jdk.incubator.vector.ShortVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main ShortVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class ShortVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.ShortVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(ShortVector.SPECIES_64 != null && ShortVector.SPECIES_128 != null, "species constants");
        check(ShortVector.SPECIES_64.elementType() == short.class, "element type short");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Short> sp = ShortVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        ShortVector a = ShortVector.broadcast(sp, (short) 9);
        ShortVector b = ShortVector.broadcast(sp, (short) 4);
        check(a.lane(0) == (short) 9, "broadcast lane");
        ShortVector sum = a.add(b);
        check(sum.lane(0) == (short) 13 && sum.lane(n - 1) == (short) 13, "add lanes");
        short[] arr = new short[n];
        sum.intoArray(arr, 0);
        check(arr[0] == (short) 13 && arr[n - 1] == (short) 13, "intoArray round-trip");
        ShortVector back = ShortVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        int reduced = a.reduceLanes(VectorOperators.ADD);
        check(reduced == 9 * n, "reduceLanes ADD");
    }


    static void testDeepen() {
        VectorSpecies<Short> sp = ShortVector.SPECIES_128;
        check(sp.length() == 8, "SPECIES_128 short length 8");
        ShortVector big = ShortVector.broadcast(sp, (short) 32000);
        ShortVector add = ShortVector.broadcast(sp, (short) 1000);
        check(big.lanewise(VectorOperators.SADD, add).lane(0) == 32767, "SADD saturate 32767");
        check(big.lanewise(VectorOperators.SADD, add).lane(0)
                == VectorMath.addSaturating((short) 32000, (short) 1000), "SADD == VectorMath.addSaturating");
        check(ShortVector.broadcast(sp, (short) -32000).lanewise(VectorOperators.SSUB, add).lane(0) == -32768, "SSUB saturate -32768");
        check(ShortVector.broadcast(sp, (short) 0x0102).lanewise(VectorOperators.REVERSE_BYTES).lane(0) == 0x0201, "REVERSE_BYTES swap");
        check(ShortVector.broadcast(sp, (short) 3).mul(ShortVector.broadcast(sp, (short) 4)).lane(0) == 12, "mul lanes");
        check(ShortVector.broadcast(sp, (short) 7).div(ShortVector.broadcast(sp, (short) 2)).lane(0) == 3, "div truncates");
        check(ShortVector.broadcast(sp, (short) 0x0F0F).lanewise(VectorOperators.XOR,
                ShortVector.broadcast(sp, (short) 0x00FF)).lane(0) == 0x0FF0, "XOR");
        check(((IntVector) ShortVector.broadcast(sp, (short) -1).convert(VectorOperators.S2I, 0)).lane(0) == -1, "S2I sign extend");
        check(((IntVector) ShortVector.broadcast(sp, (short) -1).convert(VectorOperators.ZERO_EXTEND_S2I, 0)).lane(0) == 65535, "ZERO_EXTEND_S2I");
        check(((LongVector) ShortVector.broadcast(sp, (short) -1).convert(VectorOperators.ZERO_EXTEND_S2L, 0)).lane(0) == 65535L, "ZERO_EXTEND_S2L");
        short[] vals = {3, 1, 4, 1, 5, 9, 2, 6};
        ShortVector v = ShortVector.fromArray(sp, vals, 0);
        check(v.reduceLanes(VectorOperators.MIN) == 1, "MIN reduce");
        check(v.reduceLanes(VectorOperators.MAX) == 9, "MAX reduce");
        check(v.reduceLanesToLong(VectorOperators.ADD) == 31L, "reduceLanesToLong ADD");
        VectorMask<Short> m = VectorMask.fromValues(sp, true, false, true, false, true, false, true, false);
        check(v.reduceLanes(VectorOperators.ADD, m) == 3 + 4 + 5 + 2, "masked reduceLanes ADD");
        check(v.compare(VectorOperators.EQ, ShortVector.broadcast(sp, (short) 1)).trueCount() == 2, "EQ trueCount");
        check(v.selectFrom(ShortVector.broadcast(sp, (short) 2)).lane(0) == 2
                && v.selectFrom(ShortVector.broadcast(sp, (short) 2)).lane(7) == 2, "selectFrom receiver supplies indexes");
        check(v.slice(2).lane(0) == 4, "slice(2) first lane");
        check(v.blend(ShortVector.broadcast(sp, (short) 99),
                VectorMask.fromValues(sp, true, false, false, false, false, false, false, false)).lane(0) == 99, "blend lane0");
        check(v.compress(m).lane(0) == 3 && v.compress(m).lane(3) == 2, "compress lanes");
        check(ShortVector.broadcast(sp, (short) 7).expand(m).lane(0) == 7
                && ShortVector.broadcast(sp, (short) 7).expand(m).lane(1) == 0
                && ShortVector.broadcast(sp, (short) 7).expand(m).lane(2) == 7, "expand lanes");
        check(v.lanewise(VectorOperators.SADD, ShortVector.broadcast(sp, (short) 32000)).lane(1) == 32001, "SADD below range no clamp");
        boolean threw = false;
        try { ShortVector.fromArray(sp, vals, 6); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "fromArray out of range IndexOutOfBoundsException");
        check(v.toString().equals("[3, 1, 4, 1, 5, 9, 2, 6]"), "toString exact");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
