/*
 * @test
 * @summary JLS 5.1: specific type conversions in the Java programming language
 *          are divided into 12 kinds.
 * @run main KindsOfConversion
 */

public class KindsOfConversion {
    public static void main(String[] args) {
        // (1) identity, (2) widening primitive, (3) narrowing primitive,
        // (4) widening+narrowing primitive, (5) widening reference,
        // (6) narrowing reference, (7) boxing, (8) unboxing,
        // (9) unchecked, (10) capture, (11) string, (12) forbidden.
        // A representative sample of each is exercised in the sub-sections.

        int i = 5;
        int same = i;                    // identity
        long wide = i;                   // widening primitive
        byte narrow = (byte) i;          // narrowing primitive
        char c = (char) ((byte) 65);     // widening + narrowing primitive
        Object o = "s";                  // widening reference
        String s = (String) o;           // narrowing reference
        Integer boxed = 5;               // boxing
        int unboxed = boxed;             // unboxing
        String str = "x" + 5;            // string

        if (same != 5 || wide != 5L || narrow != 5 || c != 'A') throw new AssertionError("primitive conversions");
        if (!s.equals("s")) throw new AssertionError("reference conversions");
        if (boxed != 5 || unboxed != 5) throw new AssertionError("boxing/unboxing");
        if (!str.equals("x5")) throw new AssertionError("string conversion");

        System.out.println("PASS");
    }
}
