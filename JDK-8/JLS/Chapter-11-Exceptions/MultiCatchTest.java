/*
 * @test
 * @summary JLS 11.2/14.20 (Java SE 8): catch clauses are tried in order, a
 *          superclass catch catches subclasses, and Java 8 supports multi-catch.
 * @run main MultiCatchTest
 */

public class MultiCatchTest {
    static String classify(Exception t) {
        try {
            throw t;
        } catch (ArithmeticException e) {
            return "arith";
        } catch (RuntimeException e) {
            return "runtime";
        } catch (Exception e) {
            return "exception";
        }
    }

    public static void main(String[] args) {
        if (!classify(new ArithmeticException()).equals("arith")) throw new AssertionError("specific catch");
        if (!classify(new IllegalStateException()).equals("runtime")) throw new AssertionError("superclass catch");
        if (!classify(new Exception()).equals("exception")) throw new AssertionError("Exception catch");

        // Multi-catch (Java 7+): one clause for multiple alternative types.
        try {
            throw new NumberFormatException();
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            // NumberFormatException is a subclass of IllegalArgumentException.
        }
        System.out.println("PASS");
    }
}
