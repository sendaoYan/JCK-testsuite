/*
 * @test
 * @summary JLS 12.4 (Java SE 8): a class is initialized lazily on its first active
 *          use; a superclass is initialized before its subclasses; and using a
 *          compile-time constant does not trigger initialization.
 * @run main InitializationTest
 */

public class InitializationTest {
    static StringBuilder log = new StringBuilder();

    static class Super { static { log.append("S"); } }
    static class Sub extends Super {
        static int X = initX();
        static int initX() { log.append("x"); return 1; }
    }
    static class HasConst {
        static final int CONST = 5;   // compile-time constant
        static { log.append("C"); }
    }

    public static void main(String[] args) {
        Sub s = null;                 // does not initialize the class
        if (log.length() != 0) throw new AssertionError("no init on null assignment");

        int x = Sub.X;                // active use: Super first, then Sub
        if (!log.toString().equals("Sx")) throw new AssertionError("superclass before subclass: " + log);

        log.setLength(0);
        int c = HasConst.CONST;       // compile-time constant: no initialization
        if (log.length() != 0) throw new AssertionError("constant does not trigger init");
        if (c != 5) throw new AssertionError("constant value");

        System.out.println("PASS");
    }
}
