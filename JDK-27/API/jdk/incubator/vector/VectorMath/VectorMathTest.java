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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
