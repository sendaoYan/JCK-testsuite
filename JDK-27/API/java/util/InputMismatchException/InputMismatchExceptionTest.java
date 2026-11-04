/*
 * @test
 * @summary java.util.InputMismatchException conformance: white-box structure + black-box behavior.
 * @run main InputMismatchExceptionTest
 */

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class InputMismatchExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.InputMismatchException");
        check(c.getSuperclass() == NoSuchElementException.class, "extends NoSuchElementException");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        check(c.getDeclaredConstructors().length == 2, "two constructors");
    }

    static void testBlackBox() {
        check(new InputMismatchException().getMessage() == null, "no-arg message null");
        check("bad".equals(new InputMismatchException("bad").getMessage()), "String ctor message");
        final Scanner s = new Scanner("xyz");
        expectThrows(s::nextInt, InputMismatchException.class, "Scanner nextInt mismatch");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
