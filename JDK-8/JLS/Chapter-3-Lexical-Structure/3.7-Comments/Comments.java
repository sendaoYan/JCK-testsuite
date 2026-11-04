/*
 * @test
 * @summary JLS 3.7 (Java SE 8): traditional (/* ... *&#47;) and end-of-line (//)
 *          comments; comments do not nest; /* and *&#47; have no special meaning
 *          in // comments; // has no special meaning in /* comments.
 * @run main Comments
 */

public class Comments {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        // end-of-line comment: /* and */ have no special meaning here

        /* traditional comment: // has no special meaning here */

        /** documentation-style comment is still a traditional comment */

        // Comments do not nest: the following single comment ends at the first
        // "*/", so the statement "int a = 1;" is NOT part of the comment.
        /* this comment /* // /** ends here: */ int a = 1;
        check(a == 1, "comment did not swallow the following statement");

        // A traditional comment can span lines and is ignored entirely.
        int b = /* line 1
                    line 2
                    */ 2;
        check(b == 2, "multi-line comment affected the value");

        // The remainder of the line is ignored in an end-of-line comment.
        int c = 3; // c = 999;
        check(c == 3, "end-of-line comment affected the value");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
