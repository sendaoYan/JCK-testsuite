/*
 * @test
 * @summary JLS 5.2: assignment contexts allow identity, widening primitive,
 *          widening reference, boxing, unboxing (each optionally followed by a
 *          widening primitive conversion), and the implicit narrowing of
 *          constant expressions of type byte/short/char/int. null may be
 *          assigned to any reference type.
 * @run main AssignmentContexts
 */

public class AssignmentContexts {
    public static void main(String[] args) {
        // Constant expression narrowing: int literal 42 -> byte.
        byte theAnswer = 42;
        if (theAnswer != 42) throw new AssertionError("constant narrowing to byte");
        short s = 42;
        char c = 65;            // constant int -> char
        if (c != 'A') throw new AssertionError("constant narrowing to char");

        // Widening primitive conversion.
        short sh = 12;
        float f = sh;           // short -> float
        if (f != 12.0f) throw new AssertionError("short -> float");

        // Widening reference conversion.
        Object o = "str";

        // Boxing conversion.
        Integer i = 5;

        // Boxing followed by widening reference conversion.
        Object oi = 5;          // int -> Integer -> Object

        // Unboxing conversion.
        int j = i;

        // Unboxing followed by widening primitive conversion.
        double d = i;           // Integer -> int -> double

        // null to any reference type.
        String ns = null;

        if (theAnswer == 42 && i == 5 && j == 5 && d == 5.0 && ns == null) {
            System.out.println("PASS");
        } else {
            throw new AssertionError("assignment conversions");
        }
    }
}
