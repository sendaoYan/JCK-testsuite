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


    static void testDeepen() {
        check(Float16.MAX_VALUE.floatValue() == 65504.0f, "MAX_VALUE 65504");
        check(Float16.MIN_VALUE.floatValue() == 5.9604645E-8f, "MIN_VALUE subnormal");
        check(Float16.MIN_NORMAL.floatValue() == 6.1035156E-5f, "MIN_NORMAL");
        check(Float16.PRECISION == 11, "PRECISION 11");
        check(Float16.MAX_EXPONENT == 15 && Float16.MIN_EXPONENT == -14, "exponent range");
        check(Float16.getExponent(Float16.valueOf(1024.0f)) == 10, "getExponent 1024");
        check(Float16.getExponent(Float16.valueOf(0.5f)) == -1, "getExponent 0.5");
        check(Float16.toHexString(Float16.valueOf(1.5f)).equals("0x1.8p0"), "toHexString 1.5");
        check(Float16.toHexString(Float16.NaN).equals("NaN"), "toHexString NaN");
        check(Float16.toHexString(Float16.valueOf(0.0f)).equals("0x0.0p0"), "toHexString zero");
        check(Float16.toHexString(Float16.valueOf(-0.0f)).equals("-0x0.0p0"), "toHexString -0");
        check(Float16.toHexString(Float16.MAX_VALUE).equals("0x1.ffcp15"), "toHexString MAX");
        check(Float16.toHexString(Float16.MIN_VALUE).equals("0x0.004p-14"), "toHexString MIN subnormal");
        check(Float16.toString(Float16.NaN).equals("NaN"), "toString NaN");
        check(Float16.toString(Float16.POSITIVE_INFINITY).equals("Infinity"), "toString Infinity");
        check(Float16.toString(Float16.valueOf(0.0f)).equals("0.0"), "toString 0.0");
        check(Float16.toString(Float16.valueOf(-0.0f)).equals("-0.0"), "toString -0.0");
        check(Float16.toString(Float16.valueOf(0.1f)).equals("0.1"), "shortest repr of half 0.1");
        check(Float16.valueOf(0.1f).floatValue() == 0.099975586f, "0.1f rounds to half 0.099975586");
        check(Float16.valueOf(0.1).floatValue() == 0.099975586f, "double overload same rounding");
        check(Float16.valueOf(65519.0f).floatValue() == 65504.0f, "65519 rounds to MAX not inf");
        check(Float16.valueOf(65505.0f).floatValue() == 65504.0f, "65505 saturates to MAX");
        check(Float16.MAX_VALUE.compareTo(Float16.POSITIVE_INFINITY) < 0, "MAX < Infinity");
        check(Float16.compare(Float16.NaN, Float16.NaN) == 0, "compare NaN==NaN");
        check(Float16.compare(Float16.NaN, Float16.MAX_VALUE) > 0, "NaN greatest");
        check(Float16.compare(Float16.valueOf(-0.0f), Float16.valueOf(0.0f)) < 0, "compare -0 < +0");
        check(Float16.compare(Float16.valueOf(0.0f), Float16.valueOf(-0.0f)) > 0, "compare +0 > -0");
        check(!Float16.valueOf(-0.0f).equals(Float16.valueOf(0.0f)), "equals distinguishes -0 and +0");
        check(Integer.toHexString(Short.toUnsignedInt(Float16.float16ToShortBits(Float16.valueOf(1.0f)))).equals("3c00"), "bits 1.0 = 0x3c00");
        check(Integer.toHexString(Short.toUnsignedInt(Float16.float16ToShortBits(Float16.NaN))).equals("7e00"), "canonical NaN 0x7e00");
        check(Integer.toHexString(Short.toUnsignedInt(Float16.float16ToShortBits(Float16.valueOf(-0.0f)))).equals("8000"), "bits -0 = 0x8000");
        check(Float16.valueOf(257.0f).byteValue() == 1, "byteValue truncates");
        check(Float16.valueOf(257.0f).shortValue() == 257, "shortValue");
        check(Float16.valueOf(257.0f).longValue() == 257L, "longValue");
        check(Float16.valueOf("65504").floatValue() == 65504.0f, "valueOf String");
        boolean nfe = false;
        try { Float16.valueOf("bogus"); } catch (NumberFormatException e) { nfe = e.getMessage().equals("For input string: \"bogus\""); }
        check(nfe, "valueOf bogus NumberFormatException exact message");
        // exhaustive 16-bit round-trip: non-NaN all fixed; NaN all map to canonical
        int fixed = 0, nanPatterns = 0;
        for (int bits = 0; bits < 65536; bits++) {
            Float16 f = Float16.shortBitsToFloat16((short) bits);
            if (Float16.float16ToShortBits(f) == (short) bits) fixed++;
            boolean isNanPattern = (bits & 0x7C00) == 0x7C00 && (bits & 0x03FF) != 0;
            if (isNanPattern) { nanPatterns++; check(Float16.isNaN(f), "NaN pattern 0x" + Integer.toHexString(bits)); }
        }
        check(fixed == 63491, "exhaustive round-trip fixed count 63491");
        check(nanPatterns == 2046, "NaN bit patterns 2046");
        check(Float16.float16ToShortBits(Float16.shortBitsToFloat16((short) 0xFE00)) == 0x7E00, "negative NaN canonicalized positive");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
