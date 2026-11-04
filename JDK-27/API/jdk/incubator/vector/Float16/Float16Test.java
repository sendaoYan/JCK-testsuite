/*
 * @test
 * @summary jdk.incubator.vector.Float16 conformance: white-box structure + black-box behavior.
 * @modules jdk.incubator.vector
 * @run main Float16Test
 */

import jdk.incubator.vector.*;
import java.lang.reflect.*;

public class Float16Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.incubator.vector.Float16");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Number.class.isAssignableFrom(c), "extends Number");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        check(Float16.SIZE == 16, "SIZE 16");
        check(Float16.BYTES == 2, "BYTES 2");
        check(hasMethod(c, "valueOf"), "valueOf");
        check(hasMethod(c, "floatValue"), "floatValue");
        check(hasMethod(c, "doubleValue"), "doubleValue");
        check(hasMethod(c, "compareTo"), "compareTo");
    }

    static void testBlackBox() {
        Float16 one = Float16.valueOf(1.0f);
        Float16 two = Float16.valueOf(2.0f);
        check(one.floatValue() == 1.0f, "floatValue");
        check(one.doubleValue() == 1.0, "doubleValue");
        check(one.intValue() == 1, "intValue");
        check(Float16.isFinite(one), "isFinite true");
        check(!Float16.isNaN(one), "isNaN false");
        check(!Float16.isInfinite(one), "isInfinite false");
        check(Float16.isNaN(Float16.NaN), "NaN constant isNaN");
        check(Float16.isInfinite(Float16.POSITIVE_INFINITY), "POSITIVE_INFINITY isInfinite");
        check(Float16.valueOf("1.5").floatValue() == 1.5f, "valueOf String");
        check(one.compareTo(two) < 0, "compareTo ordering");
        check(Float16.compare(one, two) < 0, "static compare");
        check(Float16.add(one, two).floatValue() == 3.0f, "add");
        check(Float16.subtract(two, one).floatValue() == 1.0f, "subtract");
        check(Float16.multiply(two, Float16.valueOf(3.0f)).floatValue() == 6.0f, "multiply");
        check(Float16.divide(Float16.valueOf(6.0f), two).floatValue() == 3.0f, "divide");
        check(Float16.max(one, two).floatValue() == 2.0f, "max");
        check(Float16.min(one, two).floatValue() == 1.0f, "min");
        check(Float16.abs(Float16.valueOf(-2.0f)).floatValue() == 2.0f, "abs");
        check(Float16.negate(one).floatValue() == -1.0f, "negate");
        check(one.equals(Float16.valueOf(1.0f)), "equals");
        check(one.hashCode() == Float16.hashCode(Float16.valueOf(1.0f)), "hashCode consistent");
        check(Float16.shortBitsToFloat16(Float16.float16ToShortBits(one)).equals(one), "bits round-trip");
        check(Float16.toString(one).length() > 0, "toString non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
