/*
 * @test
 * @summary java.util.InputMismatchException API conformance: extends NoSuchElementException,
 *          the (String) constructor, and Scanner.nextInt() throwing it on a non-int token.
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

    static void testHierarchyAndCtor() {
        InputMismatchException e = new InputMismatchException("msg");
        check(e instanceof NoSuchElementException, "extends NoSuchElementException");
        check("msg".equals(e.getMessage()), "(String) constructor");
    }

    static void testThrownByScanner() {
        try {
            new Scanner("abc").nextInt();
            throw new AssertionError("nextInt on non-int: expected InputMismatchException");
        } catch (InputMismatchException expected) {
            passed++;
        }
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

    public static void main(String[] args) {
        testHierarchyAndCtor();
        testThrownByScanner();
        testMismatchContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
