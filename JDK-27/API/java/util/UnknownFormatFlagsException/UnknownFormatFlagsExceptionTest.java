/*
 * @test
 * @summary java.util.UnknownFormatFlagsException conformance: white-box structure + black-box behavior.
 * @run main UnknownFormatFlagsExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.UnknownFormatFlagsException;

public class UnknownFormatFlagsExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.UnknownFormatFlagsException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredMethod("getFlags").getReturnType() == String.class, "getFlags String");
    }

    static void testBlackBox() {
        UnknownFormatFlagsException e = new UnknownFormatFlagsException("!");
        check("!".equals(e.getFlags()), "getFlags");
        check("Flags = !".equals(e.getMessage()), "getMessage");
        check(e instanceof IllegalFormatException, "instanceof IllegalFormatException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
