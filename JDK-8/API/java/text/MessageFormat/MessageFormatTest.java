/*
 * @test
 * @summary java.text.MessageFormat API conformance: argument substitution.
 * @run main MessageFormatTest
 */

import java.text.MessageFormat;

public class MessageFormatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMessageFormat() {
        MessageFormat mf = new MessageFormat("{0} + {1} = {2}");
        String s = mf.format(new Object[]{1, 2, 3});
        check("1 + 2 = 3".equals(s), "format substitution");
    }

    public static void main(String[] args) {
        testMessageFormat();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
