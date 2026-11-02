/*
 * @test
 * @summary java.lang.runtime.ExactConversionsSupport conformance: the exactness
 *          predicates at their documented boundaries.
 * @run main ExactConversionsSupportTest
 */

import java.lang.runtime.ExactConversionsSupport;

public class ExactConversionsSupportTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(!ExactConversionsSupport.class.isInterface(),
              "ExactConversionsSupport is a final class");

        // int -> byte
        check(ExactConversionsSupport.isIntToByteExact(127), "isIntToByteExact(127)");
        check(ExactConversionsSupport.isIntToByteExact(-128), "isIntToByteExact(-128)");
        check(!ExactConversionsSupport.isIntToByteExact(128), "!isIntToByteExact(128)");
        check(!ExactConversionsSupport.isIntToByteExact(-129), "!isIntToByteExact(-129)");

        // int -> short
        check(ExactConversionsSupport.isIntToShortExact(32767), "isIntToShortExact(32767)");
        check(!ExactConversionsSupport.isIntToShortExact(32768), "!isIntToShortExact(32768)");

        // int -> char
        check(ExactConversionsSupport.isIntToCharExact(65535), "isIntToCharExact(65535)");
        check(ExactConversionsSupport.isIntToCharExact(0), "isIntToCharExact(0)");
        check(!ExactConversionsSupport.isIntToCharExact(65536), "!isIntToCharExact(65536)");
        check(!ExactConversionsSupport.isIntToCharExact(-1), "!isIntToCharExact(-1)");

        // int -> float (24-bit mantissa)
        check(ExactConversionsSupport.isIntToFloatExact(16777216), "isIntToFloatExact(2^24)");
        check(!ExactConversionsSupport.isIntToFloatExact(16777217), "!isIntToFloatExact(2^24+1)");

        // long -> int
        check(ExactConversionsSupport.isLongToIntExact(2147483647L), "isLongToIntExact(MAX)");
        check(!ExactConversionsSupport.isLongToIntExact(2147483648L), "!isLongToIntExact(MAX+1)");

        // long -> double (53-bit mantissa)
        check(ExactConversionsSupport.isLongToDoubleExact(9007199254740992L),
              "isLongToDoubleExact(2^53)");
        check(!ExactConversionsSupport.isLongToDoubleExact(9007199254740993L),
              "!isLongToDoubleExact(2^53+1)");

        // float -> int
        check(ExactConversionsSupport.isFloatToIntExact(42.0f), "isFloatToIntExact(42.0f)");
        check(!ExactConversionsSupport.isFloatToIntExact(42.5f), "!isFloatToIntExact(42.5f)");
        check(!ExactConversionsSupport.isFloatToIntExact(Float.NaN),
              "!isFloatToIntExact(NaN)");
        check(!ExactConversionsSupport.isFloatToIntExact(Float.POSITIVE_INFINITY),
              "!isFloatToIntExact(Infinity)");

        // double -> long
        check(ExactConversionsSupport.isDoubleToLongExact(9007199254740992.0),
              "isDoubleToLongExact(2^53)");
        check(ExactConversionsSupport.isDoubleToLongExact(42.0), "isDoubleToLongExact(42.0)");
        check(!ExactConversionsSupport.isDoubleToLongExact(42.5),
              "!isDoubleToLongExact(42.5 fractional)");
        check(!ExactConversionsSupport.isDoubleToLongExact(1e20),
              "!isDoubleToLongExact(1e20 out of range)");
        check(!ExactConversionsSupport.isDoubleToLongExact(Double.NaN),
              "!isDoubleToLongExact(NaN)");

        // double -> float
        check(ExactConversionsSupport.isDoubleToFloatExact(16777216.0),
              "isDoubleToFloatExact(2^24)");
        check(!ExactConversionsSupport.isDoubleToFloatExact(16777217.0),
              "!isDoubleToFloatExact(2^24+1)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
