/*
 * @test
 * @summary JLS 3.7: traditional (/* ... *&#47;) and end-of-line (//) comments;
 *          comments do not nest; /* and *&#47; have no special meaning in //
 *          comments; // has no special meaning in /* comments.
 * @run main Comments
 */

public class Comments {
    public static void main(String[] args) {
        // end-of-line comment: /* and */ have no special meaning here

        /* traditional comment: // has no special meaning here */

        /** documentation-style comment is still a traditional comment */

        // Comments do not nest: the following single comment ends at the first
        // "*/", so the statement "int a = 1;" is NOT part of the comment.
        /* this comment /* // /** ends here: */ int a = 1;
        if (a != 1) {
            throw new AssertionError("comment swallowed the following statement");
        }

        // A traditional comment can span lines and is ignored entirely.
        int b = /* line 1
                    line 2
                    */ 2;
        if (b != 2) {
            throw new AssertionError("multi-line comment affected the value");
        }

        // The remainder of the line is ignored in an end-of-line comment.
        int c = 3; // c = 999;
        if (c != 3) {
            throw new AssertionError("end-of-line comment affected the value");
        }

        System.out.println("PASS");
    }
}
