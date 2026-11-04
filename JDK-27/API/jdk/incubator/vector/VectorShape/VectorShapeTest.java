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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
