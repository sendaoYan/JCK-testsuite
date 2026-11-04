/*
 * @test
 * @summary JVMS 2.5.5: the run-time constant pool is derived from the class
 *          file's constant pool and holds resolved symbolic references, so
 *          string literals denote the same interned String object and class
 *          literals denote the same Class object as Class.forName.
 * @run main RunTimeConstantPoolInterning
 */

public class RunTimeConstantPoolInterning {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        // A string literal is interned and shared with intern().
        String lit = "abc";
        check(lit == "abc", "string literal is canonical");
        check(lit == lit.intern(), "intern() returns the same instance");
        check(("a" + "b" + "c").intern() == "abc",
                "intern() unifies equal strings");
        check("x" + 1 + true + 'c' + 1L + 1.0f != null,
                "concatenation resolves to a String");

        // A class literal and Class.forName resolve to the same Class object.
        check(String.class == Class.forName("java.lang.String"),
                "class literal and Class.forName denote the same Class");
        check(int[].class == Class.forName("[I"),
                "array class literal and Class.forName denote the same Class");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
