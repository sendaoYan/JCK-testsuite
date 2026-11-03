/*
 * @test
 * @summary java.lang.runtime.ExactConversionsSupport conformance: white-box structure + black-box behavior.
 * @run main ExactConversionsSupportTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.runtime.ExactConversionsSupport;

public class ExactConversionsSupportTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.runtime.ExactConversionsSupport");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getDeclaredConstructor().getModifiers()) == false,
              "no public constructor");
        int predicates = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().startsWith("is") && m.getReturnType() == boolean.class
                && Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) {
                predicates++;
            }
        }
        check(predicates == 21, "21 public static is*Exact predicates, got " + predicates);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // int -> narrower exactness
        check(ExactConversionsSupport.isIntToByteExact(0), "0 -> byte exact");
        check(ExactConversionsSupport.isIntToByteExact(127), "127 -> byte exact");
        check(ExactConversionsSupport.isIntToByteExact(-128), "-128 -> byte exact");
        check(!ExactConversionsSupport.isIntToByteExact(128), "128 -> byte lossy");
        check(!ExactConversionsSupport.isIntToByteExact(-129), "-129 -> byte lossy");
        check(ExactConversionsSupport.isIntToShortExact(32767), "32767 -> short exact");
        check(!ExactConversionsSupport.isIntToShortExact(32768), "32768 -> short lossy");
        check(ExactConversionsSupport.isIntToCharExact(0), "0 -> char exact");
        check(ExactConversionsSupport.isIntToCharExact(65535), "65535 -> char exact");
        check(!ExactConversionsSupport.isIntToCharExact(-1), "-1 -> char lossy");
        check(ExactConversionsSupport.isIntToCharExact('A'), "'A' -> char exact");
        // int -> float may lose precision (24-bit mantissa)
        check(ExactConversionsSupport.isIntToFloatExact(1 << 24), "2^24 -> float exact");
        check(!ExactConversionsSupport.isIntToFloatExact((1 << 24) + 1), "2^24+1 -> float lossy");
        check(ExactConversionsSupport.isIntToFloatExact(16777216), "16777216 exact");
        check(!ExactConversionsSupport.isIntToFloatExact(16777217), "16777217 lossy");

        // long -> others
        check(ExactConversionsSupport.isLongToByteExact(100L), "100L -> byte exact");
        check(ExactConversionsSupport.isLongToShortExact(-32768L), "-32768L -> short exact");
        check(!ExactConversionsSupport.isLongToShortExact(32768L), "32768L -> short lossy");
        check(ExactConversionsSupport.isLongToCharExact(65535L), "65535L -> char exact");
        check(ExactConversionsSupport.isLongToIntExact(0x7FFFFFFFL), "Long.MAX_INT -> int exact");
        check(!ExactConversionsSupport.isLongToIntExact(0x7FFFFFFF00000000L), "big long -> int lossy");
        check(ExactConversionsSupport.isLongToFloatExact(0L), "0L -> float exact");
        check(ExactConversionsSupport.isLongToDoubleExact(1L << 53), "2^53 -> double exact");
        check(!ExactConversionsSupport.isLongToDoubleExact((1L << 53) + 1), "2^53+1 -> double lossy");
        check(ExactConversionsSupport.isLongToDoubleExact(Long.MAX_VALUE) == false,
              "Long.MAX_VALUE -> double lossy");

        // float -> integral types: truncation before range check; negative zero excluded
        check(ExactConversionsSupport.isFloatToByteExact(100.0f), "100.0f -> byte exact");
        check(!ExactConversionsSupport.isFloatToByteExact(-0.0f), "-0.0f -> byte lossy (negative zero)");
        check(!ExactConversionsSupport.isFloatToByteExact(100.5f), "100.5f -> byte lossy");
        check(!ExactConversionsSupport.isFloatToByteExact(1000.0f), "1000.0f out of byte");
        check(ExactConversionsSupport.isFloatToShortExact(-32768.0f), "-32768.0f -> short exact");
        check(ExactConversionsSupport.isFloatToCharExact(65535.0f), "65535.0f -> char exact");
        check(ExactConversionsSupport.isFloatToIntExact(2.0e9f), "2e9f -> int exact");
        check(!ExactConversionsSupport.isFloatToIntExact(3e9f), "3e9f out of int");
        check(ExactConversionsSupport.isFloatToIntExact(2.0f), "2.0f -> int exact");
        check(ExactConversionsSupport.isFloatToLongExact(9.007199254740992e15f), "2^53 float -> long exact");
        check(ExactConversionsSupport.isFloatToIntExact(0.5f) == false, "0.5f -> int lossy");
        // NaN / infinity are never exact for integral targets
        check(!ExactConversionsSupport.isFloatToIntExact(Float.NaN), "NaN -> int lossy");
        check(!ExactConversionsSupport.isFloatToLongExact(Float.POSITIVE_INFINITY), "+inf -> long lossy");

        // double -> others
        check(ExactConversionsSupport.isDoubleToByteExact(127.0), "127.0 -> byte exact");
        check(!ExactConversionsSupport.isDoubleToByteExact(127.5), "127.5 -> byte lossy");
        check(ExactConversionsSupport.isDoubleToShortExact(-32768.0), "-32768.0 -> short exact");
        check(ExactConversionsSupport.isDoubleToCharExact(0.0), "0.0 -> char exact");
        check(!ExactConversionsSupport.isDoubleToCharExact(65536.0), "65536.0 out of char");
        check(ExactConversionsSupport.isDoubleToIntExact(Integer.MAX_VALUE), "int max double -> int exact");
        // (double)Long.MAX_VALUE rounds to 0x1p63 which the predicate explicitly excludes
        check(!ExactConversionsSupport.isDoubleToLongExact(Long.MAX_VALUE),
              "Long.MAX_VALUE double rounds to 2^63 -> lossy");
        check(ExactConversionsSupport.isDoubleToLongExact(9007199254740992.0), "2^53 -> long exact");
        check(!ExactConversionsSupport.isDoubleToLongExact(9.223372036854776e18), "2^63 excluded");
        check(!ExactConversionsSupport.isDoubleToLongExact(1e19), "1e19 out of long");
        check(ExactConversionsSupport.isDoubleToFloatExact(0.5), "0.5 -> float exact");
        check(!ExactConversionsSupport.isDoubleToFloatExact(0.1), "0.1 -> float lossy");
        check(ExactConversionsSupport.isDoubleToFloatExact(1.0), "1.0 -> float exact");
        check(!ExactConversionsSupport.isDoubleToIntExact(Double.NEGATIVE_INFINITY), "-inf -> int lossy");
        // NaN is representable as float, and the predicate treats NaN as exact (n != n)
        check(ExactConversionsSupport.isDoubleToFloatExact(Double.NaN), "NaN -> float exact per predicate");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
