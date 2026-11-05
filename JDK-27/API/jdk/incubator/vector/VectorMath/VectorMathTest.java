/*
 * @test
 * @summary jdk.incubator.vector.VectorMath conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main VectorMathTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class VectorMathTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorMath");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(hasMethod(c, "addSaturating"), "addSaturating");
        check(hasMethod(c, "subSaturating"), "subSaturating");
        check(hasMethod(c, "minUnsigned"), "minUnsigned");
        check(hasMethod(c, "maxUnsigned"), "maxUnsigned");
    }

    static void testBlackBox() {
        check(VectorMath.addSaturating(Integer.MAX_VALUE, 1) == Integer.MAX_VALUE, "addSaturating int clamps");
        check(VectorMath.addSaturating(1, 2) == 3, "addSaturating int normal");
        check(VectorMath.subSaturating(Integer.MIN_VALUE, 1) == Integer.MIN_VALUE, "subSaturating int clamps");
        check(VectorMath.subSaturating(5, 3) == 2, "subSaturating int normal");
        check(VectorMath.addSaturating((byte) 120, (byte) 20) == (byte) 127, "addSaturating byte clamps");
        check(VectorMath.subSaturating((byte) -128, (byte) 1) == (byte) -128, "subSaturating byte clamps");
        check(VectorMath.minUnsigned(0, -1) == 0, "minUnsigned int treats as unsigned");
        check(VectorMath.maxUnsigned(0, -1) == -1, "maxUnsigned int");
        check(VectorMath.minUnsigned(0L, -1L) == 0L, "minUnsigned long");
        check(VectorMath.maxUnsigned(0L, -1L) == -1L, "maxUnsigned long");
    }


    static void testDeepen() {
        // signed saturating add/sub edges for all four types
        check(VectorMath.addSaturating(Byte.MAX_VALUE, (byte) 1) == Byte.MAX_VALUE, "addSat byte MAX");
        check(VectorMath.addSaturating(Byte.MIN_VALUE, (byte) -1) == Byte.MIN_VALUE, "addSat byte MIN");
        check(VectorMath.addSaturating(Short.MAX_VALUE, (short) 1) == Short.MAX_VALUE, "addSat short MAX");
        check(VectorMath.addSaturating(Short.MIN_VALUE, (short) -1) == Short.MIN_VALUE, "addSat short MIN");
        check(VectorMath.addSaturating(Integer.MAX_VALUE, -1) == Integer.MAX_VALUE - 1, "addSat int no clamp");
        check(VectorMath.addSaturating(Long.MAX_VALUE, 1L) == Long.MAX_VALUE, "addSat long MAX");
        check(VectorMath.addSaturating(Long.MIN_VALUE, -1L) == Long.MIN_VALUE, "addSat long MIN");
        check(VectorMath.subSaturating(Short.MIN_VALUE, (short) 1000) == -32768, "subSat short clamp");
        check(VectorMath.subSaturating(Long.MIN_VALUE, 1L) == Long.MIN_VALUE, "subSat long clamp");
        check(VectorMath.subSaturating((short) 5, (short) 3) == 2, "subSat normal");
        // unsigned saturating add/sub for all four types
        check(VectorMath.addSaturatingUnsigned((byte) -1, (byte) 1) == (byte) -1, "addSatU byte max stays");
        check(VectorMath.addSaturatingUnsigned((short) -1, (short) 2) == (short) -1, "addSatU short max stays");
        check(VectorMath.addSaturatingUnsigned(-1, 5) == -1, "addSatU int max stays");
        check(VectorMath.addSaturatingUnsigned(-1L, 2L) == -1L, "addSatU long max stays");
        check(VectorMath.addSaturatingUnsigned((byte) 3, (byte) 4) == 7, "addSatU normal");
        check(VectorMath.subSaturatingUnsigned((byte) 5, (byte) 6) == 0, "subSatU byte clamps 0");
        check(VectorMath.subSaturatingUnsigned((short) 5, (short) 6) == 0, "subSatU short clamps 0");
        check(VectorMath.subSaturatingUnsigned(0, 5) == 0, "subSatU int clamps 0");
        check(VectorMath.subSaturatingUnsigned(5L, 6L) == 0L, "subSatU long clamps 0");
        check(VectorMath.subSaturatingUnsigned(9, 4) == 5, "subSatU normal");
        // unsigned min/max for all four types
        check(VectorMath.minUnsigned((byte) -1, (byte) 7) == 7, "minU byte 255 vs 7");
        check(VectorMath.maxUnsigned((byte) -1, (byte) 7) == -1, "maxU byte picks 255");
        check(VectorMath.minUnsigned((short) -1, (short) 0) == 0, "minU short 0");
        check(VectorMath.maxUnsigned((short) -1, (short) 3) == -1, "maxU short 65535");
        check(VectorMath.minUnsigned(0, -1) == 0 && VectorMath.maxUnsigned(0, -1) == -1, "minU/maxU int");
        check(VectorMath.minUnsigned(0L, -1L) == 0L && VectorMath.maxUnsigned(0L, -1L) == -1L, "minU/maxU long");
        // cross-check with lanewise vector semantics
        byte[] xa = new byte[ByteVector.SPECIES_64.length()], ya = new byte[ByteVector.SPECIES_64.length()];
        xa[0] = 120; ya[0] = 20;
        ByteVector v = ByteVector.fromArray(ByteVector.SPECIES_64, xa, 0);
        ByteVector w = ByteVector.fromArray(ByteVector.SPECIES_64, ya, 0);
        check(v.lanewise(VectorOperators.SADD, w).lane(0) == VectorMath.addSaturating((byte) 120, (byte) 20), "SADD agrees with VectorMath");
        check(v.lanewise(VectorOperators.SUADD, w).lane(0) == VectorMath.addSaturatingUnsigned((byte) 120, (byte) 20), "SUADD agrees");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
