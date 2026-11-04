/*
 * @test
 * @summary JLS 12.4.1: using a static field that is a constant variable does not
 *          trigger initialization of the class that declares it.
 * @run main ConstantVariableNoInit
 */

public class ConstantVariableNoInit {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean constInitialized;
    static boolean nonConstInitialized;

    static class HasConst {
        static final int CONST = 42;   // constant variable
        static { constInitialized = true; }
    }

    static class HasNonConst {
        static final int NOT_CONST = compute();   // not a constant variable
        static { nonConstInitialized = true; }
        static int compute() { return 1; }
    }

    public static void main(String[] args) {
        int c = HasConst.CONST;
        check(c == 42, "constant value inlined");
        check(!constInitialized, "constant variable use does not initialize the class");

        int n = HasNonConst.NOT_CONST;
        check(n == 1, "non-constant final value");
        check(nonConstInitialized, "non-constant final use does initialize the class");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
