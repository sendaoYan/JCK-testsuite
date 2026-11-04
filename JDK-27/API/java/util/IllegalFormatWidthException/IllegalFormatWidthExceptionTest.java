/*
 * @test
 * @summary java.util.IllegalFormatWidthException conformance: white-box structure + black-box behavior.
 * @run main IllegalFormatWidthExceptionTest
 */

import java.util.IllegalFormatException;
import java.util.IllegalFormatWidthException;

public class IllegalFormatWidthExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.IllegalFormatWidthException");
        check(c.getSuperclass() == IllegalFormatException.class, "extends IllegalFormatException");
        c.getDeclaredConstructor(int.class);
        check(c.getDeclaredMethod("getWidth").getReturnType() == int.class, "getWidth int");
    }

    static void testBlackBox() {
        IllegalFormatWidthException e = new IllegalFormatWidthException(-1);
        check(e.getWidth() == -1, "getWidth");
        check("-1".equals(e.getMessage()), "getMessage");
        check(new IllegalFormatWidthException(-3).getWidth() == -3, "negative width preserved");
        check(e instanceof IllegalFormatException, "instanceof IllegalFormatException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
