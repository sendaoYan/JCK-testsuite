/*
 * @test
 * @summary JLS 3.10.5 (Java SE 8): a string literal is zero or more characters
 *          in double quotes; its content may use escape sequences; a string
 *          literal always refers to the same (interned) instance; strings that
 *          are values of constant expressions are interned, while strings
 *          computed by run-time concatenation are newly created.
 * @run main StringLiterals
 */

public class StringLiterals {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        check("".isEmpty(), "empty string literal");
        check("\"".equals("\""), "escaped double quote");
        check("This is a string".equals("This is a string"), "string content");
        check("\n".equals("\n"), "escaped linefeed");

        // Example 3.10.5-1 interning semantics.
        String hello = "Hello", lo = "lo";
        check(hello == "Hello", "same literal is the same instance");
        check(hello == ("Hel" + "lo"), "constant concat is interned");
        check(hello != ("Hel" + lo), "runtime concat is a distinct instance");
        check(hello == ("Hel" + lo).intern(), "intern() returns the literal instance");

        check("x" instanceof String, "a string literal is a String");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
