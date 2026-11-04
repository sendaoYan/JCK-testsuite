/*
 * @test
 * @summary JLS 3.6 (Java SE 8): white space is the ASCII space, horizontal tab,
 *          and form feed characters and line terminators; any of them may
 *          separate tokens.
 * @run main WhiteSpace
 */

public class WhiteSpace {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        int	a	=	1	;		// tab-separated
        check(a == 1, "tab-separated tokens");

        int b=2;			// form-feed (\f) separated
        check(b == 2, "form-feed-separated tokens");

        int c = 3;   // ordinary spaces
        check(c == 3, "space-separated tokens");

        int    d    =    4    ;
        check(d == 4, "multiple white space");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
