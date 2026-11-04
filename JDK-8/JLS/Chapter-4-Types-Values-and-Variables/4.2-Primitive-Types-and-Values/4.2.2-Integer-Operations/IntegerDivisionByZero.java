/*
 * @test
 * @summary JLS 4.2.2 (Java SE 8): the integer divide operator / and the integer
 *          remainder operator % can throw ArithmeticException if the right-hand
 *          operand is zero. Any integer operator can throw NullPointerException
 *          if unboxing of a null reference is required.
 * @run main IntegerDivisionByZero
 */

public class IntegerDivisionByZero {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Throwing { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> ex, Throwing r, String msg) throws Exception {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + ex.getSimpleName() + " got " + t, t);
        }
        throw new AssertionError(msg + ": expected " + ex.getSimpleName() + " but nothing thrown");
    }

    public static void main(String[] args) throws Exception {
        // Integer division by zero throws ArithmeticException.
        checkThrows(ArithmeticException.class, () -> { int x = 1 / 0; },
                    "int division by zero");

        // Integer remainder by zero throws ArithmeticException.
        checkThrows(ArithmeticException.class, () -> { int x = 1 % 0; },
                    "int remainder by zero");

        // Long division by zero throws ArithmeticException too.
        checkThrows(ArithmeticException.class, () -> { long x = 1L / 0L; },
                    "long division by zero");

        // Unboxing a null Integer reference throws NullPointerException.
        Integer boxedNull = null;
        checkThrows(NullPointerException.class, () -> { int x = boxedNull + 1; },
                    "unboxing a null reference throws NPE");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
