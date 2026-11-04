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


    // ===================== 契约深化：层次/构造矩阵/Scanner 触发 =====================
    static void testMismatchContracts() {
        check(InputMismatchException.class.getSuperclass() == java.util.NoSuchElementException.class,
              "extends NoSuchElementException");
        check(new InputMismatchException().getMessage() == null, "no-arg ctor null message");
        check("why".equals(new InputMismatchException("why").getMessage()), "message ctor");
        java.util.Scanner sc = new java.util.Scanner("notanumber");
        check(sc.hasNext(), "token available");
        try {
            sc.nextInt();
            check(false, "Scanner nextInt on non-number IME");
        } catch (InputMismatchException e) {
            check(true, "Scanner nextInt on non-number IME");
        }
        sc.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMismatchContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
