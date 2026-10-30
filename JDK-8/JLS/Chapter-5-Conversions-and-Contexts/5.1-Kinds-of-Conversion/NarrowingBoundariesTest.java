/*
 * @test
 * @summary JLS 5.1.3/5.1.4 (Java SE 8): narrowing primitive conversions at the
 *          value-set boundaries, evaluated at run time (operands passed through
 *          method parameters so javac cannot constant-fold the casts).  Integral
 *          narrowing drops all but the n lowest-order bits; floating-point to
 *          integral rounds toward zero, clamps out-of-range values to the
 *          target's min/max, and maps NaN to 0; double->float overflows to
 *          Infinity and underflows to zero.
 * @run main NarrowingBoundariesTest
 */

public class NarrowingBoundariesTest {
    static byte   toByte(int x)        { return (byte) x; }
    static short  toShort(int x)       { return (short) x; }
    static char   toChar(int x)        { return (char) x; }
    static int    longToInt(long x)    { return (int) x; }
    static int    floatToInt(float x)  { return (int) x; }
    static long   doubleToLong(double x) { return (long) x; }
    static float  doubleToFloat(double x) { return (float) x; }
    static int    doubleToInt(double x)   { return (int) x; }
    static char   byteToChar(byte x)   { return (char) x; }

    public static void main(String[] args) {
        // int -> byte: low 8 bits, two's complement
        if (toByte(127) != 127)   throw new AssertionError("byte 127");
        if (toByte(128) != -128)  throw new AssertionError("byte 128");
        if (toByte(255) != -1)    throw new AssertionError("byte 255");
        if (toByte(256) != 0)     throw new AssertionError("byte 256");
        if (toByte(-129) != 127)  throw new AssertionError("byte -129");
        if (toByte(-128) != -128) throw new AssertionError("byte -128");

        // int -> short: low 16 bits
        if (toShort(32767) != 32767)   throw new AssertionError("short 32767");
        if (toShort(32768) != -32768)  throw new AssertionError("short 32768");
        if (toShort(65535) != -1)      throw new AssertionError("short 65535");
        if (toShort(65536) != 0)       throw new AssertionError("short 65536");
        if (toShort(-32769) != 32767)  throw new AssertionError("short -32769");

        // int -> char: low 16 bits, unsigned
        if (toChar(65535) != 65535) throw new AssertionError("char 65535");
        if (toChar(65536) != 0)     throw new AssertionError("char 65536");
        if (toChar(-1) != 65535)    throw new AssertionError("char -1");

        // long -> int: low 32 bits
        if (longToInt(2147483648L) != Integer.MIN_VALUE) throw new AssertionError("int 2^31");
        if (longToInt(-2147483649L) != Integer.MAX_VALUE) throw new AssertionError("int -2^31-1");
        if (longToInt(4294967296L) != 0) throw new AssertionError("int 2^32");

        // float -> int: round toward zero, clamp, NaN -> 0
        if (floatToInt(3.99f) != 3)   throw new AssertionError("float 3.99");
        if (floatToInt(-3.99f) != -3) throw new AssertionError("float -3.99");
        if (floatToInt(Float.POSITIVE_INFINITY) != Integer.MAX_VALUE) throw new AssertionError("float +Inf");
        if (floatToInt(Float.NEGATIVE_INFINITY) != Integer.MIN_VALUE) throw new AssertionError("float -Inf");
        if (floatToInt(Float.NaN) != 0) throw new AssertionError("float NaN");
        if (floatToInt(2147483648.0f) != Integer.MAX_VALUE) throw new AssertionError("float 2^31 clamp");
        if (floatToInt(-2147483649.0f) != Integer.MIN_VALUE) throw new AssertionError("float -2^31-1 clamp");

        // double -> long
        if (doubleToLong(3.99) != 3L)   throw new AssertionError("double 3.99");
        if (doubleToLong(-3.99) != -3L) throw new AssertionError("double -3.99");
        if (doubleToLong(Double.POSITIVE_INFINITY) != Long.MAX_VALUE) throw new AssertionError("double +Inf");
        if (doubleToLong(Double.NEGATIVE_INFINITY) != Long.MIN_VALUE) throw new AssertionError("double -Inf");
        if (doubleToLong(Double.NaN) != 0L) throw new AssertionError("double NaN");
        if (doubleToLong(9.223372036854776E18) != Long.MAX_VALUE) throw new AssertionError("double 2^63 clamp");

        // double -> float: overflow to Infinity, underflow to zero
        if (doubleToFloat(Double.MAX_VALUE) != Float.POSITIVE_INFINITY) throw new AssertionError("d2f overflow");
        if (doubleToFloat(Double.MIN_VALUE) != 0.0f) throw new AssertionError("d2f underflow");
        if (doubleToFloat(1.0e39) != Float.POSITIVE_INFINITY) throw new AssertionError("d2f 1e39");
        if (doubleToFloat(Double.POSITIVE_INFINITY) != Float.POSITIVE_INFINITY) throw new AssertionError("d2f +Inf");

        // double -> int: clamp
        if (doubleToInt(1e20) != Integer.MAX_VALUE) throw new AssertionError("d2i 1e20");
        if (doubleToInt(-1e20) != Integer.MIN_VALUE) throw new AssertionError("d2i -1e20");

        // byte -> char: widening byte->int then narrowing int->char (JLS 5.1.4)
        if (byteToChar((byte) -1) != 65535) throw new AssertionError("byte -1 -> char");
        if (byteToChar((byte) 200) != 65480) throw new AssertionError("byte 200 -> char");

        System.out.println("PASS");
    }
}
