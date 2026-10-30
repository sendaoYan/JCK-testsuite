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

    public static void main(String[] args) {
        testHierarchyAndCtor();
        testThrownByScanner();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
