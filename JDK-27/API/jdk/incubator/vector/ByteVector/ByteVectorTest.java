/*
 * @test
 * @summary jdk.incubator.vector.ByteVector conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main ByteVectorTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class ByteVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.ByteVector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Vector.class.isAssignableFrom(c), "extends Vector");
        check(ByteVector.SPECIES_64 != null && ByteVector.SPECIES_128 != null, "species constants");
        check(ByteVector.SPECIES_64.elementType() == byte.class, "element type byte");
        check(hasMethod(c, "broadcast"), "broadcast");
        check(hasMethod(c, "add"), "add");
        check(hasMethod(c, "fromArray"), "fromArray");
    }

    static void testBlackBox() {
        VectorSpecies<Byte> sp = ByteVector.SPECIES_256;
        int n = sp.length();
        check(n > 0, "species length positive");
        ByteVector a = ByteVector.broadcast(sp, (byte) 5);
        ByteVector b = ByteVector.broadcast(sp, (byte) 3);
        check(a.lane(0) == (byte) 5, "broadcast lane");
        ByteVector sum = a.add(b);
        check(sum.lane(0) == (byte) 8 && sum.lane(n - 1) == (byte) 8, "add lanes");
        byte[] arr = new byte[n];
        sum.intoArray(arr, 0);
        check(arr[0] == (byte) 8 && arr[n - 1] == (byte) 8, "intoArray round-trip");
        ByteVector back = ByteVector.fromArray(sp, arr, 0);
        check(back.equals(sum), "fromArray round-trip equals");
        int reduced = a.reduceLanes(VectorOperators.ADD);
        check(reduced == (byte) (5 * n), "reduceLanes ADD");
    }


    static void testDeepen() {
        VectorSpecies<Byte> sp = ByteVector.SPECIES_128;
        int n = sp.length();
        check(n == 16, "SPECIES_128 byte length 16");
        byte[] xa = new byte[n], ya = new byte[n];
        for (int i = 0; i < n; i++) { xa[i] = (byte) (110 + i); ya[i] = (byte) (110 - i); }
        ByteVector x = ByteVector.fromArray(sp, xa, 0);
        ByteVector y = ByteVector.fromArray(sp, ya, 0);
        ByteVector sadd = x.lanewise(VectorOperators.SADD, y);
        for (int i = 0; i < n; i++) check(sadd.lane(i) == 127, "SADD saturates lane " + i);
        check(sadd.lane(0) == VectorMath.addSaturating(xa[0], ya[0]), "SADD == VectorMath.addSaturating");
        ByteVector ssub = x.lanewise(VectorOperators.SSUB, y);
        for (int i = 0; i < n; i++) check(ssub.lane(i) == (byte) (2 * i), "SSUB lane " + i);
        ByteVector suadd = x.lanewise(VectorOperators.SUADD, y);
        check(suadd.lane(0) == (byte) 220, "SUADD unsigned saturate 220");
        check(suadd.lane(0) == VectorMath.addSaturatingUnsigned(xa[0], ya[0]), "SUADD == VectorMath.addSaturatingUnsigned");
        ByteVector susub = x.lanewise(VectorOperators.SUSUB, y);
        check(susub.lane(0) == 0 && susub.lane(1) == 2, "SUSUB underflow clamps 0");
        check(x.lanewise(VectorOperators.UMIN, ByteVector.broadcast(sp, (byte) -1)).lane(0) == 110, "UMIN unsigned vs 255");
        check(x.lanewise(VectorOperators.UMAX, ByteVector.broadcast(sp, (byte) -1)).lane(0) == -1, "UMAX picks 255");
        ByteVector f0 = ByteVector.broadcast(sp, (byte) 0xF0);
        ByteVector ff = ByteVector.broadcast(sp, (byte) 0xFF);
        check(f0.lanewise(VectorOperators.AND, ff).equals(f0), "AND");
        check(f0.lanewise(VectorOperators.OR, ff).equals(ff), "OR");
        check(ff.lanewise(VectorOperators.XOR, ff).lane(0) == 0, "XOR self zero");
        check(f0.lanewise(VectorOperators.AND_NOT, ff).lane(0) == 0, "AND_NOT");
        check(f0.lanewise(VectorOperators.NOT).lane(0) == (byte) 0x0F, "NOT");
        check(ByteVector.broadcast(sp, (byte) 1).lanewise(VectorOperators.LSHL, 7).lane(0) == (byte) 0x80, "LSHL 7");
        check(ByteVector.broadcast(sp, (byte) -128).lanewise(VectorOperators.ASHR, 4).lane(0) == (byte) -8, "ASHR sign ext");
        check(ByteVector.broadcast(sp, (byte) -128).lanewise(VectorOperators.LSHR, 4).lane(0) == (byte) 8, "LSHR zero fill");
        byte[] raw = new byte[n];
        for (int i = 0; i < n; i++) raw[i] = (byte) i;
        byte[] raw20 = new byte[n + 4];
        for (int i = 0; i < raw20.length; i++) raw20[i] = (byte) i;
        ByteVector off = ByteVector.fromArray(sp, raw20, 2);
        check(off.lane(0) == 2 && off.lane(n - 1) == (byte) (n + 1), "fromArray with offset");
        byte[] outb = new byte[n + 4];
        ByteVector.fromArray(sp, raw, 0).intoArray(outb, 2);
        for (int i = 2; i < n + 2; i++) check(outb[i] == raw[i - 2], "intoArray with offset lane " + i);
        check(ByteVector.fromArray(sp, raw, 0).lanewise(VectorOperators.REVERSE_BYTES).lane(0) == 0, "REVERSE_BYTES identity pair");
        byte[] pairBytes = new byte[ByteVector.SPECIES_64.length()];
        pairBytes[0] = 2; pairBytes[1] = 1;
        ByteVector pair = ByteVector.fromArray(ByteVector.SPECIES_64, pairBytes, 0);
        ShortVector sv = (ShortVector) pair.reinterpretShape(ShortVector.SPECIES_64, 0);
        check(sv.lane(0) == 0x0102, "reinterpret little-endian 0x0102");
        check(((ByteVector) sv.reinterpretShape(ByteVector.SPECIES_64, 0)).equals(pair), "reinterpret round-trip");
        VectorMask<Byte> m = VectorMask.fromValues(sp, false, true, false, true, false, true, false, true,
                false, true, false, true, false, true, false, true);
        ByteVector ml = ByteVector.fromArray(sp, raw, 0, m);
        check(ml.lane(0) == 0 && ml.lane(1) == 1 && ml.lane(2) == 0, "masked fromArray zeroes unset lanes");
        byte[] dstb = new byte[n];
        ByteVector.broadcast(sp, (byte) 9).intoArray(dstb, 0, m);
        check(dstb[1] == 9 && dstb[0] == 0 && dstb[2] == 0, "masked intoArray writes only set lanes");
        ByteVector cv = ByteVector.fromArray(ByteVector.SPECIES_64, new byte[] {1, 2, 3, 4, 5, 6, 7, 8}, 0);
        VectorMask<Byte> cm = VectorMask.fromValues(ByteVector.SPECIES_64, false, true, false, true, false, true, false, true);
        check(cv.compress(cm).lane(0) == 2 && cv.compress(cm).lane(3) == 8 && cv.compress(cm).lane(4) == 0, "compress lanes");
        ByteVector ev = ByteVector.broadcast(ByteVector.SPECIES_64, (byte) 7);
        check(ev.expand(cm).lane(0) == 0 && ev.expand(cm).lane(1) == 7 && ev.expand(cm).lane(3) == 7, "expand lanes");
        ByteVector fnz = ByteVector.fromArray(ByteVector.SPECIES_64, new byte[] {0, 0, 5, 0, 0, 0, 0, 0}, 0);
        check(fnz.reduceLanes(VectorOperators.FIRST_NONZERO) == 5, "FIRST_NONZERO 5");
        check(ByteVector.zero(ByteVector.SPECIES_64).reduceLanes(VectorOperators.FIRST_NONZERO) == 0, "FIRST_NONZERO all zero");
        check(cv.reduceLanes(VectorOperators.AND) == 0, "AND reduce of 1..8");
        check(ByteVector.broadcast(ByteVector.SPECIES_64, (byte) 3).reduceLanes(VectorOperators.XOR) == 0, "XOR reduce even count");
        check(cv.reduceLanes(VectorOperators.OR) == 15, "OR reduce 1|..|8");
        check(cv.reduceLanes(VectorOperators.MIN) == 1 && cv.reduceLanes(VectorOperators.MAX) == 8, "MIN/MAX reduce");
        check(cv.compare(VectorOperators.EQ, cv).allTrue(), "EQ self allTrue");
        check(ByteVector.broadcast(ByteVector.SPECIES_64, (byte) -1).compare(VectorOperators.LT,
                ByteVector.broadcast(ByteVector.SPECIES_64, (byte) 1)).allTrue(), "LT signed -1<1");
        check(!ByteVector.broadcast(ByteVector.SPECIES_64, (byte) -1).compare(VectorOperators.ULT,
                ByteVector.broadcast(ByteVector.SPECIES_64, (byte) 1)).anyTrue(), "ULT 255 unsigned not <1");
        check(ByteVector.broadcast(ByteVector.SPECIES_64, (byte) -1).compare(VectorOperators.UGT,
                ByteVector.broadcast(ByteVector.SPECIES_64, (byte) 1)).allTrue(), "UGT 255 unsigned");
        check(ByteVector.broadcast(sp, (byte) 7).withLane(0, (byte) 42).lane(0) == 42, "withLane");
        boolean threw = false;
        try { ByteVector.broadcast(sp, (byte) 7).lane(n + 1); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "lane out of range throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
