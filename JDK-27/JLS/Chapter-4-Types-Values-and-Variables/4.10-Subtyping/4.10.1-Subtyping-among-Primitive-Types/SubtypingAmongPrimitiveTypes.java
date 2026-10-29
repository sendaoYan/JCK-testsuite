/*
 * @test
 * @summary JLS 4.10.1: direct supertype relation among primitive types:
 *          double >1 float >1 long >1 int >1 char, int >1 short >1 byte.
 * @run main SubtypingAmongPrimitiveTypes
 */

public class SubtypingAmongPrimitiveTypes {
    public static void main(String[] args) {
        // Each primitive type is assignable to its direct (and transitive) supertypes.
        byte b = 1;
        short s = b;      // byte <: short
        int i = s;        // short <: int
        long l = i;       // int <: long
        float f = l;      // long <: float
        double d = f;     // float <: double

        char c = 'A';
        int ic = c;       // char <: int
        long lc = c;      // char <: long
        double dc = c;    // char <: double

        if (d != 1.0) throw new AssertionError("byte widened to double");
        if (ic != 'A') throw new AssertionError("char widened to int");

        // Widening conversions are lossless for these small values.
        if (l != 1L) throw new AssertionError("int widened to long");

        System.out.println("PASS");
    }
}
