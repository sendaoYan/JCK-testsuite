/*
 * @test
 * @summary JLS 3.6: white space is the ASCII space, horizontal tab, and form
 *          feed characters and line terminators; any of them may separate
 *          tokens.
 * @run main WhiteSpace
 */

public class WhiteSpace {
    public static void main(String[] args) {
        // Spaces, tabs and form feeds between tokens.
        int	a	=	1	;		// tab-separated
        if (a != 1) throw new AssertionError("tab-separated tokens");

        int b=2;			// form-feed (\f) separated
        if (b != 2) throw new AssertionError("form-feed-separated tokens");

        int c = 3;   // ordinary spaces
        if (c != 3) throw new AssertionError("space-separated tokens");

        // Multiple consecutive white space characters are a single separator.
        int    d    =    4    ;
        if (d != 4) throw new AssertionError("multiple white space");

        System.out.println("PASS");
    }
}
