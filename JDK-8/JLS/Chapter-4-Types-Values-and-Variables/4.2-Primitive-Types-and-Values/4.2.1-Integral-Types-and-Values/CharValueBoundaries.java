/*
 * @test
 * @summary JLS 4.2.1 (Java SE 8): char is an unsigned 16-bit integral type with
 *          values 0..65535.  char+char promotes to int, char++ wraps modulo
 *          65536, comparisons are unsigned, and surrogate code points are valid
 *          char values.  Operands route through method parameters so javac
 *          cannot constant-fold the arithmetic.
 * @run main CharValueBoundaries
 */

public class CharValueBoundaries {
    static int     charPlusInt(char a, int b) { return a + b; }   // char promotes to int
    static char    charCast(int x)            { return (char) x; } // narrowing wraps
    static char    incr(char c)               { c++; return c; }
    static char    decr(char c)               { c--; return c; }
    static boolean charGt(char a, char b)     { return a > b; }    // unsigned comparison
    static boolean charLt(char a, char b)     { return a < b; }

    public static void main(String[] args) {
        // unsigned 16-bit range: 0..65535.
        if ((int) Character.MIN_VALUE != 0) throw new AssertionError("char min != 0");
        if ((int) Character.MAX_VALUE != 65535) throw new AssertionError("char max != 65535");
        if ('\u0000' != 0) throw new AssertionError("\\u0000 literal != 0");
        if ('\uffff' != 65535) throw new AssertionError("\\uffff literal != 65535");
        if ('\uFFFF' != 65535) throw new AssertionError("\\uFFFF literal != 65535");

        // char promotes to int under binary numeric promotion: 65535 + 1 == 65536
        // (an int), no wrap.
        if (charPlusInt((char) 65535, 1) != 65536) throw new AssertionError("char+int promotes to int");

        // narrowing int -> char wraps modulo 65536 (JLS 5.1.4).
        if (charCast(-1) != 65535) throw new AssertionError("(char)-1 != 65535");
        if (charCast(65536) != 0) throw new AssertionError("(char)65536 != 0");
        if (charCast(131071) != 65535) throw new AssertionError("(char)131071 != 65535");

        // char++ / char-- wrap modulo 65536.
        if (incr((char) 65535) != 0) throw new AssertionError("char++ must wrap MAX -> 0");
        if (decr((char) 0) != 65535) throw new AssertionError("char-- must wrap 0 -> MAX");

        // comparisons are unsigned: 65535 > 0 and 65535 > 1.
        if (!charGt((char) 65535, (char) 0)) throw new AssertionError("unsigned compare MAX > 0");
        if (charLt((char) 65535, (char) 1)) throw new AssertionError("unsigned compare MAX > 1");

        // surrogate code points are valid char values.
        if ('\ud800' != 0xD800) throw new AssertionError("high surrogate literal");
        if ('\udfff' != 0xDFFF) throw new AssertionError("low surrogate literal");

        System.out.println("PASS");
    }
}
