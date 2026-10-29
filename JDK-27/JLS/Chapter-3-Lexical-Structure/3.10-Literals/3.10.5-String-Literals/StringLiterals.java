/*
 * @test
 * @summary JLS 3.10.5: a string literal is zero or more characters in double
 *          quotes; its content may use escape sequences; a string literal
 *          always refers to the same (interned) instance; strings that are
 *          values of constant expressions are interned, while strings computed
 *          by run-time concatenation are newly created.
 * @run main StringLiterals
 */

public class StringLiterals {
    public static void main(String[] args) {
        // Content and escapes (JLS 3.10.5, 3.10.7).
        if (!"".isEmpty()) throw new AssertionError("empty string literal");
        if (!"\"".equals("\"")) throw new AssertionError("escaped double quote");
        if (!"This is a string".equals("This is a string")) {
            throw new AssertionError("string content mismatch");
        }
        if (!"\n".equals("\n")) throw new AssertionError("escaped linefeed");

        // Example 3.10.5-1: interning semantics.
        String hello = "Hello", lo = "lo";
        // Same literal -> same instance.
        if (hello != "Hello") throw new AssertionError("same literal must be same instance");
        // Constant expression -> computed at compile time, treated as literal.
        if (hello != ("Hel" + "lo")) throw new AssertionError("constant concat must be interned");
        // Run-time concatenation -> newly created and distinct.
        if (hello == ("Hel" + lo)) throw new AssertionError("runtime concat must be distinct");
        // Explicit interning yields the same instance.
        if (hello != ("Hel" + lo).intern()) throw new AssertionError("intern() must return literal instance");

        // A string literal is of type String.
        if (!("x" instanceof String)) throw new AssertionError("string literal must be a String");

        System.out.println("PASS");
    }
}
