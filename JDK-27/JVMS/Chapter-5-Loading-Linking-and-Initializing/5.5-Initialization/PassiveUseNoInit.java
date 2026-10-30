/*
 * @test
 * @summary JVMS 5.5: passive uses do not trigger initialization: reading a
 *          compile-time constant and creating an array of a type do not initialize
 *          the declaring/element class.
 * @run main PassiveUseNoInit
 */

public class PassiveUseNoInit {
    static boolean constInit = false;
    static boolean arrayInit = false;

    static class HasConst {
        static final int CONST = 42;   // compile-time constant
        static { constInit = true; }
    }

    static class Elem {
        static { arrayInit = true; }
    }

    public static void main(String[] args) {
        int c = HasConst.CONST;       // passive use: constant is inlined
        if (c != 42) throw new AssertionError("CONST");
        if (constInit) throw new AssertionError("constant access initialized class");

        Elem[] arr = new Elem[10];    // passive use: array creation
        if (arrayInit) throw new AssertionError("array creation initialized element class");

        Elem e = new Elem();          // active use
        if (!arrayInit) throw new AssertionError("new did not initialize class");

        System.out.println("PASS");
    }
}
