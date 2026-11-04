/*
 * @test
 * @summary java.text.ParseException API conformance: checked hierarchy, error offset, and
 *          thrown by SimpleDateFormat.parse.
 * @run main ParseExceptionTest
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ParseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testParseException() throws Exception {
        ParseException e = new ParseException("bad", 5);
        check("bad".equals(e.getMessage()), "message");
        check(e.getErrorOffset() == 5, "error offset");
        check(e instanceof Exception, "is checked Exception");
        check(!((Object) e instanceof RuntimeException), "not unchecked");

        boolean thrown = false;
        try {
            new SimpleDateFormat("yyyy").parse("not-a-year");
        } catch (ParseException ex) {
            thrown = true;
        }
        check(thrown, "SimpleDateFormat.parse throws ParseException");
    }

    public static void main(String[] args) throws Exception {
        testParseException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
