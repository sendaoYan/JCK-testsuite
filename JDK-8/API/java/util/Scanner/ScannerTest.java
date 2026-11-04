/*
 * @test
 * @summary java.util.Scanner API conformance: nextInt/next/hasNext, useDelimiter, and
 *          hasNextInt.
 * @run main ScannerTest
 */

import java.util.Scanner;

public class ScannerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTokens() {
        Scanner s = new Scanner("10 20 hello");
        check(s.nextInt() == 10 && s.nextInt() == 20, "nextInt");
        check("hello".equals(s.next()), "next");
        check(!s.hasNext(), "exhausted");
    }

    static void testDelimiter() {
        Scanner s = new Scanner("a,b,c").useDelimiter(",");
        check("a".equals(s.next()) && "b".equals(s.next()) && "c".equals(s.next()),
              "useDelimiter");
    }

    static void testHasNextInt() {
        Scanner s = new Scanner("42");
        check(s.hasNextInt() && s.nextInt() == 42, "hasNextInt/nextInt");
    }

    public static void main(String[] args) {
        testTokens();
        testDelimiter();
        testHasNextInt();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
