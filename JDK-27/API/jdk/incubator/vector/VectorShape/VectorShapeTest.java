/*
 * @test
 * @summary jdk.incubator.vector.VectorShape conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main VectorShapeTest
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class VectorShapeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.VectorShape");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Enum.class.isAssignableFrom(c), "is enum");
        check(hasMethod(c, "vectorBitSize"), "vectorBitSize");
        check(hasMethod(c, "forBitSize"), "forBitSize");
        check(hasMethod(c, "forIndexBitSize"), "forIndexBitSize");
        check(hasMethod(c, "withLanes"), "withLanes");
        check(hasMethod(c, "preferredShape"), "preferredShape");
    }

    static void testBlackBox() {
        check(VectorShape.S_64_BIT.vectorBitSize() == 64, "64 bit");
        check(VectorShape.S_128_BIT.vectorBitSize() == 128, "128 bit");
        check(VectorShape.S_256_BIT.vectorBitSize() == 256, "256 bit");
        check(VectorShape.S_512_BIT.vectorBitSize() == 512, "512 bit");
        check(VectorShape.forBitSize(128) == VectorShape.S_128_BIT, "forBitSize 128");
        check(VectorShape.forBitSize(256) == VectorShape.S_256_BIT, "forBitSize 256");
        check(VectorShape.preferredShape() != null, "preferredShape non-null");
        check(VectorShape.valueOf("S_64_BIT") == VectorShape.S_64_BIT, "valueOf");
        check(VectorShape.values().length >= 5, "values at least 5");
        check(VectorShape.forIndexBitSize(Integer.SIZE, 128).vectorBitSize() == 64, "forIndexBitSize");
        VectorSpecies<Integer> sp = VectorShape.S_256_BIT.withLanes(int.class);
        check(sp.vectorBitSize() == 256 && sp.elementType() == int.class, "withLanes");
    }


    static void testDeepen() {
        check(java.util.Arrays.toString(VectorShape.values()).equals("[S_64_BIT, S_128_BIT, S_256_BIT, S_512_BIT, S_Max_BIT]"), "values exact order");
        int maxBits = VectorShape.S_Max_BIT.vectorBitSize();
        check(maxBits >= 64 && Integer.bitCount(maxBits) == 1, "S_Max_BIT power of two >= 64");
        check(VectorShape.forBitSize(64) == VectorShape.S_64_BIT
                && VectorShape.forBitSize(128) == VectorShape.S_128_BIT
                && VectorShape.forBitSize(256) == VectorShape.S_256_BIT
                && VectorShape.forBitSize(512) == VectorShape.S_512_BIT, "forBitSize exact table");
        check(VectorShape.forBitSize(maxBits) == VectorShape.S_512_BIT
                || VectorShape.forBitSize(maxBits) == VectorShape.S_Max_BIT, "forBitSize max maps to 512-class shape");
        int[] bad = {0, 32, 65, 511, 8192};
        for (int b : bad) {
            boolean iae = false;
            try { VectorShape.forBitSize(b); } catch (IllegalArgumentException e) {
                iae = e.getMessage().equals("Bad vector bit-size: " + b);
            }
            check(iae, "forBitSize(" + b + ") IAE exact message");
        }
        check(VectorShape.forIndexBitSize(32, 128) == VectorShape.S_64_BIT, "forIndexBitSize(32,128)");
        check(VectorShape.forIndexBitSize(64, 64) == VectorShape.S_64_BIT, "forIndexBitSize(64,64)");
        int pref = VectorShape.preferredShape().vectorBitSize();
        check(Integer.bitCount(pref) == 1 && pref >= 64, "preferredShape power of two");
        check(VectorShape.S_256_BIT.compareTo(VectorShape.S_128_BIT) > 0, "enum ordering by declaration");
        check(VectorShape.S_64_BIT.name().equals("S_64_BIT") && VectorShape.S_Max_BIT.toString().equals("S_Max_BIT"), "name/toString");
        VectorSpecies<Integer> sp = VectorShape.S_256_BIT.withLanes(int.class);
        check(sp.withShape(VectorShape.S_128_BIT).length() == 4, "withLanes+withShape length 4");
        check(sp.withShape(VectorShape.S_128_BIT).vectorShape() == VectorShape.S_128_BIT, "withShape result shape");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
