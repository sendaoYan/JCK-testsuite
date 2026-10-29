/*
 * @test
 * @summary JLS 5.1.7: the nine boxing conversions (boolean/byte/short/char/int/
 *          long/float/double -> their wrapper types, and null -> null). Values
 *          in the cache range (boolean, \u0000..\u007f, -128..127) always box
 *          to the same reference (a == b).
 * @run main BoxingConversion
 */

public class BoxingConversion {
    public static void main(String[] args) {
        Boolean bn = true;
        Byte by = (byte) 1;
        Short sh = (short) 2;
        Character ch = 'c';
        Integer in = 3;
        Long ln = 4L;
        Float fl = 5.0f;
        Double db = 6.0;

        if (!bn || by != 1 || sh != 2 || ch != 'c' || in != 3 || ln != 4L) {
            throw new AssertionError("boxing conversions");
        }
        if (fl != 5.0f || db != 6.0) throw new AssertionError("float/double boxing");

        // Boxing produces a reference with the corresponding value.
        if (!in.equals(3) || in.intValue() != 3) throw new AssertionError("Integer box");

        // Cached boxed values are identical (a == b) for -128..127 and chars <= 0x7f.
        Integer a1 = 100;
        Integer a2 = 100;
        if (a1 != a2) throw new AssertionError("cached Integer must be identical");
        Character c1 = 'A';
        Character c2 = 'A';
        if (c1 != c2) throw new AssertionError("cached Character must be identical");
        Boolean b1 = true;
        Boolean b2 = true;
        if (b1 != b2) throw new AssertionError("cached Boolean must be identical");

        // Boxing a NaN yields a Float/Double whose isNaN() is true.
        Double nan = Double.NaN;
        if (!nan.isNaN()) throw new AssertionError("boxed NaN must be NaN");

        System.out.println("PASS");
    }
}
