/*
 * @test
 * @summary JLS 14.30.3: a type pattern whose type is a supertype is unconditional
 *          and applicable for the selector type; a primitive type pattern is
 *          applicable and unconditional for its own type (via a record
 *          component).
 * @run main UnconditionalPattern
 */

record P(int v) {}

public class UnconditionalPattern {
    public static void main(String[] args) {
        Object x = "str";

        // Object is unconditional for every reference type.
        if (!(x instanceof Object o)) throw new AssertionError("Object pattern");
        // Applicable: testing conversion String -> CharSequence exists.
        if (!(x instanceof CharSequence cs)) throw new AssertionError("CharSequence pattern");

        // Primitive type pattern applicable and unconditional at its own type.
        Object p = new P(7);
        if (!(p instanceof P(int v)) || v != 7)
            throw new AssertionError("primitive component pattern");

        System.out.println("PASS");
    }
}
